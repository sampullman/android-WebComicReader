package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.graphics.Bitmap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class XKCDReader extends Reader {

    Pattern pImages, pPrev, pNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if(getLastCustomNonConfigurationInstance() == null)
            loadInitial(max);
    }

    public void UISetup() {
        super.UISetup();
        setAltListener(altListener);
    }

    public String handleRawPage(Comic c, String page) {
        Matcher mImages = pImages.matcher(page);
        Matcher mPrev = pPrev.matcher(page);
        Matcher mNext = pNext.matcher(page);

        mImages.find();
        String imgUrl = mImages.group(1);
        c.setAlt(mImages.group(2));

        if(mPrev.find()) {
            c.setPrevInd(mPrev.group(1));
            /* Normal comic */
            if(mNext.find()) {
                c.setNextInd(mNext.group(1));
                /* Last comic */
            } else {
                String temp = Integer.toString(Integer.parseInt(mPrev.group(1))+1);
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
        return imgUrl;
    }

    protected OnClickListener altListener = new OnClickListener() {
            public void onClick(View v) {
                dispAltText(curComic.altData, "XKCD Hover Text");
            }
        };

}