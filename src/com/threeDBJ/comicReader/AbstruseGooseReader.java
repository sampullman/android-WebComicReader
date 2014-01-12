package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.graphics.Bitmap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AbstruseGooseReader extends Reader {

    Pattern pImages, pPrev, pNext, pCur;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String prevPat = "First.*?<a href=\"http://abstrusegoose.com/(.*?)\">.*?Previous</a>";
        String nextPat = "Random.*?<a href=\"http://abstrusegoose.com/(.*?)\">Next";
        String imgPat = "<img.*?src=\"(http://abstrusegoose.com/strips/.*?)\".*?(?:(?:title=\"(.*?)\" />)|(?: /></p>)|(?: /></a>))";
        String curPat = "<h1 class=\"storytitle\"><a href=\"http://abstrusegoose.com/(.*?)\">";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pCur = Pattern.compile(curPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://abstrusegoose.com/";
        this.max = "http://abstrusegoose.com/";

        this.title = "Abstruse Goose";
        this.shortTitle = "Abstruse";
        this.storeUrl = "http://www.cafepress.com/abstrusegoose";

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
        boolean success = mImages.find();
        String imgUrl = mImages.group(1);
        if(mImages.groupCount() == 2) {
            c.setAlt(mImages.group(2));
        } else {
            c.setAlt(null);
        }

        if(mPrev.find()) {
            c.setPrevInd(mPrev.group(1));
            /* Normal comic */
            if(mNext.find() && mNext.group(1) != null) {
                c.setNextInd(mNext.group(1));
                /* Last comic */
            } else {
                Matcher mCur = pCur.matcher(page);
                if(mCur.find()) {
                    String temp = mCur.group(1);
                    setMaxIndex(temp);
                    setMaxNum(temp);
                }
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
                dispAltText(getCurComic().altData, "Abstruse Goose Hover Text");
            }
        };

    protected OnClickListener randomListener = new OnClickListener() {
            public void onClick(View v) {
                state.clearComics();
                clearVisible();
                loadInitial(max + "random.php");
            }
        };

}