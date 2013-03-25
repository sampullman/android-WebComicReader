package com.threeDBJ.comicReader;

import android.graphics.Bitmap;

public class Comic {

    Bitmap comic;
    String url, ind, prevInd, nextInd, altData, imgTitle;
    boolean error=false;

    public Comic(String base, String ind) {
        this.url = base + ind;
        this.ind = ind;
    }

    public Comic(CachedComic c) {
        comic = c.image;
        url = "";
        ind = c.curInd;
        prevInd = c.prevInd;
        nextInd = c.nextInd;
        altData = c.altData;
        imgTitle = c.imgTitle;
    }

    public String getUrl() {
        return url;
    }

    public String getInd() {
        return ind;
    }

    public void setComic(Bitmap comic) {
        this.comic = comic;
    }

    public Bitmap getComic() {
        return comic;
    }

    public void setNextInd(String ind) {
        this.nextInd = ind;
    }

    public void setPrevInd(String ind) {
        this.prevInd = ind;
    }

    public void setAlt(String alt) {
        this.altData = alt;
    }

    public void setImageTitle(String title) {
        this.imgTitle = title;
    }

    public boolean getError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

}