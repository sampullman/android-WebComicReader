package com.threeDBJ.comicReader.reader;

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
import android.text.method.LinkMovementMethod;
import android.text.Spanned;

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
import java.util.regex.Pattern;

import com.threeDBJ.comicReader.*;
import com.threeDBJ.comicReader.ComicReaderApp.ComicState;

public class Reader extends FragmentActivity {
    static final String TAG = "webComicReader";
    public static final String PREFS_NAME = "ComicPrefsFile";
    ComicReaderApp app;
    ComicState state;

    String prevPat, nextPat, imgPat, base, max, errorInd,
        firstInd="1", maxInd, storeUrl,	sdPath, title, shortTitle;
    Pattern pImages, pPrev, pNext, pIndices, pMax, pAlt;

    Spanned aboutHtml;
    String aboutText = "Created by 3DBJ developers. Please email questions, comments, or concerns to "+
        "<a href=\"mailto:3dbj.dev@gmail.com\">3dbj.dev@gmail.com</a>";

    int maxNum=-1;

    EditText selectEdit;
    Dialog selectDialog;
    TextView errorText;

    int errorCount=0, accessed=0;
    boolean firstRun=false, error=false, swipe=false,
        nextEnabled=true, prevEnabled=false, loadLastViewed;

    MyViewPager mViewPager;
    ReaderPagerAdapter mReaderPagerAdapter;

    RequestManager rm;
    SharedPreferences prefs;

    ProgressDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (ComicReaderApp)getApplicationContext();
        app.setReader(this);
        state = app.getComicState();
        rm = app.getRequestManager();
        this.aboutHtml = Html.fromHtml(aboutText);

        setContentView(R.layout.reader);

        sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.UISetup();

