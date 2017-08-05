package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMBCReader extends Reader {
    public static String prevPat = "<!-- Back button--><a href=\"[?]id=([0-9]+).*?class=\"backRollover\">";
    public static String nextPat = "<!-- Next button --><a href=\"[?]id=([0-9]+).*?class=\"nextRollover\">";
    public static String imgPat = "<div id=\"comicimage\">.*?<img src='(http://www[.]smbc-comics[.]com/comics/.*?)'>.*?</div>";
    public static String altPat = "<div id=\"aftercomic\".*?<img src='(http://www[.]smbc-comics[.]com/comics/.*?after[.]gif)'>";
    public static String maxPat = "function jumpToRandom[(][)] [{].*?var num = Math.floor[(]Math.random[(][)].([0-9]*?)[)]";

    String altURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();
        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pAlt = Pattern.compile(altPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.base = "http://www.smbc-comics.com/index.php?db=comics&id=";
        this.max = "http://www.smbc-comics.com/";

        this.title = "SMBC";
        this.shortTitle = title;
        this.storeUrl = "http://smbc.myshopify.com/";
        setAltListener(altListener);
    }

    /* Description of comic logic:
       First comic only has a next
       Last comic has a first and a previous
       All other comics have a first, previous, and next */
    public String handleRawPage(Comic c, String page) {
        Matcher mImages = pImages.matcher(page);
        Matcher mPrev = pPrev.matcher(page);
        Matcher mNext = pNext.matcher(page);
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

        boolean haveNext = mNext.find();
        boolean havePrev = mPrev.find();
        /* First comic */
        if(!havePrev) {
            c.setNextInd(mNext.group(1));
        } else if(!haveNext) {
            if(!haveMax()) {
                Matcher mMax = pMax.matcher(page);
                mMax.find();
                String temp = mMax.group(1);
                setMaxIndex(temp);
                setMaxNum(temp);
            }
            c.setPrevInd(mPrev.group(1));
        } else {
            c.setPrevInd(mPrev.group(1));
            c.setNextInd(mNext.group(1));
        }
        return imgUrl;
    }

    protected OnClickListener altListener = new OnClickListener() {
        public void onClick(View v) {
            if(getCurComic().getAlt() != null) {
                dispAltImage(getCurComic().getAlt(), "SMBC Red Button");
            } else {
                dispAltText("No Red Button Available!", "SMBC Red Button");
            }
        }
    };
}
