package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class QuestionableContentReader extends Reader {

    public static String prevPat = "<li><a href=\"view.php[?]comic=([0-9]+)\">Previous</a></li>";
    public static String nextPat = "<li><a href=\"view.php[?]comic=([0-9]+)\">Next</a></li>";
    public static String imgPat = "<img.*?src=\".*?/comics/([0-9]+)([.][a-zA-Z]+?)\">";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(getMax());
    }

    @Override
    public void UISetup() {
        super.UISetup();
        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://questionablecontent.net/view.php?comic=";
        this.max = "http://questionablecontent.net/index.php";
        this.firstInd = "1";

        this.title = "Questionable Content";
        this.shortTitle = "Questionable";
        this.storeUrl = "http://www.topatoco.com/merchant.mvc?Screen=CTGY&Store_Code=TO&Category_Code=QC";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        boolean success = imageMatcher.find();
        if(success) {
            String imgUrl = "https://questionablecontent.net/comics/" + imageMatcher.group(1) + imageMatcher.group(2);
            Timber.e("Success %s", imgUrl);
            if (prevMatcher.find()) {
                Timber.d("prev: %s", prevMatcher.group(1));
                c.setPrevInd(prevMatcher.group(1));
                if (nextMatcher.find() && nextMatcher.group(1) != null) {
                    c.setNextInd(nextMatcher.group(1));
                } else {
                    setMaxIndex(imageMatcher.group(1));
                    setMaxNum(imageMatcher.group(1));
                }
            } else {
                if (nextMatcher.find(1)) {
                    c.setNextInd(nextMatcher.group(1));
                } else {
                    /* Anomaly, this should not happen */
                }
            }
            return imgUrl;
        }
        return null;
    }
}
