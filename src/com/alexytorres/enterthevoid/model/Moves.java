package com.alexytorres.enterthevoid.model;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;

public class Moves {
	
	public static int touchedBorder(View element, View panel, double vectorX, double vectorY, View topLine)	{
		
		// Récupération des tailles du paneau de jeu
		int panelWidth = panel.getWidth();
		int panelHeight = panel.getHeight();
		
		// Récupération des taille de la balle
		int elementWidth = element.getWidth();
		int elementHeight = element.getHeight();
		
		// Récupération des coordonées de la balle
		float elementX = (float) (element.getX() + vectorX);
		float elementY = (float) (element.getY() + vectorY);		
		
		// Si on touche le haut
		if (elementX <= 0)
			return 1;
		
		// Si on touche à droite
		else if (elementX + elementWidth >= panelWidth)
			return 2;
		
		// Si on touche en bas
		else if (elementY <= topLine.getY() + 1)
			return 3;
		
		// Si on touche à gauche
		else if (elementY + elementHeight >= panelHeight)
			return 4;
		
		// Si il n'y à pas colision
		else
			return 0;
	} // touchedBorder()	
	
	public static boolean inHole(View element, View hole) {		
		
		// Récupéation des coordonées de la balle et du trou
		int[] holeLocation = new int[2];
		int[] elementLocaction = new int[2];
		
		element.getLocationInWindow(elementLocaction);
		hole.getLocationInWindow(holeLocation);
		
		// Création des hitbox des éléments
		Rect elementHitBox = new Rect(elementLocaction[0], elementLocaction[1], elementLocaction[0] + element.getWidth(), elementLocaction[1] + element.getHeight());
		Rect holeHitBox = new Rect(holeLocation[0], holeLocation[1], holeLocation[0] + hole.getWidth(), holeLocation[1] + hole.getHeight());
	
		// Vérification de la colision
		return (Rect.intersects(elementHitBox, holeHitBox));
	} // inHole()
	
	public static int touchedObstacle(View element, ArrayList<int[]> walls, double vectorX, double vectorY) {
		
		// A remplacer par un meilleur algorithme non "fait main"
		// Voir Sthephen ALGO
		
		// Récupération des coordonnées de la balle
		int elementLeft = (int) (element.getX() + vectorX);
		int elementRight = (int) elementLeft + element.getWidth();
		int elementTop = (int) (element.getY() + vectorY);
		int elementBottom = (int) elementTop + element.getHeight();
		
		for (int[] wall : walls) {	
			
			// Si on touche en bas
			if (elementTop < wall[1] + wall[3] && elementBottom > wall[1] + wall[3]  && (elementRight >= wall[0] && elementLeft <= wall[0] + wall[2]) && vectorY <= 0) {
				if (wall[4] == 1)
					return 5;
				else
					return 1;
			}			
			// Si on touche en haut
			else if (elementBottom > wall[1] && elementTop < wall[1] && (elementRight >= wall[0] && elementLeft <= wall[0] + wall[2]) && vectorY >=0) {
				if (wall[4] == 1)
					return 5;
				else
					return 2;
			}			
			// Si on touche à droite
			else if (elementLeft < wall[0] + wall[2] && elementRight > wall[0] + wall[2] && (elementBottom >= wall[1] && elementTop <= wall[1] + wall[3]) && vectorX <= 0) {
				if (wall[4] == 1)
					return 5;
				else	
					return 3;
			}			
			// Si on touche à gauche
			else if (elementRight > wall[0] && elementLeft < wall[0] && (elementBottom >= wall[1] && elementTop <= wall[1] + wall[3]) && vectorX >= 0) {
				if (wall[4] == 1)
					return 5;
				else
					return 4;
			}
			
		}
		
		// Si il n'y a pas colision
		return 0;        
	} // touchedObstacle()
	

	public static void moveBall(Activity activity, final View ball, final double vectorX, final double vectorY) {	
		
		// Sémaphore pour bloquer le thread de calcul plus rapide que l'UI
		final Semaphore semaphore = new Semaphore(0);
		
		Runnable move = new Runnable() {
			@Override
			public void run() {

				// Déplacement de la balle
				PointF currentPos = new PointF(ball.getX(), ball.getY());
				
				ball.setX((float) (currentPos.x + vectorX));
				ball.setY((float) (currentPos.y + vectorY));
				
				// Libération de la sémaphore
				semaphore.release();				
			}
		};
		
		// Lancement du mouvement sur le thread UI
		activity.runOnUiThread(move);
		
		// Attente de libération de la sémaphore
		semaphore.acquireUninterruptibly();		
	} // moveBall()
	
	public static void moveX(Activity activity, final View element, final float value) {	
		
		// Sémaphore pour bloquer le thread de calcul plus rapide que l'UI
		final Semaphore semaphore = new Semaphore(0);
		
		Runnable move = new Runnable() {
			
			@Override
			public void run() {
				element.setX(value);
				
				// Libération de la sémaphore
				semaphore.release();
			}
		};
		
		// Lancement du mouvement sur le thread UI
		activity.runOnUiThread(move);
		
		// Attente de libération de la sémaphore
		semaphore.acquireUninterruptibly();
	} // moveX()
	
	public static void moveY(Activity activity, final View element, final float value)  {
		
		// Sémaphore pour bloquer le thread de calcul plus rapide que l'UI
		final Semaphore semaphore = new Semaphore(0);
		
		Runnable move = new Runnable() {
			
			@Override
			public void run() {
				element.setY(value);
				
				// Libération de la sémaphore
				semaphore.release();
			}
		};
		
		// Lancement du mouvement sur le thread UI
		activity.runOnUiThread(move);
		
		// Attente de libération de la sémaphore
		semaphore.acquireUninterruptibly();
	} // moveY()
}
