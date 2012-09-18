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

public class ExplosmReader extends Reader {

    String altUrl;
    Pattern pImages, pPrev, pNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String prevPat = "a rel=\"prev\".*?href=\"/comics/([0-9]+)/\"";
	String nextPat = "a rel=\"next\".*?href=\"/comics/([0-9]+)/\"";
	String imgPat = "src=\"(http://www.explosm.net/db/files/Comics.*?)\"";

	this.pImages = Pattern.compile(imgPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pPrev = Pattern.compile(prevPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.base = "http://explosm.net/comics/";
	this.max = "http://explosm.net/comics/";

	this.title = "Explosm";
	this.storeUrl = "http://store.explosm.net/";

	/* Wierd, but true */
	this.firstInd = "15";

	grabComicCold(max);

    }

    public String handleRawPage(String page) {
	Matcher mImages = pImages.matcher(page);
	Matcher mPrev = pPrev.matcher(page);
	Matcher mNext = pNext.matcher(page);

	if(mPrev.find()) {
	    setPrevIndex(mPrev.group(1));
	    /* Normal comic */
	    if(mNext.find()) {
		setNextIndex(mNext.group(1));
	    /* Last comic */
	    } else {
		String temp = Integer.toString(Integer.parseInt(mPrev.group(1))+1);
		setMaxIndex(temp);
		setMaxNum(temp);
	    }
	/* First comic */
	} else {
	    if(mNext.find(1)) {
		setNextIndex(mNext.group(1));
	    } else {
		/* Anomaly, this should not happen */
	    }
	}
	if(mImages.find()) {
	    String imgUrl = mImages.group(1);
	    return imgUrl;
	} else {
	    return null;
	}
    }

}