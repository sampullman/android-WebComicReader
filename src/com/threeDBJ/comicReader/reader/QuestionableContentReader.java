package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.threeDBJ.comicReader.Comic;
import com.threeDBJ.comicReader.DebugLog;
import com.threeDBJ.comicReader.R;

public class QuestionableContentReader extends Reader {
    public static String prevPat = "<a href=\"view.php[?]comic=([0-9]+)\">Previous</a>";
    public static String nextPat = "<a href=\"view.php[?]comic=([0-9]+)\">Next</a>";
    public static String imgPat = "<img id=\"strip\" src=.*?/comics/([0-9]+)([.][a-zA-Z]+?)\">";
    public static String comicBase = "http://questionablecontent.net/view.php?comic=";
    public static String comicMax = "http://questionablecontent.net/index.php";
    public static String comicFirstInd = "1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        loadInitial(getMax());
    }

    public void init() {
        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.title = "Questionable Content";
        this.shortTitle = "Questionable";
        this.storeUrl = "http://www.topatoco.com/merchant.mvc?Screen=CTGY&Store_Code=TO&Category_Code=QC";
    }

    public String getBase() {
        return comicBase;
    }

    public String getMax() {
        return comicMax;
    }

    public String getFirstInd() {
        return comicFirstInd;
    }

    public String handleRawPage(Comic c, String page) {
        Matcher mImages = pImages.matcher(page);
        Matcher mPrev = pPrev.matcher(page);
        Matcher mNext = pNext.matcher(page);

        boolean success = mImages.find();
        String imgUrl = "http://questionablecontent.net/comics/"+mImages.group(1)+mImages.group(2);
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
