package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExplosmReader extends Reader {
    public static String prevPat = "<a href=\"/comics/([0-9]+)/\" class=\"nav-previous";
    public static String nextPat = "<a href=\"/comics/([0-9]+)/\" class=\"nav-next";
    public static String imgPat = "<meta property=\"og:image\" content=\"(.*?)\">";
    String altUrl;

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

        this.base = "http://explosm.net/comics/";
        this.max = "http://explosm.net/comics/latest/";

        this.title = "Explosm";
        this.shortTitle = title;
        this.storeUrl = "http://store.explosm.net/";

        /* Weird, but true */
        this.firstInd = "15";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            /* Normal comic */
            if(nextMatcher.find()) {
                c.setNextInd(nextMatcher.group(1));
                /* Last comic */
            } else {
                String temp = Integer.toString(Integer.parseInt(prevMatcher.group(1)) + 1);
                setMaxIndex(temp);
                setMaxNum(temp);
            }
            /* First comic */
        } else {
            if(nextMatcher.find(1)) {
                c.setNextInd(nextMatcher.group(1));
            } else {
                /* Anomaly, this should not happen */
            }
        }
        if(imageMatcher.find()) {
            String imageUrl = imageMatcher.group(1);

            int questionInd = imageUrl.lastIndexOf("?");
            if (questionInd != -1) {
                imageUrl = imageUrl.substring(0, questionInd);
            }
            return imageUrl;
        }
        return null;
    }

}
