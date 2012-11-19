package com.threeDBJ.comicReader;

import android.graphics.Bitmap;
import android.util.Log;

public class CachedComic {

    public Bitmap image;
    public String prevInd, nextInd, curInd, altData, imgTitle;
    public int size = 0;
    boolean loaded = false;

    public CachedComic() {
    }

    public CachedComic(Bitmap image, String prevInd, String curInd, String nextInd) {
	set(image, prevInd, curInd, nextInd, null, null);
    }

    public void set(Bitmap image, String prevInd, String curInd, String nextInd, String altData, String imgTitle) {
	if(image != null) {
	    size = image.getRowBytes() * image.getHeight();
	}
	this.image = image;
	this.prevInd = prevInd;
	this.curInd = curInd;
	this.nextInd = nextInd;
	this.altData = altData;
	this.imgTitle = imgTitle;
    }

    public void load(Comic c) {
	set(c.comic, c.prevInd, c.ind, c.nextInd, c.altData, c.imgTitle);
	loaded = true;
    }

    public int getSizeInBytes() {
	return size;
    }

    public void setComic(Bitmap comic) {
	this.image = comic;
    }

    public void become(CachedComic other) {
	set(other.image, other.prevInd, other.curInd, other.nextInd, other.altData, other.imgTitle);
	loaded = other.loaded;
    }

    public void clear() {
	if(image != null) {
	}
	set(null, null, null, null, null, null);
	loaded = false;
    }

}