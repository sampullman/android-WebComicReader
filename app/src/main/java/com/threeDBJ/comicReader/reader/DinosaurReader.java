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

        String prevPat = "<a title=\"Previous comic\"  rel=\"prev\" href=\"/*?index.php[?]comic=([0-9]+)\">";
        String nextPat = "<a title=\"Next comic\" rel=\"next\" href=\".*?index.php[?]comic=([0-9]+)\">";
        String imgPat = "og:description\" content=\"(.*?)\" />.*?og:image\" content=\"(.*?)\"";

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
        Matcher imageMatcher = pImages.matcher(page);
        Matcher prevMatcher = pPrev.matcher(page);
        Matcher nextMatcher = pNext.matcher(page);

        boolean success = imageMatcher.find();
        if(success) {
            String imgUrl = imageMatcher.group(2);
            c.setAlt(imageMatcher.group(1));
            Timber.d("Dinosaur %s %s", imgUrl, imageMatcher.group(2));

            if (prevMatcher.find()) {
                c.setPrevInd(prevMatcher.group(1));
                if (nextMatcher.find() && nextMatcher.group(1) != null) {
                    c.setNextInd(nextMatcher.group(1));
                } else {
                    String temp = Integer.toString(Integer.parseInt(prevMatcher.group(1)) + 1);
                    setMaxIndex(temp);
                    setMaxNum(temp);
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
