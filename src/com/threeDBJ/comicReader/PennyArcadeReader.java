package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PennyArcadeReader extends Reader {

    String altUrl;
    Pattern pImages, pPrev, pNext, pMax;

    // You keep finding shit in the SMBC red button
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String prevPat = "<a class=\"btnPrev btn\" href=\"http://penny-arcade.com/comic/(.*?)\" title=\"Previous\">Previous</a>";
	String nextPat = "<a class=\"btnNext btn\" href=\"http://penny-arcade.com/comic/(.*?)\" title=\"Next\">Next</a>";
	String imgPat = "<img src=\"(http://art.penny-arcade.com/photos/(.*?))\" alt=\"";
	String maxPat = "<input type=\"hidden\" name=\"return_to\" value=\"http://penny-arcade[.]com/comic/(.*?)#added\" />";

	this.pImages = Pattern.compile(imgPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pPrev = Pattern.compile(prevPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.base = "http://penny-arcade.com/comic/";
	this.max = "http://penny-arcade.com/comic/";

	this.title = "PennyArcade";
	this.storeUrl = "http://store.penny-arcade.com/";

	this.firstInd = "1998/11/18";
	Button b = (Button) findViewById(R.id.comic_random);
	LinearLayout nav = (LinearLayout) findViewById(R.id.nav_bar);
        nav.removeView(b);

	loadInitial(max);

    }

    public String handleRawPage(Comic c, String page) {
	Matcher mImages = pImages.matcher(page);
	Matcher mPrev = pPrev.matcher(page);
	Matcher mNext = pNext.matcher(page);

	mImages.find();
	String imgUrl = mImages.group(1);
	String imgTitle = mImages.group(2);
	int dotInd = imgTitle.indexOf(".");
	int slashInd = imgTitle.lastIndexOf("/");
	if(dotInd != -1) {
	    imgTitle = imgTitle.substring(slashInd+1, dotInd);
	}
	c.setImageTitle(imgTitle);
	if(mPrev.find()) {
	    c.setPrevInd(mPrev.group(1));
	    /* Normal comic */
	    if(mNext.find()) {
		if(mNext.group(1).equals("") && !haveMax()) {
		    Matcher mMax = pMax.matcher(page);
		    mMax.find();
		    setMaxIndex(mMax.group(1));
		    Log.e("wat", "found max");
		} else {
		    Log.e("wat", "not the max");
		    c.setNextInd(mNext.group(1));
		}
	    }
	    /* No easy way to detect last comic */
	/* First comic */
	} else {
	    if(mNext.find(1)) {
		c.setNextInd(mNext.group(1));
	    } else {
		/* Anomaly, this should not happen */
	    }
	}
	Log.e("ahmmmh", "max: "+maxInd);
	return imgUrl;
    }

    public void selectComic() {
	showDialog("Select Comic", "Feature currently unavailable for this comic.");
    }

    protected OnClickListener randomListener = new OnClickListener() {
            public void onClick(View v) {
		clearComics();
		clearVisible();
		loadInitial(base + "random");
            }
        };

}