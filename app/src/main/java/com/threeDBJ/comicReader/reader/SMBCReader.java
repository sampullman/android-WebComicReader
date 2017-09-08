package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class SMBCReader extends Reader {

    public static String prevPat = "rel=\"start\"></a><a href=\"http://www.smbc-comics.com/comic/(.*?)\" class=\"prev\" rel=\"prev\"></a>";
    public static String nextPat = "rel=\"rss\"></a><a href=\"http://www.smbc-comics.com/comic/(.*?)\" class=\"next\" rel=\"next\"></a>";
    public static String imgPat = "<meta property=\"og:image\" content=\"http://www.smbc-comics.com/comics/(.*?)\" />";
    public static String altPat = "<div id=\"aftercomic\".*?<img src='//smbc-comics.com/comics/(.*?)'>";
    public static String maxPat = "<input id=\"permalinktext\" type=\"text\" value=\"http://smbc-comics.com/comic/(.*?)\" />";

    String altURL, imageBase;

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
        this.base = "http://www.smbc-comics.com/comic/";
        this.max = "http://www.smbc-comics.com/";
        this.imageBase = "http://www.smbc-comics.com/comics/";

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
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);
        String imgUrl;
        try {
            imageMatcher.find();
            imgUrl = imageBase + imageMatcher.group(1);
            Timber.d("IMAGE %s", imgUrl);
            Matcher mAlt = pAlt.matcher(page);
            if(mAlt.find()) {
                c.setAlt(imageBase + mAlt.group(1));
            } else {
                c.setAlt(null);
            }
        } catch(Exception e) {
            imgUrl = "http://cdn.shopify.com/s/files/1/0066/2852/products/science_large_grande.jpg?100646";
        }
        boolean haveNext = nextMatcher.find();
        boolean havePrev = prevMatcher.find();
        /* First comic */
        if(!havePrev) {
            c.setNextInd(nextMatcher.group(1));
        } else if(!haveNext) {
            if(!haveMax()) {
                Matcher mMax = pMax.matcher(page);
                mMax.find();
                String temp = mMax.group(1);
                setMaxIndex(temp);
            }
            Timber.d("Test3 %s", prevMatcher.group(1));
            c.setPrevInd(prevMatcher.group(1));
        } else {
            Timber.d("Test6");
            c.setPrevInd(prevMatcher.group(1));
            Timber.d("Test7");
            c.setNextInd(nextMatcher.group(1));
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
