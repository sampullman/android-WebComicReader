package com.threeDBJ.comicReader.reader;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.threeDBJ.comicReader.CachedComic;
import com.threeDBJ.comicReader.Comic;
import com.threeDBJ.comicReader.ComicReaderApp;
import com.threeDBJ.comicReader.ComicReaderApp.ComicState;
import com.threeDBJ.comicReader.view.ComicPager;
import com.threeDBJ.comicReader.PageFragment;
import com.threeDBJ.comicReader.R;
import com.threeDBJ.comicReader.RequestManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public abstract class Reader extends AppCompatActivity {
    public static final String PREFS_NAME = "ComicPrefsFile";
    ComicReaderApp app;
    ComicState state;
    @BindView(R.id.toolbar) Toolbar toolBar;
    @BindView(R.id.reader_pager) ComicPager viewPager;
    @BindView(R.id.comic_alt) Button altButton;

    String prevPat, nextPat, imgPat, base, max, errorInd,
        firstInd="1", maxInd, storeUrl,	sdPath, title, shortTitle;
    Pattern pImages, pPrev, pNext, pMax, pAlt;

    Spanned aboutHtml = Html.fromHtml(aboutText);
    static final String aboutText = "Created by 3DBJ developers. Please email questions, comments, or concerns to "+
        "<a href=\"mailto:3dbj.dev@gmail.com\">3dbj.dev@gmail.com</a>";

    int maxNum=-1;

    EditText selectEdit;
    Dialog selectDialog;
    TextView errorText;

    int errorCount=0;
    boolean firstRun=false, error=false, swipe=false,
        nextEnabled=true, prevEnabled=false, loadLastViewed;

    ReaderPagerAdapter readerPagerAdapter;

    RequestManager rm;

    ProgressDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (ComicReaderApp)getApplicationContext();
        app.setReader(this);
        state = app.getComicState();
        rm = app.getRequestManager();

        setContentView(R.layout.reader);
        ButterKnife.bind(this);
        this.UISetup();
        toolBar.setTitle(title);
        setSupportActionBar(toolBar);

        sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        this.loadLastViewed = getIntent().getBooleanExtra("load_last_viewed", false);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        setSwipe(settings.getBoolean("swipe", false));
    }

    /* Set up click listeners and initialize the view pager */
    public void UISetup() {
        altButton.setVisibility(View.GONE);
        firstRun = true;
        readerPagerAdapter = new ReaderPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(readerPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(pageListener);
        viewPager.setCurrentItem(state.setPager);
        viewPager.setCurrentItem(state.setPager);
        /*
          PageFragment frag = readerPagerAdapter.getFragment(state.setPager);
          if(state.curComic != null && frag != null) {
          frag.setComic(state.curComic, viewPager);
          }
        */
    }

    public ComicPager getViewPager() {
        return this.viewPager;
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
            edit.apply();
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
        Timber.d("Comic Reader Destroyed");
    }

    public void loadInitial(String url) {
        if(loadLastViewed) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String lastInd = prefs.getString(shortTitle, "");
            lastInd = lastInd == null ? "" : lastInd;

            if(lastInd.equals(state.curComic.getInd())) {
                readerPagerAdapter.freshComics = true;
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
        Timber.d("Loading last: freshComic=%b, loading=%b, running=%d, curComic null=%b",
            freshComic, state.isLoading(), rm.running, (state.curComic.getComic()==null));

        if(freshComic && state.curComic.getComic() != null) {
            //notifyComicLoaded(state.curComic);
            Timber.d("Loading last comic image");
            readerPagerAdapter.freshComics = true;
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
        Timber.d("Notified of comic load, cur? %b", (cached == state.curComic));
        if(cached == state.curComic) {
            errorInd = null;
            if(loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            PageFragment frag = readerPagerAdapter.getFragment(viewPager.getCurrentItem());
            if(frag == null) {
                state.handleComicCancel();
            } else {
                frag.setComic(cached, viewPager);
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
        //Timber.d("error: %d %d", c.getInd(), state.curComic.getInd());
        if(cached == state.curComic && !state.curComic.isLoaded()) {
            //Timber.d("error: errTextNull = %b", (errorText == null));
            ViewGroup ll = findViewById(R.id.comic_wrapper);
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
                Timber.d("Page selected: %d", position);
                if(firstRun) {
                    firstRun = false;
                    return;
                }
                /* Load the next comic. */
                String newInd = "";
                if(position > state.prevPos) {
                    Timber.d("Next page selected");
                    if(state.curComic.getInd() == null && nextEnabled) {
                        Timber.d("Current index is null, next is enabled");

                    } else if(state.curComic.getInd() == null || !nextEnabled || state.curComic.getInd().equals(maxInd) ||
                              state.curComic.getInd().equals(getMax())) {

                        viewPager.setCurrentItem(state.prevPos);
                        return;
                    }
                    newInd = state.transitionToNext();

                    if(state.curComic.isLoaded()) {
                        state.comicMap.put(state.curComic.getNextInd(), state.nextComic);
                        loadComic(state.curComic.getNextInd());
                    }
                    /* Load the previous comic. */
                } else if(position < state.prevPos) {
                    if(state.curComic.getInd() == null && prevEnabled) {
                        Timber.d("Current index is null, prev is enabled");

                    } else if(state.curComic.getInd() == null || !prevEnabled || state.curComic.getInd().equals(getFirstInd())) {

                        viewPager.setCurrentItem(state.prevPos);
                        return;
                    }

                    newInd = state.transitionToPrev();
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
                    int curItem = viewPager.getCurrentItem();
                    PageFragment pf = readerPagerAdapter.getFragment(curItem);
                    pf.setComic(state.curComic, viewPager);
                }
                state.prevPos = position;
            }
        };

    /* Adapter for the view pager. Creates and destroys views when necessary. */
    private class ReaderPagerAdapter extends FragmentPagerAdapter {
        boolean freshComics = false;
        HashMap<Integer, PageFragment> fragMap = new HashMap<>();

        ReaderPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
            PageFragment frag = fragMap.remove(position);
            if(frag != null) {
                frag.clean();
            }
        }

        void clearImages() {
            for(PageFragment frag : fragMap.values()) {
                frag.setComic(null, viewPager);
            }
        }

        @Override @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int pos) {
            PageFragment fragment = (PageFragment) super.instantiateItem(container, pos);
            Timber.d("FragAdapter instantiating at pos: %d", pos);
            fragMap.put(pos, fragment);
            if(freshComics) {
                fragment.setComic(state.curComic, viewPager);
                freshComics = false;
            }
            return fragment;
        }

        private PageFragment makePageFragment(int pos) {
            PageFragment ret = PageFragment.newInstance(null);
            fragMap.put(pos, ret);
            return ret;
        }

        PageFragment getFragment(int pos) {
            PageFragment frag = fragMap.get(pos);
            if(frag == null) {
                frag = makePageFragment(pos);
            }
            return frag;
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
    @OnClick(R.id.comic_prev)
    public void prevClicked() {
        if(prevEnabled && (state.curComic.getInd() != null) && !state.curComic.getInd().equals(getFirstInd())) {
            viewPager.setCurrentItem(state.prevPos - 1);
        }
    }

    /* Handles a click of the next button. */
    @OnClick(R.id.comic_next)
    public void nextClicked() {
        String ind = state.curComic.getInd();
        if(nextEnabled && (state.curComic.getInd() != null) && !ind.equals(maxInd) && !ind.equals(max)) {
            viewPager.setCurrentItem(state.prevPos + 1);
        }
    }

    /* Handles a click of the prev button. */
    @OnClick(R.id.comic_last)
    public void lastClicked() {
        String ind = state.curComic.getInd();
        if(nextEnabled && ind != null && !ind.equals(maxInd) && !ind.equals(max)) {
            viewPager.setCurrentItem(state.prevPos + 1);
        }
    }

    /* Sets a listener for the alt text/image of a comic strip. */
    public void setAltListener(OnClickListener altListener) {
        altButton.setVisibility(View.VISIBLE);
        altButton.setOnClickListener(altListener);
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
        selectEdit = selectDialog.findViewById(R.id.select_input);

        Button b = selectDialog.findViewById(R.id.select_enter);
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
                if(!dir.exists() && !dir.mkdirs()) {
                    Timber.d("Could not create the save folder");
                }
                String saveTitle;
                if(state.curComic.getImageTitle() != null) {
                    saveTitle = state.curComic.getImageTitle();
                } else if(state.curComic.getInd().equals(maxInd) || state.curComic.getInd().equals(getMax())) {
                    saveTitle = (maxInd == null) ? Integer.toString(maxNum) : maxInd;
                } else {
                    saveTitle = state.curComic.getInd();
                }
                Timber.v("SD Save: %s/%s.png", dir.getAbsolutePath(), saveTitle);
                File file = new File(dir, saveTitle + ".png");
                FileOutputStream fOut = new FileOutputStream(file);

                state.curComic.image.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();
            } catch(Exception e) {

                if(e.getMessage() != null) {
                    Timber.v("SDError %s", e.getMessage());
                }
                showDialog("Warning", "Comic could not be saved.");
            }
        } else {
            /* Warn that comic could not be saved */
            Timber.v("SDError: bitmap was null :(");
            showDialog("Warning", "Comic could not be saved.");
        }
    }

    /* Creats a dialog and returns it's body TextView to fill */
    public Dialog createEmptyDialog(String title) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.text_alt);
        dialog.setTitle(title);
        dialog.setCancelable(true);
        Button done = dialog.findViewById(R.id.alt_done);
        done.setOnClickListener(makeDialogCancelListener(dialog));
        return dialog;
    }

    /* Shows a cancelable dialog with the given title and text. */
    public void showDialog(String title, CharSequence text) {
        final Dialog dialog = createEmptyDialog(title);
        TextView t = dialog.findViewById(R.id.alt_text);
        t.setText(text);
        t.setMovementMethod(LinkMovementMethod.getInstance());
        dialog.show();
    }

    /* Displays alt text for a comic. */
    public void dispAltText(String text, String title) {
        if(text != null) {
            final Dialog dialog = createEmptyDialog(title);
            TextView t = dialog.findViewById(R.id.alt_text);
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
        Button done = dialog.findViewById(R.id.alt_done);
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
        if(maxNum == -1) {
            this.maxNum = (int) Double.parseDouble(ind);
        }
    }

    /* Returns whether the max has been recorded. */
    public boolean noMax() {
        return maxNum == -1 && maxInd == null;
    }

    /* Sets the next index of the comic being grabbed. */
    public void setNextInd(String next) {
        state.curComic.setNextInd(next);
    }

    /* Sets the previous index of the comic being grabbed. */
    public void setPrevInd(String prev) {
        state.curComic.setPrevInd(prev);
    }

    void freshComic(String ind) {
        state.clearComics();
        clearVisible();
        state.comicMap.put(ind, state.curComic);
        loadComic(ind);
        errorInd = ind;
        showLoadingDialog();
    }

    /* Handles a click of the first button. */
    @OnClick(R.id.comic_first)
    public void firstClicked() {
        String firstInd = getFirstInd();
        if(state.curComic.getInd() == null) {
            showDialog("Error", "Cannot connect to the internet.");
        } else if(!state.curComic.getInd().equals(firstInd)) {
            freshComic(firstInd);
        }
    }

    /* Handles a click of the last button. */
    @OnClick(R.id.comic_last)
    public void lastClicked(View v) {
        if(state.curComic.getInd() == null) {
            showDialog("Error", "Cannot connect to the internet.");
        } else if(!state.curComic.getInd().equals(maxInd)) {
            state.clearComics();
            clearVisible();
            loadLast(getMax());
        }
    }

    /* Override if the comic id similar, but not exactly equal to the index. */
    public String getIndFromNum(String num) {
        return num;
    }

    /* Clears visible comic */
    public void clearVisible() {
        readerPagerAdapter.clearImages();
    }

    /* General click listener for getting a random comic.
       To be used when the comic as integer indices that increment by 1. */
    @OnClick(R.id.comic_random)
    public void randomClicked() {
        if(maxNum != -1) {
            int n = (int)(Math.random() * maxNum);
            String ind = getIndFromNum(Integer.toString(n));

            freshComic(ind);
        } else {
            showDialog("Error", "Could not resolve random comic.");
        }
    }

    /* Listener for the store button. Opens a browser window with the strip's store. */
    @OnClick(R.id.comic_store)
    public void storeClicked(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(storeUrl));
        startActivity(i);
    }

    public void removeError() {
        ViewGroup ll = findViewById(R.id.comic_wrapper);
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
                    freshComic(newInd);
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
        viewPager.setSwipeEnabled(on);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Editor editor = settings.edit();
        editor.putBoolean("swipe", swipe);
        editor.apply();
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
        MenuItem item = menu.findItem(R.id.comic_swipe);
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
