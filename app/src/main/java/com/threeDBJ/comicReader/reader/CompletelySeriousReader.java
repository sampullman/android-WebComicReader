package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompletelySeriousReader extends Reader {

    Pattern pImages, pPrev, pNext, pMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();
        String prevPat = "First</a>.*?<a href=\"http://completelyseriouscomics.com/.p=(.*?)\" class=\"navi navi-prev\"";
        String nextPat = "<td class=\"comic_navi_right\">.*?<a href=\"http://completelyseriouscomics.com/.p=(.*?)\" class=\"navi navi-next\"";
        String imgPat = "<img src=\"(http://completelyseriouscomics.com/comics/.*?)\" alt=\"(.*?)\"";
        // Comic title could be .*? group
        String maxPat = "<h2 class=\"post-title\"><a href=\"http://completelyseriouscomics.com/.p=([0-9]+)\">.*?</a></h2>";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://completelyseriouscomics.com/?p=";
        this.max = "http://completelyseriouscomics.com/";
        this.firstInd = "6";

        this.title = "Completely Serious";
        this.shortTitle = "Completely";
        this.storeUrl = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BAT2CLMHSCR36&lc=CA&currency_code=CAD&bn=PP-DonationsBF:btn_donateCC_LG.gif:NonHosted";

        setAltListener(altListener);
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        boolean success = imageMatcher.find();
        String imgUrl = imageMatcher.group(1);
        if(imageMatcher.groupCount() == 2) {
            c.setAlt(imageMatcher.group(2));
        } else {
            c.setAlt(null);
        }

        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            /* Normal comic */
            if(nextMatcher.find()) {
                c.setNextInd(nextMatcher.group(1));
                /* Last comic */
            } else {
                if(!haveMax()) {
                    Matcher mMax = pMax.matcher(page);
                    if(mMax.find()) {
                        String maxInd = mMax.group(1);
                        setMaxIndex(maxInd);
                        setMaxNum(maxInd);
                    }
                }
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
            dispAltText(getCurComic().getAlt(), "Completely Serious Hover Text");
        }
    };

    protected OnClickListener randomListener = new OnClickListener() {
        public void onClick(View v) {
            state.clearComics();
            clearVisible();
            loadInitial(max + "/?randomcomic&nocache=1");
        }
    };

}
