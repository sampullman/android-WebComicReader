package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class AbstruseGooseReader extends Reader {

    Pattern pImages, pPrev, pNext, pCur;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        String prevPat = "First.*?<a href=\"http://abstrusegoose.com/(.*?)\">.*?Previous</a>";
        String nextPat = "Random.*?<a href=\"http://abstrusegoose.com/(.*?)\">Next";
        String imgPat = "<img.*?src=\"(http://abstrusegoose.com/strips/.*?)\".*?(?:(?:title=\"(.*?)\" />)|(?: /></p>)|(?: /></a>))";
        String curPat = "<h1 class=\"storytitle\"><a href=\"http://abstrusegoose.com/(.*?)\">";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pCur = Pattern.compile(curPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://abstrusegoose.com/";
        this.max = "http://abstrusegoose.com/";

        this.title = "Abstruse Goose";
        this.shortTitle = "Abstruse";
        this.storeUrl = "http://www.cafepress.com/abstrusegoose";
        setAltListener(altListener);
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);
        boolean success = imageMatcher.find();
        if(!success) {
            return null;
        }
        String imgUrl = imageMatcher.group(1);

        if(imageMatcher.groupCount() == 2) {
            c.setAlt(imageMatcher.group(2));
        } else {
            c.setAlt(null);
        }

        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            /* Normal comic */
            if(nextMatcher.find() && nextMatcher.group(1) != null) {
                c.setNextInd(nextMatcher.group(1));
                /* Last comic */
            } else {
                Matcher mCur = pCur.matcher(page);
                if(mCur.find()) {
                    String temp = mCur.group(1);
                    setMaxIndex(temp);
                    setMaxNum(temp);
                }
            }
            /* First comic */
        } else {
            if(nextMatcher.find(1)) {
                c.setNextInd(nextMatcher.group(1));
            } else {
                /* Anomaly, this should not happen */
                Timber.e("No prev and no next?");
            }
        }
        return imgUrl;
    }

    protected OnClickListener altListener = v -> {
        dispAltText(getCurComic().getAlt(), "Abstruse Goose Hover Text");
    };

    protected OnClickListener randomListener = v -> {
        state.clearComics();
        clearVisible();
        loadInitial(max + "random.php");
    };

}
