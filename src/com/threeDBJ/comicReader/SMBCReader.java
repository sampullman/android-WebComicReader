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

public class SMBCReader extends Reader {

    String altURL;
    Pattern pImages, pIndices, pMax, pAlt;
    String[] indices = new String[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	String indPat = "(?:.*?href=\"/index.php\\?db=comics&id=([0-9]+).*?)";
	String imgPat = "\"(http://www[.]smbc-comics[.]com/comics/.*?)\"";
	String altPat = "<img src='(http://.*?[.]smbc-comics[.]com/comics/.*?after[.]gif)'>";
	String maxPat = "function jumpToRandom[(][)] [{].*?var num = Math.floor[(]Math.random[(][)].([0-9]*?)[)]";
	this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pIndices = Pattern.compile(indPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pAlt = Pattern.compile(altPat, Pattern.DOTALL | Pattern.UNIX_LINES);
	this.base = "http://www.smbc-comics.com/index.php?db=comics&id=";
	this.max = "http://www.smbc-comics.com/";

	this.title = "SMBC";
	this.storeUrl = "http://smbc.myshopify.com/";

	loadInitial(max);
    }

    public void UISetup() {
	super.UISetup();
	setAltListener(altListener);
    }

    /* Description of comic logic:
       First comic only has a next
       Last comic has a first and a previous
       All other comics have a first, previous, and next */
    public String handleRawPage(Comic c, String page) {
	int nIndices=0;
	Matcher mImages = pImages.matcher(page);
	Matcher mIndices = pIndices.matcher(page);
	String imgUrl;
	try {
	    mImages.find();
	    imgUrl = mImages.group(1);
	    Matcher mAlt = pAlt.matcher(page);
	    if(mAlt.find()) {
		c.setAlt(mAlt.group(1));
	    } else {
		c.setAlt(null);
	    }
	} catch(Exception e) {
	    imgUrl = "http://cdn.shopify.com/s/files/1/0066/2852/products/science_large_grande.jpg?100646";
	}
	while(mIndices.find() && nIndices < indices.length) {
	    indices[nIndices] = mIndices.group(1);
	    nIndices += 1;
	}
	/* First comic */
	if(nIndices == 1) {
	    c.setNextInd(indices[0]);
	} else if(nIndices == 2) {
	    if(haveMax() && indices[1].equals(Integer.toString(Integer.parseInt(maxInd)-2))) {
		c.setNextInd(Integer.toString(Integer.parseInt(indices[1])+2));
	    } else if(!haveMax()) {
		Matcher mMax = pMax.matcher(page);
		mMax.find();
		String temp = mMax.group(1);
		setMaxIndex(temp);
		setMaxNum(temp);
	    } else {
	    }
	    c.setPrevInd(indices[1]);
	} else if(nIndices == 3){
	    c.setPrevInd(indices[1]);
	    c.setNextInd(indices[2]);
	} else {
	    Log.v("smbc", "error - invalid comic");
	}
	return imgUrl;
    }

    protected OnClickListener altListener = new OnClickListener() {
            public void onClick(View v) {
		if(curComic.altData != null) {
		    dispAltImage(curComic.altData, "SMBC Red Button");
		} else {
		    dispAltText("No Red Button Available!", "SMBC Red Button");
		}
            }
        };
}