package com.threeDBJ.comicReader;

import android.graphics.Bitmap;

public class CachedComic extends Comic {
    boolean loaded = false;

    public CachedComic() {
    }

    public CachedComic(Bitmap image, String prevInd, String curInd, String nextInd) {
        set(image, prevInd, curInd, nextInd, null, null);
    }

    public void set(Bitmap image, String prevInd, String curInd, String nextInd, String altData, String imgTitle) {
        this.image = image;
        this.prevInd = prevInd;
        this.ind = curInd;
        this.nextInd = nextInd;
        this.altData = altData;
        this.imgTitle = imgTitle;
    }

    public void load(Comic c) {
        set(c.image, c.prevInd, c.ind, c.nextInd, c.altData, c.imgTitle);
        loaded = true;
    }

    public void become(CachedComic other) {
        set(other.image, other.prevInd, other.ind, other.nextInd, other.altData, other.imgTitle);
        loaded = other.loaded;
    }

    public void clear() {
        set(null, null, null, null, null, null);
        loaded = false;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

}
