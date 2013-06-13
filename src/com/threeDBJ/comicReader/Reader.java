package com.threeDBJ.comicReader;

import com.google.ads.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.text.Html;

import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Gravity;
import android.widget.Button;
import android.view.LayoutInflater;

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
import java.util.HashSet;
import java.util.regex.Pattern;

public class Reader extends FragmentActivity {

    public static final String PREFS_NAME = "ComicPrefsFile";

    String prevPat, nextPat, imgPat, base, max, errorInd,
        firstInd="1", maxInd, storeUrl,	sdPath, title, shortTitle;
    Pattern pImages, pPrev, pNext, pIndices, pMax, pAlt;

    static String aboutText = "Created by 3DBJ developers. Please email questions, comments, or " +
        "concerns to 3dbj.dev@gmail.com";

    HashSet<String> loadingInds = new HashSet<String>();
    HashMap<String, CachedComic> comicMap = new HashMap<String, CachedComic>();
    CachedComic curComic = new CachedComic();
    CachedComic prevComic = new CachedComic();
    CachedComic nextComic = new CachedComic();

    int maxNum=-1;

    EditText selectEdit;
    Dialog selectDialog;
    TextView errorText;

    int setPager=20000, errorCount=0, accessed=0;
    boolean firstRun=false, error=false, swipe=false,
        nextEnabled=true, prevEnabled=false, loadLastViewed;

    MyViewPager mViewPager;
    ReaderPagerAdapter mReaderPagerAdapter;

    RequestManager rm = new RequestManager();
    SharedPreferences prefs;

    ProgressDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        Configuration config = this.getResources().getConfiguration();
        if(config.orientation == 1) {
            setContentView(R.layout.reader);
        } else if(config.orientation == 2) {
            setContentView(R.layout.reader_wide);
        }
        sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.UISetup();

