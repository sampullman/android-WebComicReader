package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XKCDReader extends Reader {

    Pattern pImages, pPrev, pNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    public void UISetup() {
        super.UISetup();

        String prevPat = "href=\"/([0-9]+)/\" accesskey=\"p\">";
        String nextPat = "href=\"/([0-9]+)/\" accesskey=\"n\">";
        String imgPat = "\"(http://imgs.xkcd.com/comics/.*?)\" title=\"(.*?)\"";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://xkcd.com/";
        this.max = "http://xkcd.com/";

        this.title = "XKCD";
        this.shortTitle = title;
        this.storeUrl = "http://store.xkcd.com/";
        setAltListener(altListener);
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        imageMatcher.find();
        String imgUrl = imageMatcher.group(1);
        c.setAlt(imageMatcher.group(2));

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
        return imgUrl;
    }

    protected OnClickListener altListener = new OnClickListener() {
        public void onClick(View v) {
            dispAltText(getCurComic().getAlt(), "XKCD Hover Text");
        }
    };

}
