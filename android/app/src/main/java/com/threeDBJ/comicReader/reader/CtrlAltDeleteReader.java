package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.threeDBJ.comicReader.Comic;
import com.threeDBJ.comicReader.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.OnClick;

public class CtrlAltDeleteReader extends Reader {

    String altUrl;
    Pattern pImages, pPrev, pNext, pMax;

    // You keep finding shit in the SMBC red button
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();

        String prevPat = "href=\"/cad/([0-9]*?)\" class=\"nav-back\">Back</a>";
        String nextPat = "href=\"/cad/([0-9]*?)\" class=\"nav-next\">Next</a>";
        String imgPat = "src=\"(http://v\\.cdn\\.cad-comic\\.com/comics/(.*?))\" alt=";
        String maxPat = "addthis:url=\"http://www[.]cad-comic[.]com/cad/(.*?)\">";

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://www.cad-comic.com/cad/";
        this.max = "http://www.cad-comic.com/cad/";

        this.firstInd = "20021023";

        this.title = "CtrlAltDelete";
        this.shortTitle = "Ctrl";
        this.storeUrl = "http://www.splitreason.com/cad-comic/";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        imageMatcher.find();
        String imgUrl = imageMatcher.group(1);
        String imgTitle = imageMatcher.group(2);
        int dotInd = imgTitle.indexOf(".");
        if(dotInd != -1) {
            imgTitle = imgTitle.substring(0, dotInd);
        }
        c.setImageTitle(imgTitle);

        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            /* Normal comic */
            if(nextMatcher.find()) {
                if(nextMatcher.group(1).equals("")) {
                    c.setNextInd(maxInd);
                } else {
                    c.setNextInd(nextMatcher.group(1));
                }
                /* Last comic */
            } else {
                Matcher mMax = pMax.matcher(page);
                mMax.find();
                setMaxIndex(mMax.group(1));
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

    public void selectComic() {
        showDialog("Select Comic", "Feature currently unavailable for this comic.");
    }

    @OnClick(R.id.comic_random)
    void randomClick(View v) {
        state.clearComics();
        clearVisible();
        loadInitial(base + "random");
    }

}