        Intent intent = getIntent();
        this.loadLastViewed = intent.getExtras().getBoolean("load_last_viewed");

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        setSwipe(settings.getBoolean("swipe", false));
    }

    /* Get the base (prefix) for comic queries */
    public String getBase() {
	return base;
    }

    /* Get the url for the latest comic. */
    public String getMax() {
	return max;
    }

    /* Get the index of the first comic */
    public String getFirstInd() {
	return firstInd;
    }

    public void init() {
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
	mReaderPagerAdapter.getFragment(mViewPager.getCurrentItem()).setComic(curComic, mViewPager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(curComic.ind != null) {
            Editor edit = prefs.edit();
            edit.putString(shortTitle, curComic.ind);
            if(maxNum != -1) {
                edit.putInt(shortTitle+"-max", maxNum);
            }
            edit.commit();
        }
    }

    public void loadInitial(String url) {
        if(loadLastViewed) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String lastInd = prefs.getString(shortTitle, "");
            if(lastInd.equals("") || lastInd.equals(max)) {
                maxNum = prefs.getInt(shortTitle+"-max", -1);
                loadLast(url);
            } else {
                comicMap.put(lastInd, curComic);
                showLoadingDialog();
                loadComic(lastInd);
            }
        } else {
            loadLast(url);
        }
    }

    public void loadLast(String url) {
        removeError();
        loadingInds.add(url);
        comicMap.put(url, curComic);
        rm.grabComic(this, new Comic("", url));
        errorInd = null;
        showLoadingDialog();
    }

    public void loadComic(String ind) {
        removeError();
        if(ind != null && !loadingInds.contains(ind)) {
            loadingInds.add(ind);
            rm.grabComic(this, new Comic(getBase(), ind));
        }
    }

    /* Notification from the RequestManager that a comic has finished loading.
       Matches comic url with current cur/prev/nextComic */
    public void notifyComicLoaded(Comic c) {
        loadingInds.remove(c.getInd());
        CachedComic cached = comicMap.remove(c.getInd());
        if(cached != null) {
            cached.load(c);
            if(cached == curComic) {
                errorInd = null;
                if(loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                PageFragment frag = mReaderPagerAdapter.getFragment(mViewPager.getCurrentItem());
                if(frag == null) {
                    handleComicCancel();
                } else {
                    frag.setComic(c, mViewPager);
                    prevEnabled = true;
                    nextEnabled = true;
                }
            } else if(cached == prevComic) {
                prevEnabled = true;
            } else if(cached == nextComic) {
                nextEnabled = true;
            }
            if(curComic.loaded) {
                if(!prevComic.loaded && !loadingInds.contains(curComic.prevInd)) {
                    comicMap.put(curComic.prevInd, prevComic);
                    loadComic(curComic.prevInd);
                } else if(!nextComic.loaded && !loadingInds.contains(curComic.nextInd)) {
                    comicMap.put(curComic.nextInd, nextComic);
                    loadComic(curComic.nextInd);
                }
            }
        }
    }

    /* Display error message (tap to refresh) - Most likely caused by
       network failure. TODO - Update method */
    public void handleComicError(Comic c) {
        loadingInds.remove(c.getInd());
        CachedComic cached = comicMap.remove(c.getInd());
        if(c.getInd() == null && curComic.ind == null) {
            comicMap.clear();
            loadingInds.clear();
            cached = curComic;
        }
        if(loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.cancel();
        }
        errorCount += 1;
        //DebugLog.e("comic", "error:"+c.getInd()+" "+curComic.ind);
        if(cached == curComic && !curComic.loaded) {
            //DebugLog.e("comic", "error: errTextNull = "+(errorText == null));
            ViewGroup ll = (ViewGroup) findViewById(R.id.comic_wrapper);
            if(errorText == null) {
                errorText = (TextView) getLayoutInflater().inflate(R.layout.error_text_view, ll, false);
                errorText.setOnClickListener(reloadListener);
                ll.setOnClickListener(reloadListener);
                ll.addView(errorText, 0);
                ll.bringChildToFront(errorText);
            }
            prevEnabled = prevComic.loaded;
            nextEnabled = nextComic.loaded;
            error = true;
        }
    }

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
                /* Load the next comic. */
                String newInd = "";
                if(position > prevPos) {
                    if(curComic.ind == null && nextEnabled) {
                    } else if(curComic.ind == null || !nextEnabled || curComic.ind.equals(maxInd) ||
                              curComic.ind.equals(getMax())) {
                        mViewPager.setCurrentItem(prevPos);
                        return;
                    }
                    prevComic.become(curComic);
                    curComic.become(nextComic);
                    nextComic.clear();

                    newInd = prevComic.nextInd;
                    if(curComic.loaded) {
                        comicMap.put(curComic.nextInd, nextComic);
                        loadComic(curComic.nextInd);
                    }
                    /* Load the previous comic. */
                } else if(position < prevPos) {
                    if(curComic.ind == null && prevEnabled) {
                    } else if(curComic.ind == null || !prevEnabled || curComic.ind.equals(getFirstInd())) {
                        mViewPager.setCurrentItem(prevPos);
                        return;
                    }
                    nextComic.become(curComic);
                    curComic.become(prevComic);
                    prevComic.clear();

                    newInd = nextComic.prevInd;
                    if(curComic.loaded && !curComic.ind.equals(getFirstInd())) {
                        comicMap.put(curComic.prevInd, prevComic);
                        loadComic(curComic.prevInd);
                    }
                }
                removeError();
                prevEnabled = true; nextEnabled = true;
                comicMap.put(newInd, curComic);
                if(loadingInds.contains(newInd)) {
                    showLoadingDialog();
                    errorInd = newInd;
                } else if(!curComic.loaded) {
                    showLoadingDialog();
                    loadComic(newInd);
                    errorInd = newInd;
                } else {
                    mReaderPagerAdapter.getFragment(mViewPager.getCurrentItem()).setComic(curComic, mViewPager);
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
            if(frag != null) frag.clean();
        }

        public void kill() {
            for(Fragment frag : fragMap.values()) {
                getSupportFragmentManager().beginTransaction().remove((Fragment)frag).commit();
            }
        }

        public void clearImages() {
            for(PageFragment frag : fragMap.values()) {
                frag.setComic(null, mViewPager);
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

    /* Helper for grabbing a random comic */
    public void grabRandomComic(String url) {
        clearComics();
        //grabComicCold(url);
    }

    /* Shows a loading dialog.
       Use when the current view page is waiting on a comic to load. */
    public void showLoadingDialog() {
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Grabbing comic...");
        loadingDialog.setCancelable(false);

        loadingDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    handleComicCancel();
                }
            });
        loadingDialog.show();
    }

    /* Handles a click of the prev button. */
    protected OnClickListener prevListener = new OnClickListener() {
            public void onClick(View v) {
                if(prevEnabled && (curComic.ind != null) && !curComic.ind.equals(getFirstInd())) {
                    mViewPager.setCurrentItem(prevPos - 1);
                }
            }
        };

    /* Handles a click of the prev button. */
    protected OnClickListener nextListener = new OnClickListener() {
            public void onClick(View v) {
		String ind = curComic.getInd();
                if(nextEnabled && ind != null && !ind.equals(maxInd) && !ind.equals(max)) {
                    mViewPager.setCurrentItem(prevPos + 1);
                }
            }
        };

    /* Sets a listener for the alt text/image of a comic strip. */
    public void setAltListener(OnClickListener altListener) {
        Button b = (Button) findViewById(R.id.comic_alt);
        b.setVisibility(View.VISIBLE);
        b.setOnClickListener(altListener);
    }

    /* Placeholder for method */
    public String handleRawPage(Comic c, String page) throws Exception {
        throw new Exception("Error - unimplemented.");
    }

    /* Occurs when the user cancels a loading comic.
       Should re-display whatever was currently showing */
    public void handleComicCancel() {
        if(curComic.image == null) {
            handleComicError(new Comic(curComic));
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
        } else if(curComic.image != null) {
            try {
                String path = sdPath + "/comics/" + title + "/";
                File dir = new File(path);
                if(!dir.exists())
                    dir.mkdirs();
                String saveTitle;
                if(curComic.imgTitle != null) {
                    saveTitle = curComic.imgTitle;
                } else if(curComic.ind.equals(maxInd) || curComic.ind.equals(getMax())) {
                    saveTitle = (maxInd == null) ? Integer.toString(maxNum) : maxInd;
                } else {
                    saveTitle = curComic.ind;
                }
                DebugLog.v("comic", dir+" "+saveTitle + ".png");
                File file = new File(dir, saveTitle + ".png");
                FileOutputStream fOut = new FileOutputStream(file);

                curComic.image.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
            } catch(Exception e) {

                if(e.getMessage() != null) {
                    DebugLog.v("SDError", e.getMessage());
                }
                showDialog("Warning", "Comic could not be saved.");
            }
        } else {
            /* Warn that comic could not be saved */
            DebugLog.v("SDError", "bitmap was null :(");
            showDialog("Warning", "Comic could not be saved.");
        }
    }

    /* Creats a dialog and returns it's body TextView to fill */
    public Dialog createEmptyDialog(String title) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.text_alt);  
        dialog.setTitle(title);     
        dialog.setCancelable(true); 
        Button done = (Button) dialog.findViewById(R.id.alt_done);
        done.setOnClickListener(makeDialogCancelListener(dialog));
        return dialog;
    }

    /* Shows a cancelable dialog with the given title and text. */
    public void showDialog(String title, String text) {
        final Dialog dialog = createEmptyDialog(title);
        TextView t = (TextView) dialog.findViewById(R.id.alt_text);
        t.setText(text);
        dialog.show();
    }

    /* Displays alt text for a comic. */
    public void dispAltText(String text, String title) {
        if(text != null) {
            final Dialog dialog = createEmptyDialog(title);
            TextView t = (TextView) dialog.findViewById(R.id.alt_text);
            t.setText(Html.fromHtml(text));
            dialog.show();
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

    /* Set the index of the last available comic.
       Only do this once -- hacky solution for SMBC where it looks like second to last is last. */
    public void setMaxIndex(String ind) {
        if(maxInd == null) {
            this.maxInd = ind;
            curComic.ind = ind;
        }
    }

    /* To account for sites like Feel Afraid where the index has a trailing .php, etc */
    public void setMaxNum(String ind) {
        if(maxNum == -1)
            this.maxNum = (int)Double.parseDouble(ind);
    }

    /* Returns whether the max has been recorded. */
    public boolean haveMax() {
        return maxNum != -1 || maxInd != null;
    }

    /* Sets the next index of the comic being grabbed. */
    public void setNextInd(String next) {
        curComic.nextInd = next;
    }

    /* Sets the previous index of the comic being grabbed. */
    public void setPrevInd(String prev) {
        curComic.prevInd = prev;
    }

    /* Handles a click of the first button. */
    protected OnClickListener firstListener = new OnClickListener() {
            public void onClick(View v) {
		String firstInd = getFirstInd();
                if(curComic.ind == null) {
                    showDialog("Error", "Cannot connect to the internet.");
                } else if(curComic.ind == null || !curComic.ind.equals(firstInd)) {
                    clearComics();
                    clearVisible();
                    comicMap.put(firstInd, curComic);
                    loadComic(firstInd);
                    errorInd = firstInd;
                    showLoadingDialog();
                }
            }
        };

    /* Handles a click of the last button. */
    protected OnClickListener lastListener = new OnClickListener() {
            public void onClick(View v) {
                if(curComic.ind == null) {
                    showDialog("Error", "Cannot connect to the internet.");
                } else if(curComic.ind == null || !curComic.ind.equals(maxInd)) {
                    clearComics();
                    clearVisible();
                    loadLast(getMax());
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
        curComic.clear();
        nextComic.clear();
        comicMap.clear();
        loadingInds.clear();
    }

    /* Clears visible comic */
    public void clearVisible() {
        mReaderPagerAdapter.clearImages();
    }

    /* General click listener for getting a random comic.
       To be used when the comic as integer indices that increment by 1. */
    protected OnClickListener randomListener = new OnClickListener() {
            public void onClick(View v) {
                if(maxNum != -1) {
                    int n = (int)(Math.random() * maxNum);
                    String ind = getIndFromNum(Integer.toString(n));

                    clearComics();
                    clearVisible();
                    comicMap.put(ind, curComic);
                    loadComic(ind);
                    errorInd = ind;
                    showLoadingDialog();
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

    public void removeError() {
        ViewGroup ll = (ViewGroup) findViewById(R.id.comic_wrapper);
        if(errorText != null && ll.findViewById(R.id.error_text) != null) {
            ll.removeView(errorText);
            errorText = null;
        }
    }

    protected OnClickListener reloadListener = new OnClickListener() {
            public void onClick(View v) {
                removeError();
                if(curComic.loaded) return;
                if(errorInd != null) {
                    comicMap.put(errorInd, curComic);
                    loadComic(errorInd);
                    showLoadingDialog();
                } else if(curComic.ind == null || curComic.ind.equals(getMax())) {
                    curComic.ind = maxInd;
                    clearComics();
                    clearVisible();
                    loadLast(getMax());
                } else {
                    comicMap.put(curComic.ind, curComic);
                    loadComic(curComic.ind);
                    showLoadingDialog();
                }
            }
        };

    /* Listener for the ok button on the 'go to' input dialog. */
    protected OnClickListener goToComicListener = new OnClickListener() {
            public void onClick(View v) {
                if(selectEdit != null) {
                    String index = selectEdit.getText().toString();
                    selectDialog.cancel();
                    String newInd = getIndFromNum(index);
                    clearComics();
                    clearVisible();
                    comicMap.put(newInd, curComic);
                    loadComic(newInd);
                    errorInd = newInd;
                    showLoadingDialog();
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
        Editor editor = settings.edit();
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