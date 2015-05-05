package com.alexytorres.enterthevoid.model;

public class Direction {
	
	// Variable des vecteurs de direction
	private double x;
	private double y;
	
	public Direction(double vectorX, double vectorY) {
		
		// Initialisation des vecteurs
		this.x = vectorX;
		this.y = vectorY;
	}

	public double getX() { return x; }

	public void setX(double x) { this.x = x; }

	public double getY() { return y; }

	public void setY(double y) { this.y = y; }
	
	public void changeDirection() {
		// Changement de direction
		y = -y;
		x = -x;
	}

	public void changeDirectionX() { x = -x; }
	public void changeDirectionY() { y = -y; }
}
