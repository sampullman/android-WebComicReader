package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CtrlAltDeleteReader extends Reader {

    String altUrl;
    Pattern pImages, pPrev, pNext;

    // You keep finding shit in the SMBC red button
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String prevPat = "href=\"/cad/([0-9]+?)\" class=\"nav-back\">Back</a>";
	String nextPat = "href=\"/cad/([0-9]+?)\" class=\"nav-next\">Next</a>";
	String imgPat = "src=\"(http://v\\.cdn\\.cad-comic\\.com/comics/(.*?))\" alt=";

	this.pImages = Pattern.compile(imgPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pPrev = Pattern.compile(prevPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.base = "http://www.cad-comic.com/cad/";
	this.max = "http://www.cad-comic.com/cad/";

	this.firstInd = "20021023";

	this.title = "CtrlAltDelete";
	this.storeUrl = "http://www.splitreason.com/cad-comic/";

	Button b = (Button) findViewById(R.id.comic_random);
	b.setOnClickListener(randomListener);

	grabComicCold(max);
    }

    public String handleRawPage(String page) {
	Matcher mImages = pImages.matcher(page);
	Matcher mPrev = pPrev.matcher(page);
	Matcher mNext = pNext.matcher(page);

	mImages.find();
	String imgUrl = mImages.group(1);
	String imgTitle = mImages.group(2);
	int dotInd = imgTitle.indexOf(".");
	if(dotInd != -1)
	    imgTitle = imgTitle.substring(0, dotInd);
	setImageTitle(imgTitle);

	if(mPrev.find()) {
	    setPrevIndex(mPrev.group(1));
	    /* Normal comic */
	    if(mNext.find()) {
		setNextIndex(mNext.group(1));
	    /* Last comic */
	    } else {
		setMaxIndex("");
	    }
	/* First comic */
	} else {
	    if(mNext.find(1)) {
		setNextIndex(mNext.group(1));
	    } else {
		/* Anomaly, this should not happen */
	    }
	}
	return imgUrl;
    }

    public void selectComic() {
	showDialog("Select Comic", "Feature currently unavailable for this comic.");
    }

    protected OnClickListener randomListener = new OnClickListener() {
            public void onClick(View v) {
		currentComic.curInd = "random";
		grabRandomComic(base + "random");
            }
        };

}