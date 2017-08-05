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
        Matcher mImages = pImages.matcher(page);
        Matcher mPrev = pPrev.matcher(page);
        Matcher mNext = pNext.matcher(page);

        mImages.find();
        String imgUrl = mImages.group(1);

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
        return imgUrl;
    }

}
