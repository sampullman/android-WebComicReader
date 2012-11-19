package com.threeDBJ.comicReader;

import com.google.ads.*;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class SavedComicMenu extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);

	TextView t = (TextView) findViewById(R.id.title);
	t.setBackgroundResource(R.drawable.saved_reader_title);

	Button b;
	b = (Button)findViewById(R.id.saved_comics);
	b.setVisibility(View.GONE);
	b = (Button)findViewById(R.id.smbc);
	b.setOnClickListener(makeSavedStarter("SMBC", 0));
	b = (Button)findViewById(R.id.xkcd);
	b.setOnClickListener(makeSavedStarter("XKCD", 1));
	b = (Button)findViewById(R.id.explosm);
	b.setOnClickListener(makeSavedStarter("Explosm", 2));
	b = (Button)findViewById(R.id.feelafraid);
	b.setOnClickListener(makeSavedStarter("FeelAfraid", 3));
	b = (Button)findViewById(R.id.phd);
	b.setOnClickListener(makeSavedStarter("PHD", 4));
	b = (Button)findViewById(R.id.buttersafe);
	b.setOnClickListener(makeSavedStarter("Buttersafe", 5));
	b = (Button)findViewById(R.id.cad);
	b.setOnClickListener(makeSavedStarter("CtrlAltDelete", 6));
	b = (Button)findViewById(R.id.pennyarcade);
	b.setOnClickListener(makeSavedStarter("PennyArcade", 7));
	b = (Button)findViewById(R.id.abstrusegoose);
	b.setOnClickListener(makeSavedStarter("Abstruse Goose", 8));
	b = (Button)findViewById(R.id.manlyguys);
	b.setOnClickListener(makeSavedStarter("Manly Guys Doing Manly Things", 9));
	b = (Button)findViewById(R.id.completelyserious);
	b.setOnClickListener(makeSavedStarter("Completely Serious", 10));
	b = (Button)findViewById(R.id.mongreldesigns);
	b.setOnClickListener(makeSavedStarter("Mongrel Designs", 11));
    }

    private OnClickListener makeSavedStarter(final String comic, final int index) {
	return new OnClickListener() {
	    public void onClick(View v) {
		Intent myIntent = new Intent(v.getContext(), SavedComicReader.class);
		myIntent.putExtra("comic", comic);
		myIntent.putExtra("index", index);
		startActivityForResult(myIntent, 1);
	    }
	};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if(resultCode == 1) {
	    showDialog("Error", "No comics to show.");
	} else if(resultCode == 2) {
	    showDialog("Warning", "SD card not readable or could not be found.");
	}

    }

    public void showDialog(String title, String text) {
	final Dialog dialog = new Dialog(this);
	dialog.setContentView(R.layout.text_alt);
	dialog.setTitle(title);
	TextView t = (TextView) dialog.findViewById(R.id.alt_text);
	t.setText(text);
	dialog.setCancelable(true);
	Button done = (Button) dialog.findViewById(R.id.alt_done);
	done.setOnClickListener(makeDialogCancelListener(dialog));
	dialog.show();
    }

    // REUSED CODE! Original is in reader, move to Application later
    public OnClickListener makeDialogCancelListener(final Dialog d) {
	return new OnClickListener() {
	    public void onClick(View v) {
		d.dismiss();
	    }
	};
    }

}