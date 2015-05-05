package com.alexytorres.enterthevoid.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
	// Instance du singleton
	private static Preferences instance = null;
	
	// Paramètre de l'application
	private boolean vibrateOnWin;
	private int currentLevel;
	private int reboundsNumber;

	// Sharedpreferences et editeur
	private SharedPreferences reader;
	private SharedPreferences.Editor writer;
	
	private Preferences(Context context) {
		
		//Récupération des sharedpreferences et de l'éditeur
		reader = context.getSharedPreferences("com.alexytorres.enterthevoid", Context.MODE_PRIVATE);
        writer = reader.edit();
        
        // Récupération des valeurs stockées
        vibrateOnWin = reader.getBoolean("vibrateOnWin", true);
        currentLevel = reader.getInt("level", 1);
        reboundsNumber = reader.getInt("rebounds", 0);
	}

	public boolean isVibrateOnWin() {
		return vibrateOnWin;
	}

	public void setVibrateOnWin(boolean vibrateOnWin) {
		this.vibrateOnWin = vibrateOnWin;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public int getReboundsNumber() {
		return reboundsNumber;
	}

	public void setReboundsNumber(int reboundsNumber) {
		this.reboundsNumber = reboundsNumber;
	}
	
	public void savePreferences() {
		
		// Sauvegardes des valeurs de paramètres
		writer.putBoolean("vibrateOnWin", vibrateOnWin);
		writer.putInt("level", currentLevel);
		writer.putInt("rebounds", reboundsNumber);
		
		writer.commit();
	}

	public static Preferences getInstance(Context context) {
		
		// Gestion singleton
		if (instance == null) {
			instance = new Preferences(context);
		}
		
		return instance;
	}
}
