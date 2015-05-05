package com.alexytorres.enterthevoid;

import com.alexytorres.enterthevoid.model.Preferences;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity {

	// R�cupration des �l�ments du layout activity_settings
	private CompoundButton vibrateOnWinSwitch;
	private Button resetGame;
	private TextView reboundsStats;
	private TextView levelNumber;
	
	// R�cup�ration du singleton de param�tres
	private Preferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		// R�cupration des �l�ments du layout activity_settings
		vibrateOnWinSwitch = (CompoundButton) findViewById(R.id.activity_settings_switch_vibrate);
		resetGame = (Button) findViewById(R.id.activity_settings_button_reset);
		reboundsStats = (TextView) findViewById(R.id.activity_settings_textView_statsbounds);
		levelNumber = (TextView) findViewById(R.id.activity_settings_textView_statslevel);
		
		// R�cup�ration du singleton de param�tres
		prefs = Preferences.getInstance(getApplicationContext());
		int boundsNb =  prefs.getReboundsNumber();
		
		// Param�trage des �l�ments de la vue
		vibrateOnWinSwitch.setChecked(prefs.isVibrateOnWin());			
		reboundsStats.setText(getResources().getQuantityString(R.plurals.rebounds, boundsNb, boundsNb));
		levelNumber.setText(getString(R.string.level, prefs.getCurrentLevel()));
		
		// Switch ou toogle button de vibration
		vibrateOnWinSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.setVibrateOnWin(isChecked);				
			}
		});
		
		// Bouton "red�marer le jeu"
		resetGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				
				// Remet de niveu courant au plus bas
				prefs.setCurrentLevel(1);
				prefs.setReboundsNumber(0);
				
				// Modification de l'affichage
				reboundsStats.setText(getResources().getQuantityString(R.plurals.rebounds, 0, 0));
				levelNumber.setText(getString(R.string.level, 1));
			}
		});
	} // onCreate()

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}// onCreateOptionsMenu

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.menu_action_contact) {
			
			// Appel des application pour envoyer un mail
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"alexy.torresa@gmail.com"});
			intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_body));

			try {
			    startActivity(Intent.createChooser(intent, getString(R.string.sendmail)));
			} 
			catch (android.content.ActivityNotFoundException ex) {
			    Toast.makeText(this, getString(R.string.nomailclient), Toast.LENGTH_SHORT).show();
			}
		}
		else if (id == R.id.menu_action_help) {
			// Affichage de l'activit� d'aide
			Intent openHelp = new Intent(SettingsActivity.this, HelpActivity.class);
			startActivity(openHelp);
		}
		else if (id == R.id.menu_action_about) {

			// Affichage de la boite de dialogue "A propos"
			final Dialog dialog = new Dialog(SettingsActivity.this);
			dialog.setContentView(R.layout.dialog_about_popup_dialog);
			dialog.setTitle(getString(R.string.menu_action_about)); 
			dialog.show();
		}
		
		return super.onOptionsItemSelected(item);
	} // onOptionItemSelected()
}
