package com.threeDBJ.comicReader;

import com.google.ads.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.DialogInterface;

import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Gravity;
import android.widget.Button;
import android.view.LayoutInflater;

import java.util.regex.Matcher;
import java.io.FileOutputStream;
import java.io.File;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.HashMap;
import android.util.Log;

public class Reader extends FragmentActivity {

    public static final String PREFS_NAME = "ComicPrefsFile";

    String prevPat, nextPat, imgPat, base, max, nextInd, altData,
	prevInd, curInd="", firstInd="1", maxInd, storeUrl,
	sdPath, title;

    static String aboutText = "Created by 3DBJ developers. Please email questions, comments, or " +
	"concerns to 3dbj.dev@gmail.com";

    CachedComic currentComic, prevComic, nextComic;

    Double maxNum=null;

    EditText selectEdit=null;
    Dialog selectDialog;
    TextView errorText=null;

    int comicInd, altInd, setPager, errorCount=0;
    int accessed = 0;
    boolean firstPressed = false, lastPressed = false;
    boolean ignoreNext = false, firstRun = false;
    boolean error = false, swipe = false;

    MyViewPager mViewPager;
    ReaderPagerAdapter mReaderPagerAdapter;

    RequestManager rm;
    SharedPreferences prefs;

    ProgressDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	setPager = 20000;
	currentComic = new CachedComic();
	prevComic = new CachedComic();
	nextComic = new CachedComic();

	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	Configuration config = this.getResources().getConfiguration();
	if(config.orientation == 1) {
            setContentView(R.layout.reader);
	} else if(config.orientation == 2) {
            setContentView(R.layout.reader_wide);
	}
	rm = new RequestManager();
	sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	rm.state = RequestManager.LoadingState.Current;
	this.UISetup();

	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	setSwipe(settings.getBoolean("swipe", false));
    }

    /* Set up click listeners and initialize the view pager */
    public void UISetup() {

	Button b = (Button) findViewById(R.id.comic_first);
	b.setOnClickListener(firstListener);
	b = (Button) findViewById(R.id.comic_prev);
	b.setOnClickListener(prevListener);
	b = (Button) findViewById(R.id.comic_last);
	b.setOnClickListener(lastListener);
	b = (Button) findViewById(R.id.comic_next);
	b.setOnClickListener(nextListener);
	b = (Button) findViewById(R.id.comic_random);
	b.setOnClickListener(randomListener);
	b = (Button) findViewById(R.id.comic_store);
	b.setOnClickListener(storeListener);
	b = (Button) findViewById(R.id.comic_alt);
	b.setVisibility(View.GONE);
	firstRun = true;
	mViewPager = (MyViewPager) findViewById(R.id.reader_pager);
	mReaderPagerAdapter = new ReaderPagerAdapter(getSupportFragmentManager());
	mViewPager.setAdapter(mReaderPagerAdapter);
	mViewPager.setOffscreenPageLimit(1);
	mViewPager.setOnPageChangeListener(pageListener);
	mViewPager.setCurrentItem(setPager);
	mViewPager.setCurrentItem(setPager);
    }

    /* Handle a change in orientation */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if(config.orientation == 1) {
            setContentView(R.layout.reader);
        } else if(config.orientation == 2) {
            setContentView(R.layout.reader_wide);
	}
	setPager = prevPos;
	this.UISetup();
	if(currentComic.image != null) {
	    setCurrentComic(currentComic.image, false);
	} else {
	}
    }

    /* Grab a comic for display on the current view page */
    public void grabComicCold(String url) {
	rm.state = RequestManager.LoadingState.Current;
	showLoadingDialog();
	rm.grabComic(this, url);
    }

    /* Helper for grabbing a random comic */
    public void grabRandomComic(String url) {
	clearComics();
	if(rm.running != 0) {
	    ignoreNext = true;
	}
	grabComicCold(url);
    }

    /* Shows a loading dialog.
       Use when the current view page is waiting on a comic to load. */
    public void showLoadingDialog() {
	loadingDialog = new ProgressDialog(this);
	loadingDialog.setMessage("Grabbing comic...");

	loadingDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
                @Override
	        public void onClick(DialogInterface dialog, int which) {
		    dialog.dismiss();
		    handleComicCancel();
		}
            });
	loadingDialog.show();
    }

    /* Sets the current comic, and preload surrounding comics.
       Special cases: Must ignore a result from the RequestManager
           -First button has been pressed and another comic is being preloaded.
           -Next button ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	   -The old next comic is being loaded after the user has swiped to previous (and vice versa) */
    public void setCurrentComic(Bitmap image, boolean setComicData) {
	errorCount = 0;
	if(image == null && ignoreNext) {
	    showLoadingDialog();
	}
	if(firstPressed || lastPressed || ignoreNext) {
	    if(firstPressed) {
		curInd = firstInd;
		prevInd = null;
	    } else if(lastPressed) {
		curInd = maxInd;
		nextInd = null;
	    }
	    firstPressed = false;
	    lastPressed = false;
	    ignoreNext = false;
	    if(rm.running != 0) {
		rm.state = RequestManager.LoadingState.Current;
		return;
	    }
	}
	Log.e("comics", curInd + " "+ maxInd);
	if(loadingDialog != null) {
	    loadingDialog.cancel();
	}
	mReaderPagerAdapter.getFragment(mViewPager.getCurrentItem()).setImage(image, mViewPager);
	currentComic.image = image;
	if(setComicData) {
	    currentComic.set(image, prevInd, curInd, nextInd, altData);
	}
	if((prevComic.image == null) && !firstInd.equals(curInd)) {
	    rm.state = RequestManager.LoadingState.Previous;
	    rm.grabComic(Reader.this, base + currentComic.prevInd);
	} else if((nextComic.image == null) && !curInd.equals(maxInd)) {
	    rm.state = RequestManager.LoadingState.Next;
	    rm.grabComic(Reader.this, base + currentComic.nextInd);
	} else {
	    rm.state = RequestManager.LoadingState.Done;
	}
    }

    /* Caches the previous comic, ignoring it when a swipe has made it obsolete.
       Triggers caching of the next comic. */
    public void setPreviousComic(Bitmap image) {
	if(ignoreNext) {
	    ignoreNext = false;
	    return;
	}
	prevComic.set(image, prevInd, currentComic.prevInd, nextInd, altData);
	if((nextComic.image == null) && !curInd.equals(maxInd)) {
	    rm.state = RequestManager.LoadingState.Next;
	    rm.grabComic(Reader.this, base + currentComic.nextInd);
	} else {
	    rm.state = RequestManager.LoadingState.Done;
	}
    }

    /* Caches the next comic, ignoring it when a swipe has made it obsolete. */
    public void setNextComic(Bitmap image) {
	if(ignoreNext) {
	    ignoreNext = false;
	    return;
	}
	nextComic.set(image, prevInd, currentComic.nextInd, nextInd, altData);
	rm.state = RequestManager.LoadingState.Done;
    }

    /* Handles a click of the prev button. */
    protected OnClickListener prevListener = new OnClickListener() {
            public void onClick(View v) {
		if(!currentComic.curInd.equals(firstInd)) {
		    mViewPager.setCurrentItem(prevPos - 1);
		}
            }
        };

    /* Handles a click of the prev button. */
    protected OnClickListener nextListener = new OnClickListener() {
            public void onClick(View v) {
		if(!currentComic.curInd.equals(maxInd)) {
		    mViewPager.setCurrentItem(prevPos + 1);
		}
            }
        };

    int prevPos = 20000;

    /* Swipe listener for the view pager. */
    OnPageChangeListener pageListener = new OnPageChangeListener() {

	    @Override
	    public void onPageScrollStateChanged (int state) {
	    }
	    @Override
	    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	    }

	    /* Handles a page change. If a caching operation is in process, it must
	       either be ignored or marked as the current comic. */
	    @Override
	    public void onPageSelected (int position) {
		if(firstRun) {
		    firstRun = false;
		    return;
		}
		mReaderPagerAdapter.clean(position);
		Bitmap temp;
		/* Load the next comic. */
		if(position > prevPos) {
		    Log.e("ctrl alt del", currentComic.curInd + " middle "+maxInd);
		    if(currentComic.curInd.equals(maxInd)) {
			mViewPager.setCurrentItem(prevPos);
			return;
		    }
		    prevComic.clear();
		    prevComic.become(currentComic);
		    currentComic.become(nextComic);
		    curInd = prevComic.nextInd;
		    currentComic.curInd = curInd;
		    temp = nextComic.image;
		    nextComic.image = null;
		    if(rm.running == 1) {
			if(temp != null) {
			    setCurrentComic(temp, false);
			    ignoreNext = true;
			    prevPos = position;
			    return;
			}
			rm.state = RequestManager.LoadingState.Current;
			if(rm.state == RequestManager.LoadingState.Previous) {
			    ignoreNext = true;
			    rm.grabComic(Reader.this, base + prevComic.nextInd);
			}
			showLoadingDialog();
		    } else {
			setCurrentComic(temp, false);
		    }
	        /* Load the previous comic. */
		} else if(position < prevPos) {
		    if(currentComic.curInd.equals(firstInd)) {
			mViewPager.setCurrentItem(prevPos);
			return;
		    }
		    nextComic.clear();
		    nextComic.become(currentComic);
		    currentComic.become(prevComic);
		    curInd = nextComic.prevInd;
		    currentComic.curInd = curInd;
		    temp = prevComic.image;
		    prevComic.image = null;
		    if(rm.running == 1) {
			if(temp != null) {
			    setCurrentComic(temp, false);
			    ignoreNext = true;
			    prevPos = position;
			    return;
			}
			rm.state = RequestManager.LoadingState.Current;
			if(rm.state == RequestManager.LoadingState.Next) {
			    ignoreNext = true;
			    rm.grabComic(Reader.this, base + nextComic.prevInd);
			}
			showLoadingDialog();
		    } else {
			setCurrentComic(temp, false);
		    }
		}
		prevPos = position;
	    }
	};

    /* Adapter for the view pager. Creates and destroys views when necessary. */
    private class ReaderPagerAdapter extends FragmentStatePagerAdapter {

	HashMap<Integer, PageFragment> fragMap = new HashMap<Integer, PageFragment>();

	public ReaderPagerAdapter(FragmentManager fm) {
	    super(fm);
	}

	public void destroyItem(ViewGroup container, int position, Object object) {
	    super.destroyItem(container, position, object);
	    PageFragment frag = fragMap.remove(position);
	    frag.clean();
	}

	public void kill() {
	    for(Fragment frag : fragMap.values()) {
		getSupportFragmentManager().beginTransaction().remove((Fragment)frag).commit();
	    }
	}

	public PageFragment getFragment(int pos) {
	    return fragMap.get(pos);
	}

	public void clean(int pos) {
	}

        @Override
	public Fragment getItem(int index) {
	    PageFragment ret;
	    ret = PageFragment.newInstance();
	    fragMap.put(index, ret);
	    return ret;
	}

        @Override
	public int getCount() {
	    return Integer.MAX_VALUE;
	}
    }

    public void loadAd() {
	AdRequest req = new AdRequest ();
        AdView ad = (AdView) findViewById(R.id.ad);
        ad.loadAd (req);
    }

    /* Sets a listener for the alt text/image of a comic strip. */
    public void setAltListener(OnClickListener altListener) {
	Button b = (Button) findViewById(R.id.comic_alt);
	b.setVisibility(View.VISIBLE);
	b.setOnClickListener(altListener);
    }

    /* Placeholder for method */
    public String handleRawPage(String page) throws Exception {
	throw new Exception("Error - unimplemented.");
    }

    /* Display error message (tap to refresh) - Most likely caused by
       network failure. TODO - Update method */
    public void handleComicError() {
	if(loadingDialog != null) {
	    loadingDialog.cancel();
	}
	errorCount += 1;
	if(currentComic.image == null) {
	    ViewGroup ll = (ViewGroup) findViewById(R.id.comic_wrapper);
	    if(errorText == null) {
		errorText = (TextView) getLayoutInflater().inflate(R.layout.error_text_view, null);
		errorText.setOnClickListener(reloadListener);
		ll.setOnClickListener(reloadListener);
		ll.addView(errorText, 0);
	    }
	    error = true;
	}
    }

    /* Occurs when the user cancels a loading comic.
       Should re-display whatever was currently showing */
    public void handleComicCancel() {
	if(currentComic.image == null) {
	    handleComicError();
	}
    }

    /* Shows a dialog for selecting a comic by entering a number. */
    public void selectComic() {
	selectDialog = new Dialog(this);
	selectDialog.setContentView(R.layout.select_comic);
	selectDialog.setTitle(title);
	selectEdit = (EditText) selectDialog.findViewById(R.id.select_input);

	Button b = (Button) selectDialog.findViewById(R.id.select_enter);
	b.setOnClickListener(goToComicListener);
	selectDialog.setCancelable(true);
	selectDialog.show();
	selectEdit.requestFocus();
    }

    /* Saves the current comic to /sdcard/comics/<comic strip name>/<comic name> */
    public void saveToSD() {
	if(!isStorageWritable()) {
	    showDialog("Warning", "SD card not writable or could not be found.");
	} else if(currentComic.image != null) {
	    try {
		String path = sdPath + "/comics/" + title + "/";
		File dir = new File(path);
		if(!dir.exists())
		    dir.mkdirs();
		String saveTitle = (currentComic.imgTitle == null) ? currentComic.curInd : currentComic.imgTitle;
		Log.v("sdpath", saveTitle + ".png");
		File file = new File(dir, saveTitle + ".png");
		FileOutputStream fOut = new FileOutputStream(file);

		currentComic.image.compress(Bitmap.CompressFormat.PNG, 85, fOut);
		fOut.flush();
		fOut.close();
	    } catch(Exception e) {
		Log.v("SDError", e.getMessage());
		showDialog("Warning", "Comic could not be saved.");
	    }
	} else {
	    /* Warn that comic could not be saved */
	    Log.v("SDError", "bitmap was null :(");
	    showDialog("Warning", "Comic could not be saved.");
	}
    }

    /* Shows a cancelable dialog with the given title and text. */
    public void showDialog(String title, String text) {
	final Dialog dialog = new Dialog(this);
	dialog.setContentView(R.layout.text_alt);
	dialog.setTitle(title);
	TextView t = (TextView) dialog.findViewById(R.id.alt_text);
	t.setText(text);
	dialog.setCancelable(true);
	Button done = (Button) dialog.findViewById(R.id.alt_done);
	done.setOnClickListener(makeDialogCancelListener(dialog));
	dialog.show();
    }

    /* Displays alt text for a comic. */
    public void dispAltText(String text, String title) {
	if(text != null) {
	    text = text.replaceAll("&#39;", "'");
	    text = text.replaceAll("&quot", "\"");
	    showDialog(title, text);
	} else {
	    showDialog(title, "No alt text to display.");
	}
    }

    /* Displays the alt image for a comic. */
    public void dispAltImage(String url, String title) {
	final Dialog dialog = new Dialog(this);

	dialog.setContentView(R.layout.image_alt);
	dialog.setTitle("Loading");
	rm.displayAltImage(dialog, url, title);
	dialog.setCancelable(true);
	Button done = (Button) dialog.findViewById(R.id.alt_done);
	done.setOnClickListener(makeDialogCancelListener(dialog));
	dialog.show();
    }

    /* For save to SD */
    public void setImageTitle(String imgTitle) {
	currentComic.setImageTitle(imgTitle);
    }

    /* Set the index of the last available comic.
     Only do this once -- hacky solution for SMBC where it looks like second to last is last. */
    public void setMaxIndex(String ind) {
	if(maxInd == null)
	    this.maxInd = ind;
	this.curInd = ind;
    }

    /* To account for sites like Feel Afraid where the index has a trailing .php, etc */
    public void setMaxNum(String ind) {
	if(maxNum == null)
	    this.maxNum = Double.parseDouble(ind);
    }

    /* Returns whether the max has been recorded. */
    public boolean haveMax() {
	return maxNum != null || maxInd != null;
    }

    /* Sets the next index of the comic being grabbed. */
    public void setNextIndex(String next) {
	this.nextInd = next;
    }

    /* Sets the previous index of the comic being grabbed. */
    public void setPrevIndex(String prev) {
	this.prevInd = prev;
    }

    /* Sets the alt text or alt image url for a comic. */
    public void setAlt(String alt) {
	altData = alt;
    }

    /* Handles a click of the first button. */
    protected OnClickListener firstListener = new OnClickListener() {
            public void onClick(View v) {
		if(curInd == null) {
		    showDialog("Error", "Cannot connect to the internet.");
		} else if(currentComic.curInd == null || !currentComic.curInd.equals(firstInd)) {
		    firstPressed = true;
		    clearComics();
		    grabComicCold(base + firstInd);
		}
            }
        };

    /* Handles a click of the last button. */
    protected OnClickListener lastListener = new OnClickListener() {
            public void onClick(View v) {
		if(curInd == null) {
		    showDialog("Error", "Cannot connect to the internet.");
		} else if(currentComic.curInd == null || !currentComic.curInd.equals(maxInd)) {
		    lastPressed = true;
		    clearComics();
		    grabComicCold(max);
		}
	    }
        };

    /* To be overidden if the comic id similar, but not exactly equal to the index. */
    public String getIndFromNum(String num) {
	return num;
    }

    /* Clears the current and cached comics. */
    public void clearComics() {
	prevComic.clear();
	currentComic.clear();
	nextComic.clear();
    }

    /* General click listener for getting a random comic.
       To be used when the comic as integer indices that increment by 1. */
    protected OnClickListener randomListener = new OnClickListener() {
            public void onClick(View v) {
		if(haveMax()) {
		    int n = (int)(Math.random() * maxNum);
		    curInd = getIndFromNum(Integer.toString(n));
		    grabRandomComic(base + curInd);
		} else {
		    showDialog("Error", "Could not resolve random comic.");
		}
            }
        };

    /* Listener for the store button. Opens a browser window with the strip's store. */
    protected OnClickListener storeListener = new OnClickListener() {
            public void onClick(View v) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(storeUrl));
		startActivity(i);
            }
        };

    protected OnClickListener reloadListener = new OnClickListener() {
            public void onClick(View v) {
		if(errorText != null) {
		    ViewGroup ll = (ViewGroup) findViewById(R.id.comic_wrapper);
		    ll.removeView(errorText);
		}
		if(currentComic.curInd == null) {
		    if(curInd == null) {
			currentComic.curInd = maxInd;
			grabComicCold(max);
		    } else {
			currentComic.curInd = curInd;
			grabComicCold(base + curInd);
		    }
		} else {
		    grabComicCold(base + currentComic.curInd);
		}
            }
        };

    /* Listener for the ok button on the 'go to' input dialog. */
    protected OnClickListener goToComicListener = new OnClickListener() {
            public void onClick(View v) {
		if(selectEdit != null) {
		    String index = selectEdit.getText().toString();
		    selectDialog.cancel();
		    clearComics();
		    curInd = getIndFromNum(index);
		    grabComicCold(base + curInd);
		    selectEdit = null;
		}
            }
        };

    /* Cancels the input dialog when clicked. */
    public OnClickListener makeDialogCancelListener(final Dialog d) {
	return new OnClickListener() {
	    public void onClick(View v) {
		d.dismiss();
	    }
	};
    }

    /* Determines whether the sdcard is writable. TODO - probably not perfect. */
    public static boolean isStorageWritable() {
	boolean storageWriteable  = false;
	String state = Environment.getExternalStorageState();

	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    storageWriteable = true;
	}

	return storageWriteable;
    }

    public void setSwipe(boolean on) {
	swipe = on;
	mViewPager.setSwipeEnabled(on);
	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	SharedPreferences.Editor editor = settings.edit();
	editor.putBoolean("swipe", swipe);
	editor.commit();
    }

    /* Inflates the options menu. TODO -convert menu stuff to action bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comic_menu, menu);
        return true;
    }

    /* Updates the swipe option title */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	MenuItem item = (MenuItem) menu.findItem(R.id.comic_swipe);
	if(swipe) {
	    item.setTitle("Disable Swipe");
	} else {
	    item.setTitle("Enable Swipe");
	}
        return true;
    }

    /* Handles the selection of a menu item. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
        case R.id.comic_select:
            selectComic();
            break;
        case R.id.comic_save:
            saveToSD();
            break;
        case R.id.comic_about:
            showDialog("About", this.aboutText);
            break;
        case R.id.comic_swipe:
            setSwipe(!swipe);
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

}