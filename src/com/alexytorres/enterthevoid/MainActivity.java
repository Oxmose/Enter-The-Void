package com.alexytorres.enterthevoid;

import com.alexytorres.enterthevoid.model.Preferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	// Elements du layout acvitity_main
	private Button activity_main_button_newgame;
	private Button activity_main_button_resumegame;
	private Button activity_main_button_settings;
	private ImageView titleImage;
	
	// Singleton des préférences
	private Preferences prefs;
	
	// Cheat mode
	private int touchNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Récupération des boutons
		activity_main_button_newgame = (Button) findViewById(R.id.activity_main_button_newgame);
		activity_main_button_resumegame = (Button) findViewById(R.id.activity_main_button_resumegame);
		activity_main_button_settings = (Button) findViewById(R.id.activity_main_button_settings);
		titleImage = (ImageView) findViewById(R.id.activity_main_image_title);
		
		// Récupération des préférences
		prefs = Preferences.getInstance(getApplicationContext());
		
		// Pramétrage des boutons
		activity_main_button_resumegame.setEnabled(prefs.getCurrentLevel() > 1);
		
		titleImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				++touchNumber;
				if (touchNumber == 6) {
					touchNumber = 0;
					Intent intent = new Intent(getApplication(), GameActivity.class);
					Bundle b = new Bundle();
					b.putInt("Level", 11);
					intent.putExtras(b);
					startActivity(intent);
				}
			}
		});
		
		// Bouton "Reprendre la partie"
		activity_main_button_resumegame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Démarage de l'activité game avec le niveau courant
				Intent intent = new Intent(getApplication(), GameActivity.class);
				Bundle b = new Bundle();
				b.putInt("Level", prefs.getCurrentLevel());
				intent.putExtras(b);
				startActivity(intent);
			}
		});
		
		// Bouton "Nouvelle partie"
		activity_main_button_newgame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Démarage du jeu au niveau de plus bas
				Intent intent = new Intent(getApplication(), GameActivity.class);
				Bundle b = new Bundle();
				b.putInt("Level", 1);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
		
		// Bouton "Paramètres"
		activity_main_button_settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				
				// Ouverture de l'activité de paramétrage
				Intent intent = new Intent(getApplication(), SettingsActivity.class);
				startActivity(intent);
			}
		});
	} // onCreate()
	
	public void onResume() {
		super.onResume();
		activity_main_button_resumegame.setEnabled(prefs.getCurrentLevel() > 1);
	} // onResume()
	
	public void onStop() {
		super.onStop();
		
		// Sauvegarde des préférences en quittant l'application
		Preferences.getInstance(getApplicationContext()).savePreferences();
	} // onStop()
	
}
