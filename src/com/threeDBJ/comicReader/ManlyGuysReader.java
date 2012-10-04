package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Button;
import android.util.Log;
import android.graphics.Bitmap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ManlyGuysReader extends Reader {

    Pattern pImages, pPrev, pNext, pMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	String prevPat = "First.*?<a href=\"http://thepunchlineismachismo.com/archives/(.*?)\" class=\"navi navi-prev\" title=\"Previous\">";
	String nextPat = "<td class=\"comic_navi_right\">.*?<a href=\"http://thepunchlineismachismo.com/archives/(.*?)\" class=\"navi navi-next\" title=\"Next\">";
	String imgPat = "<img src=\"(http://thepunchlineismachismo.com/comics/.*?)\" alt=\"(.*?)\".*?/></div>";
	String maxPat = "<h2 class=\"post-title\"><a href=\"http://thepunchlineismachismo.com/archives/([0-9]*?)\">";

	this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.base = "http://thepunchlineismachismo.com/archives/";
	this.max = "http://thepunchlineismachismo.com/";
	this.firstInd = "71";

	this.title = "Manly Guys Doing Manly Things";
	this.storeUrl = "http://www.dreamhost.com/donate.cgi?id=13906";

	Button b = (Button) findViewById(R.id.comic_random);
	LinearLayout nav = (LinearLayout) findViewById(R.id.nav_bar);
        nav.removeView(b);

	grabComicCold(max);
    }

    public void UISetup() {
	super.UISetup();
	setAltListener(altListener);
    }


    public String handleRawPage(String page) {
	Matcher mImages = pImages.matcher(page);
	Matcher mPrev = pPrev.matcher(page);
	Matcher mNext = pNext.matcher(page);

	boolean success = mImages.find();
	String imgUrl = mImages.group(1);

	if(mImages.groupCount() == 2) {
	    setAlt( mImages.group(2));
	} else {
	    setAlt(null);
	}

	if(mPrev.find()) {
	    setPrevIndex(mPrev.group(1));
	    /* Normal comic */
	    if(mNext.find()) {
		setNextIndex(mNext.group(1));
		/* Last comic */
	    } else {
		if(maxInd == null) {
		    Matcher mMax = pMax.matcher(page);
		    mMax.find();
		    setMaxIndex(mMax.group(1));
		}
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

    protected OnClickListener altListener = new OnClickListener() {
            public void onClick(View v) {
		dispAltText(currentComic.altData, "Abstruse Goose Hover Text");
            }
        };


    public void selectComic() {
	showDialog("Select Comic", "Feature currently unavailable for this comic.");
    }

}