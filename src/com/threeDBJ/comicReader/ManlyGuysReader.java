package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Button;
import android.graphics.Bitmap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ManlyGuysReader extends Reader {

    Pattern pImages, pPrev, pNext, pMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String prevPat = "First.*?<a href=\"http://thepunchlineismachismo.com/archives/comic/(.*?)\" class=\"comic-nav-previous\">.*?Prev</a>";
        String nextPat = "Random.*?<td class=\"comic-nav\"><a href=\"http://thepunchlineismachismo.com/archives/comic/(.*?)\" class=\"comic-nav-next\">Next";
        String imgPat = "<img src=\"(http://thepunchlineismachismo.com/wp-content.*?)\" alt=\"(.*?)\".*?/>.*?</div>";
        String maxPat = "<h2 class=\"post-title\"><a href=\"http://thepunchlineismachismo.com/archives/([0-9]*?)\">";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://thepunchlineismachismo.com/archives/comic/";
        this.max = "http://thepunchlineismachismo.com/";
        this.firstInd = "02222010";

        this.title = "Manly Guys Doing Manly Things";
        this.shortTitle = "Manly";
        this.storeUrl = "http://www.dreamhost.com/donate.cgi?id=13906";

        Button b = (Button) findViewById(R.id.comic_random);
        LinearLayout nav = (LinearLayout) findViewById(R.id.nav_bar);
        nav.removeView(b);

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
            c.setAlt( mImages.group(2));
            DebugLog.e("comic", c.altData+"");
        } else {
            c.setAlt(null);
        }

        if(mPrev.find()) {
            DebugLog.e("comic", "previous: "+mPrev.group(1));
            c.setPrevInd(mPrev.group(1));
            /* Normal comic */
            if(mNext.find()) {
            DebugLog.e("comic", "next: "+mPrev.group(1));
                c.setNextInd(mNext.group(1));
                /* Last comic */
            } else {
                if(maxInd == null) {
                    Matcher mMax = pMax.matcher(page);
                    mMax.find();
                    setMaxIndex(mMax.group(1));
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
                dispAltText(getCurComic().altData, "Manly Guys Hover Text");
            }
        };


    public void selectComic() {
        showDialog("Select Comic", "Feature currently unavailable for this comic.");
    }

}