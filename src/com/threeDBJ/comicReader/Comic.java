package com.threeDBJ.comicReader;

import android.graphics.Bitmap;

public class Comic {

    public Bitmap image;
    String url, ind, prevInd, nextInd, altData, imgTitle;
    boolean error=false;

    public Comic() {
    }

    public Comic(String base, String ind) {
        this.url = base + ind;
        this.ind = ind;
    }

    public Comic(CachedComic c) {
        image = c.image;
        url = "";
        ind = c.ind;
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

    public void setInd(String ind) {
        this.ind = ind;
    }

    public Bitmap getComic() {
        return image;
    }

    public void setComic(Bitmap comic) {
        this.image = comic;
    }

    public void setNextInd(String ind) {
        this.nextInd = ind;
    }

    public String getNextInd() {
        return this.nextInd;
    }

    public void setPrevInd(String ind) {
        this.prevInd = ind;
    }

    public String getPrevInd() {
        return this.prevInd;
    }

    public void setAlt(String alt) {
        this.altData = alt;
    }

    public String getAlt() {
        return this.altData;
    }

    public void setImageTitle(String title) {
        this.imgTitle = title;
    }

    public String getImageTitle() {
        return this.imgTitle;
    }

    public boolean getError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

}
