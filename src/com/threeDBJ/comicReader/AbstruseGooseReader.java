package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import android.graphics.Bitmap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AbstruseGooseReader extends Reader {

    Pattern pImages, pPrev, pNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	String prevPat = "First.*?<a href = \"http://abstrusegoose.com/(.*?)\">.*?Previous</a>";
	String nextPat = "Random.*?<span><a href = \"http://abstrusegoose.com/(.*?)\">Next";
	String imgPat = "<img.*?src=\"(http://abstrusegoose.com/strips/.*?)\".*?(?:(?:title=\"(.*?)\" /></p>)|(?: /></p>)|(?: /></a>))";

	this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.base = "http://abstrusegoose.com/";
	this.max = "http://abstrusegoose.com/";

	this.title = "Abstruse Goose";
	this.storeUrl = "http://www.cafepress.com/abstrusegoose";

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
	    setAlt(mImages.group(2));
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
	return imgUrl;
    }

    protected OnClickListener altListener = new OnClickListener() {
            public void onClick(View v) {
		dispAltText(currentComic.altData, "Abstruse Goose Hover Text");
            }
        };

    protected OnClickListener randomListener = new OnClickListener() {
            public void onClick(View v) {
		currentComic.curInd = "random";
		clearComics();
		grabComicCold(max + "random.php");
            }
        };

}