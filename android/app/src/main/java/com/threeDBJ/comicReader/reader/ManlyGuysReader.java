package com.threeDBJ.comicReader.reader;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.threeDBJ.comicReader.Comic;
import com.threeDBJ.comicReader.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class ManlyGuysReader extends Reader {

    Pattern pImages, pPrev, pNext, pMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();

        String prevPat = "First.*?<a href=\"http://thepunchlineismachismo.com/archives/comic/(.*?)\" class=\"comic-nav-previous\">.*?Prev</a>";
        String nextPat = "Random.*?<td class=\"comic-nav\"><a href=\"http://thepunchlineismachismo.com/archives/comic/(.*?)\" class=\"comic-nav-next\">Next";
        String imgPat = "<img src=\"(http://thepunchlineismachismo.com/wp-content.*?)\" alt=\"(.*?)\".*?/>.*?</div>";
        String maxPat = "<h2 class=\"post-title\"><a href=\"http://thepunchlineismachismo.com/archives/([0-9]*?)\">";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://thepunchlineismachismo.com/archives/comic/";
        this.max = "http://thepunchlineismachismo.com/";
        this.firstInd = "02222010";

        this.title = "Manly Guys Doing Manly Things";
        this.shortTitle = "Manly";
        this.storeUrl = "http://www.dreamhost.com/donate.cgi?id=13906";

        Button b = (Button) findViewById(R.id.comic_random);
        LinearLayout nav = (LinearLayout) findViewById(R.id.nav_bar);
        nav.removeView(b);

        setAltListener(altListener);
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        boolean success = imageMatcher.find();
        String imgUrl = imageMatcher.group(1);

        if(imageMatcher.groupCount() == 2) {
            c.setAlt(imageMatcher.group(2));
            Timber.d("Manly alt: %s", c.getAlt());
        } else {
            c.setAlt(null);
        }

        if(prevMatcher.find()) {
            Timber.d("previous: %s", prevMatcher.group(1));
            c.setPrevInd(prevMatcher.group(1));
            /* Normal comic */
            if(nextMatcher.find()) {
                Timber.d("next: %s", prevMatcher.group(1));
                c.setNextInd(nextMatcher.group(1));
                /* Last comic */
            } else {
                if(maxInd == null) {
                    Matcher mMax = pMax.matcher(page);
                    mMax.find();
                    setMaxIndex(mMax.group(1));
                }
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

    protected OnClickListener altListener = new OnClickListener() {
        public void onClick(View v) {
            dispAltText(getCurComic().getAlt(), "Manly Guys Hover Text");
        }
    };


    public void selectComic() {
        showDialog("Select Comic", "Feature currently unavailable for this comic.");
    }

}
