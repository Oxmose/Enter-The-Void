package com.alexytorres.enterthevoid;

import java.util.ArrayList;

import com.alexytorres.enterthevoid.model.Direction;
import com.alexytorres.enterthevoid.model.LevelLoader;
import com.alexytorres.enterthevoid.model.Moves;
import com.alexytorres.enterthevoid.model.Preferences;
import com.alexytorres.enterthevoid.model.StructLevel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {
	
	// Singleton des préférences
	private Preferences prefs;
	
	// Elements du layout activity_game
	private TextView levelTitle;
	private TextView boundsLeft;
	private ImageView ball;
	private ImageView hole;	
	private ImageView arrow;
	private RelativeLayout gamePanel;	
	private View topLine;
	private Button menuButton;

	// Thread et boolean de mouvement
	private Thread lauchThread = null;
	boolean launching = false;
		
	// Paramètres et informations de niveau
	private int levelNumber;
	private StructLevel level;
	private int boundsNbLeft;
	private TextView looseText;	
	private ArrayList<int[]> walls;
	
	// Gestion des FPS
	private int frameRate = 1000 / 125;
	
	// Gestion du redémmarage du niveau
	private int restart = 0;
	private int restartCounter = 0;
	
	// Vécteurs de déplacement de la balle
	private double vectorX;
	private double vectorY;
	
	// Gestion de la reprise du thread après un onPause de l'activity
	private Object mPauseLock;
    private boolean mPaused;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);		
		Bundle b = getIntent().getExtras();
		
		// Récupération des information du niveau
		levelNumber = b.getInt("Level");	
		
		prefs = Preferences.getInstance(getApplicationContext());
		level = LevelLoader.LoadLevel(levelNumber, getApplicationContext());
		walls = new ArrayList<int[]>();
		
		// Récupération des éléments du layout
		levelTitle = (TextView) findViewById(R.id.activity_game_textview_level);
		boundsLeft = (TextView) findViewById(R.id.activity_game_textview_bounds);
		looseText = (TextView) findViewById(R.id.activity_game_textview_loose);		
		ball = (ImageView) findViewById(R.id.activity_game_image_ball);
		hole = (ImageView) findViewById(R.id.activity_game_image_hole);		
		gamePanel = (RelativeLayout) findViewById(R.id.activity_game_layout_main);		
		arrow = (ImageView) findViewById(R.id.activity_game_image_arrow);
		topLine = findViewById(R.id.activity_game_topline);
		menuButton = (Button) findViewById(R.id.activity_game_menubutton);
		
		// Paramétrage de l'activity
		looseText.setVisibility(View.INVISIBLE);		
		boundsNbLeft = level.maxbounds;
		createGameLoop();
		loadBoard();
		
		
		menuButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), MainActivity.class);
	        	startActivity(intent);		
	        	   
	        	GameActivity.this.finish();
			}
		});
		
		// Lock pour le thread de mouvement (un seul thread UI bloquait la mise à jour de l'affichage)
		mPauseLock = new Object();
		
		// Parametrage des action de "touch screen" tactile
		gamePanel.setOnTouchListener(new OnTouchListener() {
			
			// Point de départ
			PointF startPt = null;			
			
			@Override
		    public boolean onTouch(View v, MotionEvent event) {
				v.performClick();
		        int eventId = event.getAction();	        
		        
		        switch (eventId) {
		            case MotionEvent.ACTION_MOVE :
		            	
		            	// Si la balle n'est pas déja lancée
		            	if (!launching) {
		            		
		            		// Point de départ du mouvement
			            	startPt = new PointF(event.getX(), event.getY());
			            	
			            	// Calcul de l'angle que produit le doigt par rapport au centre de la balle sur le cercle trigonmétrique
			            	float angle = (float) Math.toDegrees(Math.atan2(event.getX() - (ball.getX()),    event.getY() -   (ball.getY())));
			            	
			            	if (angle < 0) {
			                    angle += 360;
			                }
			            	
			            	// Rotation de la flèche
			            	arrow.setPivotX(LevelLoader.convertPx(100, getBaseContext()));
			            	arrow.setPivotY(LevelLoader.convertPx(280, getBaseContext()));
			            	arrow.setRotation(-angle + 180);
		            	}
		            			                
		                break;
		                
		            case MotionEvent.ACTION_DOWN :
		            	
		            	// Si la balle n'est pas lancée, on gère le mouvement de la flèche de lancé
		            	if (!launching) {
		            		// Point de départ du mouvement
			            	startPt = new PointF(event.getX(), event.getY());
			            	
			            	// Calcul de l'angle que produit le doigt par rapport au centre de la balle sur le cercle trigonmétrique
			            	float angle = (float) Math.toDegrees(Math.atan2(event.getX() - (ball.getX()),    event.getY() -   (ball.getY())));
			            	
			            	if(angle < 0){
			                    angle += 360;
			                }
			            	
			            	// Rotation de la flèche
			            	arrow.setPivotX(LevelLoader.convertPx(100, getBaseContext()));
			            	arrow.setPivotY(LevelLoader.convertPx(280, getBaseContext()));
			            	arrow.setRotation(-angle + 180);
		            	}
		            	else {
		            		// Incrémentation du conpteur pour redémmarer (2 touches d'écran)
		            		++restart;
		            		
		            		// Si on a touché deux fois l'écran dans le temps impartit (géré plus loins)
		            		if (restart == 2) {
		            			
		            			// On démmare la même activity
		            			Intent intent = new Intent(getApplication(), GameActivity.class);
								Bundle b = new Bundle();
								b.putInt("Level", levelNumber);
								intent.putExtras(b);
								startActivity(intent);
								   
								// On ferme celle-ci
								GameActivity.this.finish();	
		            		}
		            	}		            	
		                break;
		                
		            case MotionEvent.ACTION_UP :	
		            	
		            	// Si la balle n'est pas lancée, on la lance
		            	if (!launching)		            		
		            	{
		            		// Lancement en cours
			            	launching = true;
			            	
			            	// Calcul des vecteurs de déplacement
			            	vectorX = startPt.x - ball.getX();
			            	vectorY = startPt.y - ball.getY();		            	
			            	
			            	// Paramétrage de l'animation de la flèche
			            	AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
	            			anim.setDuration(500);
	            			anim.setAnimationListener(new AnimationListener() {
	            				
								@Override
								public void onAnimationEnd(Animation animation) {
									// Cache finalement la balle au lieu de la remmetre à un alpha de 1.0
									arrow.setVisibility(View.INVISIBLE);									
								}

								@Override
								public void onAnimationStart(Animation animation) {}
								@Override
								public void onAnimationRepeat(Animation animation) {}
							});
	            			
	            			// Démmarage de l'animation de la balle et du threade de mouvement
	            			arrow.startAnimation(anim);			            				            	
			            	lauchThread.start();
		            	}
		                break;
		                
		            default :
		                break;
		        }
		        
		        return true;
		    }
		}); // gamepanel.setOnTouchListener
	} // onCreate ()
	
	private void animateHoleBall() {
		Runnable runable = new Runnable() {			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {						
						
						// Animation du trou (fadeIn puis fadeOut) appel récursif
						final AlphaAnimation fadeOut = new AlphaAnimation(0.1f, 1.0f);
						fadeOut.setDuration(1000);
						final AlphaAnimation fadeIn = new AlphaAnimation(1.0f, 0.1f);
						fadeIn.setDuration(1000);
						
						fadeOut.setAnimationListener(new AnimationListener() {
							
							@Override
							public void onAnimationStart(Animation animation) {}
							
							@Override
							public void onAnimationRepeat(Animation animation) {}
							
							@Override
							public void onAnimationEnd(Animation animation) {
								hole.startAnimation(fadeIn);	
								
							}
						});						
						fadeIn.setAnimationListener(new AnimationListener() {
							
							@Override
							public void onAnimationEnd(Animation animation) {
								hole.startAnimation(fadeOut);									
							}

							@Override
							public void onAnimationStart(Animation animation) {}

							@Override
							public void onAnimationRepeat(Animation animation) {}
						});						
						
						// Lancement de l'animation
						hole.startAnimation(fadeOut);
					}
				});
			}
		};
		
		// Lancement du thread de l'animation
		runable.run();
	} // animateHoleBall

	private void loadBoard() {
		
		// Placement du trou
		hole.setX(level.holex);
		hole.setY(level.holey);

		// Placement des murs
		for (int[] wall : level.walls)
		{	
			// Pas de dessins sur canvas, cela pause problème au niveau des colisions
	        View wallConcrete = new View(getBaseContext());
	        wallConcrete.setX(wall[0]);
	        wallConcrete.setY(wall[1]);
	        wallConcrete.setLayoutParams(new LayoutParams(wall[2], wall[3]));
	        
	        if (wall[4] == 0)
	        	wallConcrete.setBackgroundColor(Color.CYAN);
	        else
	        	wallConcrete.setBackgroundColor(Color.RED);
	        
	        // Ajout du nouveau mur
	        gamePanel.addView(wallConcrete);
			
	        // Ajout des coordonnées du mur courant dans le tableau des murs
	        // But : gestion des colisions
	        walls.add(wall);		        
		}
		
		// Paramétrage des autres éléments
		levelTitle.setText(getString(R.string.level, levelNumber));
		boundsLeft.setText(getResources().getQuantityString(R.plurals.reboundsLeft, boundsNbLeft, boundsNbLeft));
		
		// Appel de l'animation du trou
		animateHoleBall();
	} // loadBoard()

	public void winFunction() {
		// On stop le lancement (sécurité)
		launching = false;
		
		// Si le niveau suivant éxiste, se sera le niveau courant en case de reprise de la partie
		if (LevelLoader.exists(levelNumber + 1, GameActivity.this.getApplicationContext()))
			prefs.setCurrentLevel(levelNumber + 1);
		
		// Annimation pour faire disparaitre la balle
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		alphaAnim.setDuration(1500);
		alphaAnim.setAnimationListener(new AnimationListener() {

	
			@Override
			public void onAnimationEnd(Animation animation) {
				ball.setVisibility(View.INVISIBLE);	
				
				// Création des boites de dialogue de fin de partie
				AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
				
				@SuppressLint("InflateParams")
				View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_win_popup, null);
				builder.setView(dialogView);
				
				// Bouton "Partager"
				builder.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
				           @Override
				           public void onClick(DialogInterface dialog, int id) {
				               Intent shareMessage = new Intent (Intent.ACTION_SEND);
				               shareMessage.setType("text/plain");
				               shareMessage.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage, levelNumber));
				               startActivity(Intent.createChooser(shareMessage, getString(R.string.shareWith)));
				               					
				               GameActivity.this.finish();				               
				           }
				       });
				
				// Bouton "Menu"
				builder.setNeutralButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						 
			        	 Intent intent = new Intent(getApplication(), GameActivity.class);
			        	 Bundle b = new Bundle();
			        	 b.putInt("Level", levelNumber);
			        	 intent.putExtras(b);
			        	 startActivity(intent);
		        	   
			        	 GameActivity.this.finish();	
					}
				});
				
				// Bouton "niveau suivant"
				builder.setNegativeButton(R.string.nextlevel, new DialogInterface.OnClickListener() {
				           @SuppressLint("InflateParams")
						public void onClick(DialogInterface dialog, int id) {
				        	   
				        	   if (LevelLoader.exists(levelNumber + 1, GameActivity.this.getApplicationContext())) {				        		   
					        	   Intent intent = new Intent(getApplication(), GameActivity.class);
					        	   Bundle b = new Bundle();
					        	   b.putInt("Level", levelNumber + 1);
					        	   intent.putExtras(b);
					        	   startActivity(intent);
					        	   
					        	   GameActivity.this.finish();	
				        	   }
				        	   else {			
				        		   	// Si le niveau suivant n'éxiste pas on affiche la boite de dialogue de fin du jeu
				        		   	AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
				   				
				   					View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_end_popup, null);
				   					builder.setView(dialogView);
				   					
				   					// Bouton "Contact"
				   					builder.setPositiveButton(R.string.menu_action_contact, new DialogInterface.OnClickListener() {
								           @Override
								           public void onClick(DialogInterface dialog, int id) {
								        	    Intent intent = new Intent(Intent.ACTION_SEND);
									   			intent.setType("message/rfc822");
									   			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"alexy.torresa@gmail.com"});
									   			intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_body));
	
									   			try {
									   			    startActivity(Intent.createChooser(intent, getString(R.string.sendmail)));
									   			} catch (android.content.ActivityNotFoundException ex) {
									   			    Toast.makeText(GameActivity.this, getString(R.string.nomailclient), Toast.LENGTH_SHORT).show();
									   			}
									   			
									   			GameActivity.this.finish();
								           }
								       });
				   					
				   					// Bouton "Menu"
				   					builder.setNegativeButton(R.string.mainmenu, new DialogInterface.OnClickListener() {
								           @Override
								           public void onClick(DialogInterface dialog, int id) {	
								        	   
								        	   GameActivity.this.finish();	
								           }
								       });
				   					
				   					
				   					Dialog dialogEnd = builder.create();
				   					dialogEnd.setCancelable(false);
				   					
				   					TextView winInfo = (TextView) dialogView.findViewById(R.id.dialog_end_popup_infotext);
				   					winInfo.setText(getString(R.string.endtext));
				   					dialogEnd.show();
				        	   }
					        	   
					        	   
				           }
				       });
				
				// Création et fin de paramétrage des boites de dialogues
				Dialog dialog = builder.create();
				dialog.setCancelable(false);
				
				TextView winInfo = (TextView) dialogView.findViewById(R.id.dialog_win_popup_infotext);
				winInfo.setText(getString(R.string.wintext, levelNumber));
				dialog.show();
				
					
			}

			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
		
		AnimationSet s = new AnimationSet(false);
		s.addAnimation(alphaAnim);

		double vectorX = hole.getX() - ball.getX();
		double vectorY = hole.getY() - ball.getY();
		
		TranslateAnimation transAnim = new TranslateAnimation(0f, (float)vectorX, 0f, (float)vectorY);
		transAnim.setDuration(1000);
		transAnim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				
				if (v.hasVibrator() && prefs.isVibrateOnWin()) {		            						
					v.vibrate(500);
				}	
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {}
		});
		
		s.addAnimation(transAnim);		
		
		ball.startAnimation(s);
		
	} // winFunction()
	
	public void looseFunction()	{
		boundsLeft.setText(getResources().getQuantityString(R.plurals.reboundsLeft, boundsNbLeft, boundsNbLeft));
		
		// Paramétrage de l'animation de la balle
		AlphaAnimation alphaAnimBall = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimBall.setDuration(1000);
		alphaAnimBall.setAnimationListener(new AnimationListener() {			
			@Override
			public void onAnimationEnd(Animation animation) {
				ball.setVisibility(View.INVISIBLE);									
			}

			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});		

		// Animation de texte permettant de redémarrer le niveau
		AlphaAnimation alphaAnimText = new AlphaAnimation(0.0f, 1.0f);
		alphaAnimText.setDuration(1000);
		alphaAnimText.setAnimationListener(new AnimationListener() {			
			@Override
			public void onAnimationEnd(Animation animation) {
				looseText.setVisibility(View.VISIBLE);									
			}

			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
		
		// Paramétrage du texte de redémarage
		looseText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), GameActivity.class);
				Bundle b = new Bundle();
				b.putInt("Level", levelNumber);
				intent.putExtras(b);
				startActivity(intent);
				
				GameActivity.this.finish();	
				
			}
		});
		
		// Démmarage des animations
		looseText.startAnimation(alphaAnimText);
		ball.startAnimation(alphaAnimBall);
	} // looseFunction
	
	public void createGameLoop() {
		
		// Définition du thread de jeu
		lauchThread = new Thread (new Runnable() {
			
			public void run () {	
				// Réduction de la vitesse de la balle (simplifie les colisions)
				vectorX = vectorX / 100;
				vectorY = vectorY / 100;
							  
				// Game loop
				while (launching) {
					
					// Gestion du redémarage prématuré du niveau
					if (restartCounter < 100 && restart == 1) {
						++restartCounter;
					} 
					else {
						restartCounter = 0;
						restart = 0;
					}
					
					
					// Récupère le temps au début de la boucle (pour gérer les FPS)
					long startTime = SystemClock.uptimeMillis();
					
					// Tests des colisions
					int borderTouched = Moves.touchedBorder(ball, gamePanel, vectorX, vectorY, topLine);
					int touchedObstacle = Moves.touchedObstacle(ball, walls, vectorX, vectorY);
					
					// Gestion des colision contre les bords
					if(borderTouched != 0) {
						
						// Décrémentation du nombre de rebonds restants
						--boundsNbLeft;
						
						// Si on a dépassé le nombre maximal de rebonds
						if (boundsNbLeft < 0) {
							
							boundsNbLeft = 0;
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									looseFunction();													
								}
							});
							return;
						}
						
						// Ajout d'un rebond au statistiques
						prefs.setReboundsNumber(prefs.getReboundsNumber() + 1);
						
						// Gestion de la direction de la balle
						Direction direction = new Direction(vectorX, vectorY);
						
						// Prise de connaissance du bord touché et réaction en conséquence
						// Ajout de marge de décalage pour éviter un compte de plusieurs rebond en un
						switch (borderTouched) {
							
	    					case 1:
								direction.changeDirectionX();
								Moves.moveX(GameActivity.this, ball, 1);
								break;
								
							case 2:
								direction.changeDirectionX();
								Moves.moveX(GameActivity.this, ball, gamePanel.getWidth() - ball.getWidth() - 1);
								break;
								
							case 3:
								direction.changeDirectionY();
								Moves.moveY(GameActivity.this, ball, topLine.getY() + 1);
								break;
								
							case 4:
								direction.changeDirectionY();
								Moves.moveY(GameActivity.this, ball, gamePanel.getHeight() - ball.getHeight() - 1);			            							
								break;
																								
							default:
								break;
						}
						
						// Modification du nombre de rebond (affichage)
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								boundsLeft.setText(getResources().getQuantityString(R.plurals.reboundsLeft, boundsNbLeft, boundsNbLeft));													
							}
						});
						
						// Changement des vecteurs de déplacement donnant la direction
						vectorX = direction.getX();
						vectorY = direction.getY();
					}		            				
					
					// Gestion de colision contre un mur
					if (touchedObstacle != 0) {
						
						// Décrémentation du nombre de rebonds restants
						--boundsNbLeft;
						
						// Si dépassment du nomre de rebons maximum
						if (boundsNbLeft < 0) {
							
							boundsNbLeft = 0;
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									looseFunction();													
								}
							});
							return;
						}
						
						// Ajout d'un rebond au statistiques
						prefs.setReboundsNumber(prefs.getReboundsNumber() + 1);
						
						// Gesiton de direction
						Direction direction = new Direction(vectorX, vectorY);
						
						// Réaction en fonction de la colision (direction)
						// Ajout de marges
						switch (touchedObstacle) {
	    					case 1:
								direction.changeDirectionY();
								Moves.moveY(GameActivity.this, ball, ball.getY() + 20);
								break;
								
							case 2:
								direction.changeDirectionY();	
								Moves.moveY(GameActivity.this, ball, ball.getY() - 20);
								break;
								
							case 3:								
								direction.changeDirectionX();		
								Moves.moveX(GameActivity.this, ball, ball.getX() + 20);
								break;
								
							case 4:			            							
								direction.changeDirectionX();
								Moves.moveX(GameActivity.this, ball, ball.getX() - 20);
								break;
								
							case 5:
								boundsNbLeft = 0;
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										looseFunction();													
									}
								});
								return;
								
							default:								
								break;
						}	            					
						
						// Modification de l'affichage (texte du nombre de rebonds restants)
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								boundsLeft.setText(getResources().getQuantityString(R.plurals.reboundsLeft, boundsNbLeft, boundsNbLeft));													
							}
						});
						
						// Changement des vecteurs de déplacement donnant la direction
						vectorX = direction.getX();
						vectorY = direction.getY();
					}
					
					// Test de colision avec le trou
					if (Moves.inHole(ball, hole)) {
						
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								winFunction();													
							}
						});
						
						return;
					}	
					
					// Déplacement de la balle
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Moves.moveBall(GameActivity.this, ball, vectorX, vectorY);
						}
					});	            				
					
					
					// Gestion des FPS
	                try {
	                	long endTime = SystemClock.uptimeMillis();
	                	long elapsedTime = endTime - startTime;
	                	
	                	if (elapsedTime < frameRate)
	                		Thread.sleep(frameRate - elapsedTime);
	                	
					} 
	                catch (InterruptedException e) {
						// On ne peut pas supportter ce genre d'exception
	                	// On arrète l'application et on affiche la stacktrace dans le log (DEV)
						e.printStackTrace();
					}
	                
	                // Gestion de reprise du niveau après un onPause ou onStop
	                synchronized (mPauseLock) {
		                while (mPaused) {
		                    try {
		                        mPauseLock.wait();
		                    } catch (InterruptedException e) {
		                    }
		                }
		            }
				}				
			
			}
				
		});
	} // createGameLoop()
	
	public void onResume() {
		super.onResume();
		
		// On débloque le lock et donc le thread gameloop
		synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
	} // onResume()
	
	public void onPause() {

		super.onPause();
		
		// On bloque le lock donc le thread gameloop
		synchronized (mPauseLock) {
            mPaused = true;
        }				
	} // onPause
}