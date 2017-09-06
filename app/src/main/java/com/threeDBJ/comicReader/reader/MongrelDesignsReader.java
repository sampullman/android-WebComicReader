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

public class MongrelDesignsReader extends Reader {

    Pattern pImages, pPrev, pNext, pMax;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();

        String prevPat = "First Comic.*?href=\"(?:http://.*?/)?http://webcomic.mongreldesigns.com/(.*?)\">Previous Comic</a>";
        String nextPat = "Previous Comic.*?href=\"http://webcomic.mongreldesigns.com/(.*?)\">Next Comic</a>";
        String maxPat = "reddit_url='http://webcomic.mongreldesigns.com/(.*?)'";
        String imgPat = "<a href=\"(http://..bp.blogspot.com/.*?)\".*?(?:(?:title=\"(.*?)\")>|(?:>))<img";

        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pMax = Pattern.compile(maxPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://webcomic.mongreldesigns.com/";
        this.max = "http://webcomic.mongreldesigns.com/";
        this.firstInd = "2010/01/webcomic-01-mua-dib-mouse-what-hops.html";

        this.title = "Mongrel Designs";
        this.shortTitle = "Mongrel";
        this.storeUrl = "http://webcomic.mongreldesigns.com/p/support.html";

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
        } else {
            c.setAlt(null);
        }
        Matcher mMax = pMax.matcher(page);
        mMax.find();
        String title = mMax.group(1);
        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            /* Normal comic */
            if(nextMatcher.find()) {
                /* Last comic -- Will have to change if comic is not started at max */
                if(nextMatcher.group(1).equals("")) {
                    setMaxIndex(title);
                } else {
                    c.setNextInd(nextMatcher.group(1));
                }
            } else {

            }
            /* First comic */
        } else {
            if(nextMatcher.find(1)) {
                c.setNextInd(nextMatcher.group(1));
            } else {
                /* Anomaly, this should not happen */
            }
        }
        int ind = title.lastIndexOf("/");
        if(ind != -1) {
            c.setImageTitle(title.substring(ind, title.length() - 1));
        } else {
            c.setImageTitle(title);
        }
        return imgUrl;
    }

    protected OnClickListener altListener = new OnClickListener() {
        public void onClick(View v) {
            dispAltText(getCurComic().getAlt(), "Mongrel Designs Hover Text");
        }
    };

    protected OnClickListener storeListener = new OnClickListener() {
        public void onClick(View v) {
            dispAltText("There is virtually no way to buy anything from this author, but if you " +
                            "would like to support him you can promote his comics. Some other support " +
                            "suggestions can be found here: http://webcomic.mongreldesigns.com/p/support.html",
                    "Mongrel Designs Support");
        }
    };

}
