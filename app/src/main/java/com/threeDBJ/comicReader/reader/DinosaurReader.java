package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class DinosaurReader extends Reader {

    Pattern pImages, pPrev, pNext, pMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();

        String prevPat = "<a title=\"Previous comic\"  rel=\"prev\" href=\"http://www.qwantz.com/index.php[?]comic=([0-9]+)\">";
        String nextPat = "<a title=\"Next comic\" rel=\"next\" href=\"http://www.qwantz.com/index.php[?]comic=([0-9]+)\">";
        String imgPat = "valign=\"middle\"><img src=\"(.*?)\" class=\"comic\" title=\"(.*?)\">";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://www.qwantz.com/index.php?mobile=0&comic=";
        this.max = "http://www.qwantz.com/index.php?mobile=0";
        this.firstInd = "1";

        this.title = "Dinosaur Comics";
        this.shortTitle = "Dinosaur";
        this.storeUrl = "http://www.qwantz.com/merchandise.php";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher mImages = pImages.matcher(page);
        Matcher mPrev = pPrev.matcher(page);
        Matcher mNext = pNext.matcher(page);

        boolean success = mImages.find();
        String imgUrl = mImages.group(1);
        Timber.e("Dinosaur %s", imgUrl);
        c.setAlt(mImages.group(2));
        Timber.e("Dinosaur %s", mImages.group(2));

        if(mPrev.find()) {
            c.setPrevInd(mPrev.group(1));
            if(mNext.find() && mNext.group(1) != null) {
                c.setNextInd(mNext.group(1));
            } else {
                String temp = Integer.toString(Integer.parseInt(mPrev.group(1)) + 1);
                setMaxIndex(temp);
                setMaxNum(temp);
            }
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
