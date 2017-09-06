package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

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

        loadInitial(getMax());
    }

    @Override
    public void UISetup() {
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
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        boolean success = imageMatcher.find();
        String imgUrl = "http://questionablecontent.net/comics/" + imageMatcher.group(1) + imageMatcher.group(2);
        if(prevMatcher.find()) {
            Timber.d("prev: %s", prevMatcher.group(1));
            c.setPrevInd(prevMatcher.group(1));
            if(nextMatcher.find() && nextMatcher.group(1) != null) {
                c.setNextInd(nextMatcher.group(1));
            } else {
                setMaxIndex(imageMatcher.group(1));
                setMaxNum(imageMatcher.group(1));
            }
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
