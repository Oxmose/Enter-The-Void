package com.alexytorres.enterthevoid.model;

import java.util.ArrayList;

public class StructLevel {
	/*
	 * Structure de niveau
	 * Classe tout public utilis� comme structure de donn�es
	 */
	
	// Position du trou
	public int holex;
	public int holey;
	
	// Tableau contenant les coordonn�es des murs + si critique ou non
	public ArrayList<int[]> walls;
	
	// Nombre maximal de rebonds autoris�s
	public int maxbounds;
}
