package com.threeDBJ.comicReader;

import android.graphics.Bitmap;
import android.util.Log;

public class CachedComic {

    public Bitmap image;
    public String prevInd, nextInd, curInd, altData, imgTitle;
    public int size = 0;

    public CachedComic() {
    }

    public CachedComic(Bitmap image, String prevInd, String curInd, String nextInd) {
	set(image, prevInd, curInd, nextInd, null);
    }

    public void set(Bitmap image, String prevInd, String curInd, String nextInd, String altData) {
	if(image != null) {
	    size = image.getRowBytes() * image.getHeight();
	}
	this.image = image;
	this.prevInd = prevInd;
	this.curInd = curInd;
	this.nextInd = nextInd;
	this.altData = altData;
    }

    public int getSizeInBytes() {
	return size;
    }

    public void become(CachedComic other) {
	set(other.image, other.prevInd, other.curInd, other.nextInd, other.altData);
    }

    public void clear() {
	if(image != null) {
	}
	set(null, null, null, null, null);
    }

    public void setImageTitle(String imgTitle) {
	this.imgTitle = imgTitle;
    }

}