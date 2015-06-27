package com.threeDBJ.comicReader;

import android.app.Application;
import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.HashSet;

public class ComicReaderApp extends Application {
    static final String TAG = "webComicReader";
    ComicState state = new ComicState();
    RequestManager rm = new RequestManager();
    Reader activeReader;

    @Override
    public void onCreate () {
    }

    public ComicState getComicState() {
	return state;
    }

    public RequestManager getRequestManager() {
	return rm;
    }

    public void setReader(Reader reader) {
	this.activeReader = reader;
    }

    public class ComicState {
	public CachedComic curComic = new CachedComic();
	public CachedComic prevComic = new CachedComic();
	public CachedComic nextComic = new CachedComic();
	HashSet<String> loadingInds = new HashSet<String>();
	HashMap<String, CachedComic> comicMap = new HashMap<String, CachedComic>();
	boolean hasUnseenComic = false;
	String prevShortTitle;

	public int prevPos=20000, setPager=20000;

	public ComicState() {
	}

	public boolean isLoading() {
	    return rm.running > 0;
	}

	public void comicLoaded(Comic c) {
	    loadingInds.remove(c.getInd());
	    CachedComic cached = comicMap.remove(c.getInd());
	    if(cached != null) {
		cached.load(c);
		if(activeReader != null) {
		    activeReader.notifyComicLoaded(cached);
		    if(curComic.loaded) {
			if(!prevComic.loaded && !loadingInds.contains(curComic.prevInd)) {
			    comicMap.put(curComic.prevInd, prevComic);
			    activeReader.loadComic(curComic.prevInd);
		    } else if(!nextComic.loaded && !loadingInds.contains(curComic.nextInd)) {
			    comicMap.put(curComic.nextInd, nextComic);
			    activeReader.loadComic(curComic.nextInd);
			}
		    }
		} else {
		    hasUnseenComic = true;
		}
	    }
	}

	public void comicError(Comic c) {
	    loadingInds.remove(c.getInd());
	    CachedComic cached = comicMap.remove(c.getInd());
	    if(c.getInd() == null && curComic.ind == null) {
		comicMap.clear();
		loadingInds.clear();
		cached = state.curComic;
	    }
	    if(activeReader != null) {
		activeReader.handleComicError(cached);
	    }
	}

	/* Occurs when the user cancels a loading comic.
	   Should re-display whatever was currently showing */
	public void handleComicCancel() {
	    if(curComic.image == null) {
		comicError(new Comic(state.curComic));
	    }
	}

	/* Clears the current and cached comics. */
	public void clearComics() {
	    prevComic.clear();
	    curComic.clear();
	    nextComic.clear();
	    comicMap.clear();
	    loadingInds.clear();
	}
	
    }

}