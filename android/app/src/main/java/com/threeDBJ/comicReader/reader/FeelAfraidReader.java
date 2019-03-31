package com.threeDBJ.comicReader.reader;

import android.os.Bundle;

import com.threeDBJ.comicReader.Comic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class FeelAfraidReader extends Reader {

    Pattern pImages, pPrev, pNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInitial(max);
    }

    @Override
    public void UISetup() {
        super.UISetup();

        String prevPat = "<a href=\"([0-9]+.php)\"><img src=\"nav_02.png\" alt=\"Previous\"></a>";
        String nextPat = "<a href=\"([0-9]+.php)\"><img src=\"nav_04.png\" alt=\"Next\">";
        String imgPat = "<img src=\"(comics/(.*?))\" alt=";

        this.pImages = Pattern.compile(imgPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pPrev = Pattern.compile(prevPat, Pattern.DOTALL | Pattern.UNIX_LINES);
        this.pNext = Pattern.compile(nextPat, Pattern.DOTALL | Pattern.UNIX_LINES);

        this.base = "http://feelafraidcomic.com/";
        this.max = "http://feelafraidcomic.com/index.php";

        this.title = "FeelAfraid";
        this.shortTitle = title;
        this.storeUrl = "http://feelafraidcomic.com/store/";

        this.firstInd = "1.php";
    }

    public String getIndFromNum(String num) {
        return num + ".php";
    }

    public String handleRawPage(Comic c, String page) {
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);
        if(prevMatcher.find()) {
            c.setPrevInd(prevMatcher.group(1));
            // TODO -- figure out better solution
            //if(getCurComic().prevInd.equals("75.php"))
            //c.setNextInd("77.php");
            /* Normal comic */
            if(nextMatcher.find()) {
                c.setNextInd(nextMatcher.group(1));
                /* Last comic */
            } else {
                String temp = prevMatcher.group(1);
                temp = temp.substring(0, temp.length() - 4);
                temp = Integer.toString(Integer.parseInt(temp) + 1);
                setMaxIndex(temp + ".php");
                setMaxNum(temp);
            }
            /* First comic */
        } else {
            if(nextMatcher.find(1)) {
                c.setNextInd(nextMatcher.group(1));
            } else {
                /* Anomaly, this should not happen */
                Timber.v("FeelAfraid grevious error");
            }
        }
        if(imageMatcher.find()) {
            String imgUrl = this.base + imageMatcher.group(1);
            String imgTitle = imageMatcher.group(2);
            int dotInd = imgTitle.indexOf(".");
            if(dotInd != -1) {
                imgTitle = imgTitle.substring(0, dotInd);
            }
            c.setImageTitle(imgTitle);
            return imgUrl;
        } else {
            return null;
        }
    }

}
