package com.alexytorres.enterthevoid.model;

import java.util.ArrayList;

public class StructLevel {
	/*
	 * Structure de niveau
	 * Classe tout public utilisé comme structure de données
	 */
	
	// Position du trou
	public int holex;
	public int holey;
	
	// Tableau contenant les coordonnées des murs + si critique ou non
	public ArrayList<int[]> walls;
	
	// Nombre maximal de rebonds autorisés
	public int maxbounds;
}
