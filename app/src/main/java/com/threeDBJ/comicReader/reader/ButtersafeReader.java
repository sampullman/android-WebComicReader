package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;

import com.threeDBJ.comicReader.Comic;
import com.threeDBJ.comicReader.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.OnClick;

public class ButtersafeReader extends Reader {

    String altUrl, randComic;
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
        String prevPat = "<div id=\"headernav\">.*?<a href=\"http://buttersafe\\.com/(.*?/)\" rel=\"prev\">&laquo;</a>";
        String nextPat = "<link rel='next' title='.*?' href='http://buttersafe\\.com/(.*?)' />";
        String imgPat = "<img src=\"(http://buttersafe.com/comics/(.*?)\\.jpg)\"";
        String maxPat = "<h2 class=\"index-title\">.*?<a href=\"http://buttersafe[.]com/(.*?)\" rel=\"bookmark\"";

        this.pImages = Pattern.compile(imgPat);
        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://buttersafe.com/";
        this.max = "http://buttersafe.com";

        this.title = "Buttersafe";
        this.shortTitle = title;
        this.storeUrl = "http://buttersafe.com/store/";

        this.firstInd = "2007/04/03/breakfast-sad-turtle/";
        this.randComic = "http://buttersafe.com/?randomcomic";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        imageMatcher.find();
        String imgUrl = imageMatcher.group(1);
        c.setImageTitle(imageMatcher.group(2));

        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            /* Normal comic */
            if(nextMatcher.find()) {
                c.setNextInd(nextMatcher.group(1));
                /* Last comic */
            } else if(!haveMax()) {
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
    public void randomClick(View v) {
        state.clearComics();
        clearVisible();
        loadInitial(randComic);
    }

}
