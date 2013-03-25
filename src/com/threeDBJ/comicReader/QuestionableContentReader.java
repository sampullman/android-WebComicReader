package com.threeDBJ.comicReader;

import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class QuestionableContentReader extends Reader {

    Pattern pImages, pPrev, pNext, pMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String prevPat = "<a href=\"view.php[?]comic=([0-9]+)\">Previous</a>";
        String nextPat = "<a href=\"view.php[?]comic=([0-9]+)\">Next</a>";
        String imgPat = "<img id=\"strip\" src=.*?/comics/([0-9]+).png\">";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://questionablecontent.net/view.php?comic=";
        this.max = "http://questionablecontent.net/index.php";
        this.firstInd = "1";

        this.title = "Questionable Content";
        this.shortTitle = "Questionable";

        loadInitial(max);
    }

    public String handleRawPage(Comic c, String page) {
        Matcher mImages = pImages.matcher(page);
        Matcher mPrev = pPrev.matcher(page);
        Matcher mNext = pNext.matcher(page);

        boolean success = mImages.find();
        String imgUrl = "http://questionablecontent.net/comics/"+mImages.group(1)+".png";
        if(mPrev.find()) {
            DebugLog.e("comic", "prev: "+mPrev.group(1));
            c.setPrevInd(mPrev.group(1));
            if(mNext.find() && mNext.group(1) != null) {
                c.setNextInd(mNext.group(1));
            } else {
                setMaxIndex(mImages.group(1));
                setMaxNum(mImages.group(1));
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

