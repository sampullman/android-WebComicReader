package com.threeDBJ.comicReader;

import com.google.ads.*;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

public class comicReader extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);
	Button b;
	b = (Button)findViewById(R.id.smbc);
	b.setOnClickListener(makeComicStarter(SMBCReader.class));
	b = (Button)findViewById(R.id.xkcd);
	b.setOnClickListener(makeComicStarter(XKCDReader.class));
	b = (Button)findViewById(R.id.explosm);
	b.setOnClickListener(makeComicStarter(ExplosmReader.class));
	b = (Button)findViewById(R.id.feelafraid);
	b.setOnClickListener(makeComicStarter(FeelAfraidReader.class));
	b = (Button)findViewById(R.id.phd);
	b.setOnClickListener(makeComicStarter(PHDReader.class));
	b = (Button)findViewById(R.id.buttersafe);
	b.setOnClickListener(makeComicStarter(ButtersafeReader.class));
	b = (Button)findViewById(R.id.cad);
	b.setOnClickListener(makeComicStarter(CtrlAltDeleteReader.class));
	b = (Button)findViewById(R.id.pennyarcade);
	b.setOnClickListener(makeComicStarter(PennyArcadeReader.class));
	b = (Button)findViewById(R.id.abstrusegoose);
	b.setOnClickListener(makeComicStarter(AbstruseGooseReader.class));
	b = (Button)findViewById(R.id.manlyguys);
	b.setOnClickListener(makeComicStarter(ManlyGuysReader.class));
	b = (Button)findViewById(R.id.completelyserious);
	b.setOnClickListener(makeComicStarter(CompletelySeriousReader.class));
	b = (Button)findViewById(R.id.mongreldesigns);
	b.setOnClickListener(makeComicStarter(MongrelDesignsReader.class));
	b = (Button)findViewById(R.id.saved_comics);
	b.setOnClickListener(makeComicStarter(SavedComicMenu.class));

	AdRequest req = new AdRequest ();
        AdView ad = (AdView) findViewById(R.id.ad);
        ad.loadAd (req);
    }

    private OnClickListener makeComicStarter(final Class type) {
	return new OnClickListener() {
	    public void onClick(View v) {
		Intent myIntent = new Intent(v.getContext(), type);
		startActivityForResult(myIntent, 0);
	    }
	};
    }
}
