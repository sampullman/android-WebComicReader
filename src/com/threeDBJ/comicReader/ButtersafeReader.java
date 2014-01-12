package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.graphics.Bitmap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ButtersafeReader extends Reader {

    String altUrl, randComic;
    Pattern pImages, pPrev, pNext, pMax;

    // You keep finding shit in the SMBC red button
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String prevPat = "<div id=\"headernav\">.*?<a href=\"http://buttersafe\\.com/(.*?/)\" rel=\"prev\">&laquo;</a>";
	String nextPat = "<link rel='next' title='.*?' href='http://buttersafe\\.com/(.*?)' />";
	String imgPat = "<img src=\"(http://buttersafe.com/comics/(.*?)\\.jpg)\"";
	String maxPat = "<h2 class=\"index-title\">.*?<a href=\"http://buttersafe[.]com/(.*?)\" rel=\"bookmark\"";

	this.pImages = Pattern.compile(imgPat);
	this.pPrev = Pattern.compile(prevPat);
	this.pNext = Pattern.compile(nextPat);
	this.pMax = Pattern.compile(maxPat);

	this.base = "http://buttersafe.com/";
	this.max = "http://buttersafe.com";

	this.title = "Buttersafe";
    this.shortTitle = title;
	this.storeUrl = "http://buttersafe.com/store/";

	this.firstInd = "2007/04/03/breakfast-sad-turtle/";
	this.randComic = "http://www.ohnorobot.com/random.pl?comic=1307";

	Button b = (Button) findViewById(R.id.comic_random);
	b.setOnClickListener(randomListener);

	loadInitial(max);

    }

    public String handleRawPage(Comic c, String page) {
	Matcher mImages = pImages.matcher(page);
	Matcher mPrev = pPrev.matcher(page);
	Matcher mNext = pNext.matcher(page);

	mImages.find();
	String imgUrl = mImages.group(1);
	c.setImageTitle(mImages.group(2));

	if(mPrev.find()) {
	    c.setPrevInd(mPrev.group(1));
	    /* Normal comic */
	    if(mNext.find()) {
		c.setNextInd(mNext.group(1));
	    /* Last comic */
	    } else if(!haveMax()) {
		Matcher mMax = pMax.matcher(page);
		mMax.find();
		setMaxIndex(mMax.group(1));
	    }
	/* First comic */
	} else {
	    if(mNext.find(1)) {
		c.setNextInd(mNext.group(1));
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
		state.clearComics();
		clearVisible();
		loadInitial(randComic);
            }
        };

}