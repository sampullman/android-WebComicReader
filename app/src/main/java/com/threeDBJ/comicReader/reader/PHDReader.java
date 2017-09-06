package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String prevPat = "archive.php\\?comicid=([0-9]+).*?prev_button.gif";
        String nextPat = "<td align=\"left\" valign=\"top\"><a href=archive.php\\?comicid=([0-9]+).*?next_button.gif";
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

        imageMatcher.find();
        String imgUrl = imageMatcher.group(1);

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

}