        Intent intent = getIntent();
        this.loadLastViewed = intent.getExtras().getBoolean("load_last_viewed");

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        setSwipe(settings.getBoolean("swipe", false));
    }

    /*
      @Override
      public void onSaveInstanceState(Bundle state) {
      state.putInt
      }
    */

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
        mViewPager.setCurrentItem(state.setPager);
        mViewPager.setCurrentItem(state.setPager);
        /*
          PageFragment frag = mReaderPagerAdapter.getFragment(state.setPager);
          if(state.curComic != null && frag != null) {
          frag.setComic(state.curComic, mViewPager);
          }
        */
    }

    public MyViewPager getViewPager() {
        return this.mViewPager;
    }

    public ComicState getState() {
        return this.state;
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

    public CachedComic getCurComic() {
        return state.curComic;
    }

    public void init() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.setReader(null);
        firstRun = true;
        state.prevShortTitle = shortTitle;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(state.curComic.getInd() != null) {
            Editor edit = prefs.edit();
            edit.putString(shortTitle, state.curComic.getInd());
            if(maxNum != -1) {
                edit.putInt(shortTitle+"-max", maxNum);
            }
            edit.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        app.setReader(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugLog.e(TAG, "Comic Reader Destroyed");
    }

    public void loadInitial(String url) {
        if(loadLastViewed) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String lastInd = prefs.getString(shortTitle, "");
            if(lastInd.equals(state.curComic.getInd())) {
                mReaderPagerAdapter.freshComics = true;
            } else if(lastInd.equals("") || lastInd.equals(max)) {
                maxNum = prefs.getInt(shortTitle+"-max", -1);
                loadLast(url);
            } else {
                state.comicMap.put(lastInd, state.curComic);
                showLoadingDialog();
                loadComic(lastInd);
            }
            loadLastViewed = false;
        } else {
            loadLast(url);
        }
    }

    public void loadLast(String url) {
        boolean freshComic = shortTitle.equals(state.prevShortTitle);
        DebugLog.e("Loading last: freshComic="+freshComic+", loading="+state.isLoading()+", running="+rm.running+", curComic null="+(state.curComic.getComic()==null));
        if(freshComic && state.curComic.getComic() != null) {
            //notifyComicLoaded(state.curComic);
            DebugLog.e("Loading last comic image");
            mReaderPagerAdapter.freshComics = true;
            state.hasUnseenComic = false;
        } else if(state.isLoading() && freshComic) {
            showLoadingDialog();
        } else {
            removeError();
            state.loadingInds.add(url);
            state.comicMap.put(url, state.curComic);
            rm.grabComic(this, new Comic("", url));
            errorInd = null;
            showLoadingDialog();
        }
    }

    public void loadComic(String ind) {
        removeError();
        if(ind != null && !state.loadingInds.contains(ind)) {
            state.loadingInds.add(ind);
            rm.grabComic(this, new Comic(getBase(), ind));
        }
    }

    /* Notification from the RequestManager that a comic has finished loading.
       Matches comic url with current cur/prev/nextComic */
    public void notifyComicLoaded(CachedComic cached) {
        DebugLog.e("Notified of comic load, cur? "+(cached == state.curComic));
        if(cached == state.curComic) {
            errorInd = null;
            if(loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            PageFragment frag = mReaderPagerAdapter.getFragment(mViewPager.getCurrentItem());
            if(frag == null) {
                state.handleComicCancel();
            } else {
                frag.setComic(cached, mViewPager);
                prevEnabled = true;
                nextEnabled = true;
            }
        } else if(cached == state.prevComic) {
            prevEnabled = true;
        } else if(cached == state.nextComic) {
            nextEnabled = true;
        }
    }

    /* Display error message (tap to refresh) - Most likely caused by
       network failure. TODO - Update method */
    public void handleComicError(CachedComic cached) {
        if(loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.cancel();
        }
        errorCount += 1;
        //DebugLog.e(TAG, "error:"+c.getInd()+" "+state.curComic.getInd());
        if(cached == state.curComic && !state.curComic.isLoaded()) {
            //DebugLog.e(TAG, "error: errTextNull = "+(errorText == null));
            ViewGroup ll = (ViewGroup) findViewById(R.id.comic_wrapper);
            if(errorText == null) {
                errorText = (TextView) getLayoutInflater().inflate(R.layout.error_text_view, ll, false);
                errorText.setOnClickListener(reloadListener);
                ll.setOnClickListener(reloadListener);
                ll.addView(errorText, 0);
                ll.bringChildToFront(errorText);
            }
            prevEnabled = state.prevComic.isLoaded();
            nextEnabled = state.nextComic.isLoaded();
            error = true;
        }
    }

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
                DebugLog.e("Page selected: "+position);
                if(firstRun) {
                    firstRun = false;
                    return;
                }
                mReaderPagerAdapter.clean(position);
                /* Load the next comic. */
                String newInd = "";
                if(position > state.prevPos) {
                    DebugLog.e("Next page selected");
                    if(state.curComic.getInd() == null && nextEnabled) {
                    } else if(state.curComic.getInd() == null || !nextEnabled || state.curComic.getInd().equals(maxInd) ||
                              state.curComic.getInd().equals(getMax())) {
                        mViewPager.setCurrentItem(state.prevPos);
                        return;
                    }
                    state.prevComic.become(state.curComic);
                    state.curComic.become(state.nextComic);
                    state.nextComic.clear();

                    newInd = state.prevComic.getNextInd();
                    if(state.curComic.isLoaded()) {
                        state.comicMap.put(state.curComic.getNextInd(), state.nextComic);
                        loadComic(state.curComic.getNextInd());
                    }
                    /* Load the previous comic. */
                } else if(position < state.prevPos) {
                    if(state.curComic.getInd() == null && prevEnabled) {
                    } else if(state.curComic.getInd() == null || !prevEnabled || state.curComic.getInd().equals(getFirstInd())) {
                        mViewPager.setCurrentItem(state.prevPos);
                        return;
                    }
                    state.nextComic.become(state.curComic);
                    state.curComic.become(state.prevComic);
                    state.prevComic.clear();

                    newInd = state.nextComic.getPrevInd();
                    if(state.curComic.isLoaded() && !state.curComic.getInd().equals(getFirstInd())) {
                        state.comicMap.put(state.curComic.getPrevInd(), state.prevComic);
                        loadComic(state.curComic.getPrevInd());
                    }
                }
                removeError();
                prevEnabled = true; nextEnabled = true;
                state.comicMap.put(newInd, state.curComic);
                if(state.loadingInds.contains(newInd)) {
                    showLoadingDialog();
                    errorInd = newInd;
                } else if(!state.curComic.isLoaded()) {
                    showLoadingDialog();
                    loadComic(newInd);
                    errorInd = newInd;
                } else {
                    int curItem = mViewPager.getCurrentItem();
                    PageFragment pf = mReaderPagerAdapter.getFragment(curItem);
                    pf.setComic(state.curComic, mViewPager);
                }
                state.prevPos = position;
            }
        };

    /* Adapter for the view pager. Creates and destroys views when necessary. */
    private class ReaderPagerAdapter extends FragmentPagerAdapter {
        boolean freshComics = false;
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

        @Override
        public Object instantiateItem(ViewGroup container, int pos) {
            PageFragment fragment = (PageFragment) super.instantiateItem(container, pos);
            DebugLog.e("FragAdapter instantiating at pos: "+pos);
            fragMap.put(pos, fragment);
            if(freshComics) {
                fragment.setComic(state.curComic, mViewPager);
                freshComics = false;
            }
            return fragment;
        }

        private PageFragment makePageFragment(int pos, Bitmap image) {
            PageFragment ret = PageFragment.newInstance(image);
            fragMap.put(pos, ret);
            return ret;
        }

        private PageFragment makePageFragment(int pos) {
            return makePageFragment(pos, null);
        }

        /* Gets the PageFragment at pos, and sets its image */
        public PageFragment getFragment(int pos, CachedComic comic) {
            PageFragment frag = fragMap.get(pos);
            if(frag == null) {
                frag = makePageFragment(pos, comic.image);
                DebugLog.e("Made fragment with serialized image, mapsize="+fragMap.size());
            } else {
                frag.setComic(comic, mViewPager);
                frag = makePageFragment(pos, comic.image);
                DebugLog.e("Set fragment image directly");
            }
            return frag;
        }

        public PageFragment getFragment(int pos) {
            PageFragment frag = fragMap.get(pos);
            if(frag == null) {
                frag = makePageFragment(pos);
            }
            return frag;
        }

        public void clean(int pos) {
        }

        @Override
        public Fragment getItem(int index) {
            PageFragment frag = fragMap.get(index);
            if(frag == null) {
                frag = makePageFragment(index);
            }
            return frag;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }
    }

    /* Helper for grabbing a random comic */
    public void grabRandomComic(String url) {
        state.clearComics();
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
                    state.handleComicCancel();
                }
            });
        loadingDialog.show();
    }

    /* Handles a click of the prev button. */
    protected OnClickListener prevListener = new OnClickListener() {
            public void onClick(View v) {
                if(prevEnabled && (state.curComic.getInd() != null) && !state.curComic.getInd().equals(getFirstInd())) {
                    mViewPager.setCurrentItem(state.prevPos - 1);
                }
            }
        };

    /* Handles a click of the prev button. */
    protected OnClickListener nextListener = new OnClickListener() {
            public void onClick(View v) {
                String ind = state.curComic.getInd();
                if(nextEnabled && ind != null && !ind.equals(maxInd) && !ind.equals(max)) {
                    mViewPager.setCurrentItem(state.prevPos + 1);
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
        } else if(state.curComic.image != null) {
            try {
                String path = sdPath + "/comics/" + title + "/";
                File dir = new File(path);
                if(!dir.exists())
                    dir.mkdirs();
                String saveTitle;
                if(state.curComic.getImageTitle() != null) {
                    saveTitle = state.curComic.getImageTitle();
                } else if(state.curComic.getInd().equals(maxInd) || state.curComic.getInd().equals(getMax())) {
                    saveTitle = (maxInd == null) ? Integer.toString(maxNum) : maxInd;
                } else {
                    saveTitle = state.curComic.getInd();
                }
                DebugLog.v("comic", dir+" "+saveTitle + ".png");
                File file = new File(dir, saveTitle + ".png");
                FileOutputStream fOut = new FileOutputStream(file);

                state.curComic.image.compress(Bitmap.CompressFormat.PNG, 85, fOut);
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

    /* Shows a cancelable dialog with the given title and text. */
    public void showDialog(String title, Spanned html) {
        final Dialog dialog = createEmptyDialog(title);
        TextView t = (TextView) dialog.findViewById(R.id.alt_text);
        t.setText(html);
        t.setMovementMethod(LinkMovementMethod.getInstance());
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
            state.curComic.setInd(ind);
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
        state.curComic.setNextInd(next);
    }

    /* Sets the previous index of the comic being grabbed. */
    public void setPrevInd(String prev) {
        state.curComic.setPrevInd(prev);
    }

    /* Handles a click of the first button. */
    protected OnClickListener firstListener = new OnClickListener() {
            public void onClick(View v) {
                String firstInd = getFirstInd();
                if(state.curComic.getInd() == null) {
                    showDialog("Error", "Cannot connect to the internet.");
                } else if(state.curComic.getInd() == null || !state.curComic.getInd().equals(firstInd)) {
                    state.clearComics();
                    clearVisible();
                    state.comicMap.put(firstInd, state.curComic);
                    loadComic(firstInd);
                    errorInd = firstInd;
                    showLoadingDialog();
                }
            }
        };

    /* Handles a click of the last button. */
    protected OnClickListener lastListener = new OnClickListener() {
            public void onClick(View v) {
                if(state.curComic.getInd() == null) {
                    showDialog("Error", "Cannot connect to the internet.");
                } else if(state.curComic.getInd() == null || !state.curComic.getInd().equals(maxInd)) {
                    state.clearComics();
                    clearVisible();
                    loadLast(getMax());
                }
            }
        };

    /* To be overidden if the comic id similar, but not exactly equal to the index. */
    public String getIndFromNum(String num) {
        return num;
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

                    state.clearComics();
                    clearVisible();
                    state.comicMap.put(ind, state.curComic);
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
                if(state.curComic.isLoaded()) return;
                if(errorInd != null) {
                    state.comicMap.put(errorInd, state.curComic);
                    loadComic(errorInd);
                    showLoadingDialog();
                } else if(state.curComic.getInd() == null || state.curComic.getInd().equals(getMax())) {
                    state.curComic.setInd(maxInd);
                    state.clearComics();
                    clearVisible();
                    loadLast(getMax());
                } else {
                    state.comicMap.put(state.curComic.getInd(), state.curComic);
                    loadComic(state.curComic.getInd());
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
                    state.clearComics();
                    clearVisible();
                    state.comicMap.put(newInd, state.curComic);
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
            showDialog("About", this.aboutHtml);
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
