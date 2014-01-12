package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.graphics.Bitmap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FeelAfraidReader extends Reader {

    Pattern pImages, pPrev, pNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	String prevPat = "<a href=\"([0-9]+.php)\"><img src=\"nav_02.png\" alt=\"Previous\"></a>";
	String nextPat = "<a href=\"([0-9]+.php)\"><img src=\"nav_04.png\" alt=\"Next\">";
	String imgPat = "<img src=\"(comics/(.*?))\" alt=";

	this.pImages = Pattern.compile(imgPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pPrev = Pattern.compile(prevPat,Pattern.DOTALL | Pattern.UNIX_LINES);
	this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);

	this.base = "http://feelafraidcomic.com/";
	this.max = "http://feelafraidcomic.com/index.php";

	this.title = "FeelAfraid";
    this.shortTitle = title;
	this.storeUrl = "http://feelafraidcomic.com/store/";

	this.firstInd = "1.php";

	loadInitial(max);
    }

    public String getIndFromNum(String num) {
	return num + ".php";
    }

    public String handleRawPage(Comic c, String page) {
	Matcher mImages = pImages.matcher(page);
	Matcher mPrev = pPrev.matcher(page);
	Matcher mNext = pNext.matcher(page);
	if(mPrev.find()) {
	    c.setPrevInd(mPrev.group(1));
	    // TODO -- figure out better solution
	    //if(getCurComic().prevInd.equals("75.php"))
	    //c.setNextInd("77.php");
	    /* Normal comic */
	    if(mNext.find()) {
		c.setNextInd(mNext.group(1));
	    /* Last comic */
	    } else {
		String temp = mPrev.group(1);
		temp = temp.substring(0,temp.length()-4);
		temp = Integer.toString(Integer.parseInt(temp)+1);
		setMaxIndex(temp+".php");
		setMaxNum(temp);
	    }
	/* First comic */
	} else {
	    if(mNext.find(1)) {
		c.setNextInd(mNext.group(1));
	    } else {
		/* Anomaly, this should not happen */
		DebugLog.v("FeelAfraid", "Grevious error");
	    }
	}
	if(mImages.find()) {
	    String imgUrl = this.base + mImages.group(1);
	    String imgTitle = mImages.group(2);
	    int dotInd = imgTitle.indexOf(".");
	    if(dotInd != -1)
		imgTitle = imgTitle.substring(0, dotInd);
	    c.setImageTitle(imgTitle);
	    return imgUrl;
	} else {
	    return null;
	}
    }

}