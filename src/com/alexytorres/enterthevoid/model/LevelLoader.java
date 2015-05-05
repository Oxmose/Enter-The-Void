package com.alexytorres.enterthevoid.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.alexytorres.enterthevoid.R;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.widget.Toast;

public class LevelLoader {
	
	public static StructLevel LoadLevel(int levelNumber, Context context) {
		
		// Création de la structure de donnée de niveau et des murs
		StructLevel level = new StructLevel();
		level.walls = new ArrayList<int[]>();
		
		try {
			
			// Ouverture du flux XML
			InputStream xmlStream = context.getAssets().open("level" + levelNumber + ".xml");
			
			// Création du parser XML
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(xmlStream, null);
			
			// Evenement parser
			int event = parser.getEventType();

			while (event != XmlPullParser.END_DOCUMENT)	{
				
				
				String name = parser.getName();
				
				// Analyse des tags
				switch (event) {
			   
			      	case XmlPullParser.START_TAG:
			      		break;
			      
			      	case XmlPullParser.END_TAG:
			    	  
			      		// Récupération des informations
			      		if(name.equals("holex")) {			        	  
			      			level.holex = convertPx(Integer.parseInt(parser.getAttributeValue(null, "value")), context);
			        	  
			      		}	 
			      		else if(name.equals("holey")) {
			        	  level.holey = convertPx(Integer.parseInt(parser.getAttributeValue(null, "value")), context);
			        	  
			      		} 
			      		else if(name.equals("wall")) {
			      			int[] wall = new int[5];
			      			wall[0] = convertPx(Integer.parseInt(parser.getAttributeValue(null, "valuex")), context);
			      			wall[1] = convertPx(Integer.parseInt(parser.getAttributeValue(null, "valuey")), context);
			      			wall[2] = convertPx(Integer.parseInt(parser.getAttributeValue(null, "width")), context);
			        	  	wall[3] = convertPx(Integer.parseInt(parser.getAttributeValue(null, "height")), context);
			        	  	wall[4] = Integer.parseInt(parser.getAttributeValue(null, "critic"));
			        	  	level.walls.add(wall);			        	  
			        	  
			      		} 
			      		else if(name.equals("options")) {
			      			level.maxbounds = Integer.parseInt(parser.getAttributeValue(null, "maxbounds"));
			      		}
			          
			          break;
				}	
				// Passage au tag suivant
				event = parser.next(); 					
			}  
			
			return level;	    
		} 
		catch (XmlPullParserException e) {
			Toast.makeText(context, context.getString(R.string.errorlevelload) + e.getMessage(), Toast.LENGTH_LONG).show();
			return null;
			
		} 
		catch (Exception e) {
			Toast.makeText(context, context.getString(R.string.errorlevelload) + e.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}
	}

	public static boolean exists(int level, Context context) {
		
		// Test d'ouverture du niveau pour vérifier son existance
		try {
			context.getAssets().open("level" + level + ".xml");
			return true;
			
		} 
		catch (IOException e) {
			return false;
		}
	}
	
	public static int convertPx(int toConv, Context context) {
		
		// Convertion pour toutes les tailles d'écran (fonctionne très vaguement)
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int densityDpi = (int)(metrics.density * 160f);
		
		// 480 en constante car les niveaux sont designer sur un ecran 480dpi
		return (toConv * densityDpi) / 480;
	}
}
