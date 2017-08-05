package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExplosmReader extends Reader {
    public static String prevPat = "a rel=\"prev\".*?href=\"/comics/([0-9]+)/\"";
    public static String nextPat = "a rel=\"next\".*?href=\"/comics/([0-9]+)/\"";
    public static String imgPat = "src=\"(http://w?w?w?.?explosm.net/db/files/(?!comic-authors).*?)\"";
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
        this.max = "http://explosm.net/comics/";

        this.title = "Explosm";
        this.shortTitle = title;
        this.storeUrl = "http://store.explosm.net/";

        /* Wierd, but true */
        this.firstInd = "15";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher mImages = pImages.matcher(page);
        Matcher mPrev = pPrev.matcher(page);
        Matcher mNext = pNext.matcher(page);

        if(mPrev.find()) {
            c.setPrevInd(mPrev.group(1));
            /* Normal comic */
            if(mNext.find()) {
                c.setNextInd(mNext.group(1));
                /* Last comic */
            } else {
                String temp = Integer.toString(Integer.parseInt(mPrev.group(1)) + 1);
                setMaxIndex(temp);
                setMaxNum(temp);
            }
            /* First comic */
        } else {
            if(mNext.find(1)) {
                c.setNextInd(mNext.group(1));
            } else {
                /* Anomaly, this should not happen */
            }
        }
        return mImages.find() ? mImages.group(1) : null;
    }

}
