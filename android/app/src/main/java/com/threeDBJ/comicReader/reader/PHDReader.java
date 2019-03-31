package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class PHDReader extends Reader {

    Pattern pImages, pPrev, pNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();

        String prevPat = "<td align=\"left\">.*?archive.php\\?comicid=([0-9]+).*?Previous Comic";
        String nextPat = "<td align=\"right\">.*?archive.php\\?comicid=([0-9]+).*?Next Comic";
        String imgPat = "src=(http://www.phdcomics.com/comics/archive/(.*?)\\.gif)";

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://www.phdcomics.com/comics/archive.php?comicid=";
        this.max = "http://www.phdcomics.com/comics.php";

        this.title = "PHD";
        this.shortTitle = title;
        this.storeUrl = "http://www.phdcomics.com/store/mojostore.php";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        Timber.e("AWWW");
        imageMatcher.find();
        String imgUrl = imageMatcher.group(1);
        Timber.e("HECK");

        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            Timber.e("prev %s %s", prevMatcher.group(1), imgUrl);
            /* Normal comic */
            if(nextMatcher.find()) {
                Timber.e("Next: %s", nextMatcher.group(1));
                c.setNextInd(nextMatcher.group(1));
                /* Last comic */
            } else {
                String temp = Integer.toString(Integer.parseInt(prevMatcher.group(1)) + 1);
                setMaxIndex(temp);
                setMaxNum(temp);
            }
        } else {
            /* First comic */
            Timber.e("No prev (first)");
            if(nextMatcher.find(1)) {
                c.setNextInd(nextMatcher.group(1));
            } else {
                /* Anomaly, this should not happen */
            }
        }
        return imgUrl;
    }

}
