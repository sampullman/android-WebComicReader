package com.threeDBJ.comicReader;

import com.google.ads.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.net.Uri;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import android.util.Log;

import java.util.Random;
import java.io.File;

public class SavedComicReader extends FragmentActivity {

    public static final String PREFS_NAME = "ComicPrefsFile";

    File[] images;
    int cur, storeIndex;
    Random rand;
    String sdPath, title;
    boolean swipe = false;

    MyViewPager mViewPager;
    ReaderPagerAdapter mReaderPagerAdapter;

    static final String[] storeUrls = new String[] { "http://smbc.myshopify.com/", "http://store.xkcd.com/",
						     "http://store.explosm.net/", "http://feelafraidcomic.com/store/",
						     "http://www.phdcomics.com/store/mojostore.php", "http://buttersafe.com/store/",
						     "http://www.splitreason.com/cad-comic/", "http://store.penny-arcade.com/",
						     "http://www.cafepress.com/abstrusegoose", "http://www.dreamhost.com/donate.cgi?id=13906",
						     "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BAT2CLMHSCR36&lc=CA&currency_code=CAD&bn=PP-DonationsBF:btn_donateCC_LG.gif:NonHosted",
						     "http://webcomic.mongreldesigns.com/p/support.html" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	Configuration config = this.getResources().getConfiguration();
	if(config.orientation == 1) {
            setContentView(R.layout.reader);
	} else if(config.orientation == 2) {
            setContentView(R.layout.reader_wide);
	}
	mViewPager = (MyViewPager) findViewById(R.id.reader_pager);
	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	setSwipe(settings.getBoolean("swipe", false));

	Intent intent = getIntent();
	this.title = intent.getExtras().getString("comic");
	rand = new Random();

	this.storeIndex = intent.getExtras().getInt("index");
	setupUI();
	if(Reader.isStorageWritable()) {
	    String path = sdPath + "/comics/" + title + "/";
	    Log.e("scr", path);
	    File dir = new File(path);
	    if (dir != null) {
		images = dir.listFiles();
		if(images == null || images.length == 0) {
		    setResult(1);
		    finish();
		    return;
		}
	    } else {
		setResult(1);
		finish();
		return;
	    }
	} else {
	    setResult(2);
	    finish();
	    return;
	}

	setResult(0);
	Log.v("scr", "" + images.length);
	mViewPager.setOnPageChangeListener(pageListener);
	mReaderPagerAdapter = new ReaderPagerAdapter(getSupportFragmentManager(), images.length);
	mViewPager.setAdapter(mReaderPagerAdapter);
	cur = images.length - 1;
	mViewPager.setCurrentItem(cur);
    }

    OnPageChangeListener pageListener = new OnPageChangeListener() {

	    @Override
	    public void onPageScrollStateChanged (int state) {
	    }
	    @Override
	    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	    }

	    @Override
	    public void onPageSelected (int position) {
		Log.v("onPageSelected", "" + position);
	    }
	};

    private class ReaderPagerAdapter extends FragmentPagerAdapter {

	private int count;

	public ReaderPagerAdapter(FragmentManager fm, int count) {
	    super(fm);
	    this.count = count;
	}

        @Override
	public Fragment getItem(int index) {
	    Log.v("getItem", "" + index);
	    PageFragment ret;
	    Bitmap image = BitmapFactory.decodeFile(images[index].getAbsolutePath());
	    ret = PageFragment.newInstance(image);
	    return ret;
	}

        @Override
	public int getCount() {
	    return count;
	}
    }

    public void setSwipe(boolean on) {
	swipe = on;
	mViewPager.setSwipeEnabled(on);
	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	SharedPreferences.Editor editor = settings.edit();
	editor.putBoolean("swipe", swipe);
	editor.commit();
    }

    public OnClickListener prevListener = new OnClickListener() {
	    public void onClick(View v) {
		if(cur > 0) {
		    cur -= 1;
		    mViewPager.setCurrentItem(cur);
		}
	    }
	};

    public OnClickListener nextListener = new OnClickListener() {
	    public void onClick(View v) {
		if(cur < images.length - 1) {
		    cur += 1;
		    mViewPager.setCurrentItem(cur);
		}
	    }
	};


    public OnClickListener firstListener = new OnClickListener() {
	    public void onClick(View v) {
		mViewPager.setCurrentItem(0);
	    }
	};

    public OnClickListener lastListener = new OnClickListener() {
	    public void onClick(View v) {
		mViewPager.setCurrentItem(images.length - 1);
	    }
	};

    public OnClickListener randomListener = new OnClickListener() {
	    public void onClick(View v) {
		if(images.length > 1) {
		    mViewPager.setCurrentItem(rand.nextInt(images.length));
		}
	    }
	};

    protected OnClickListener storeListener = new OnClickListener() {
            public void onClick(View v) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(storeUrls[storeIndex]));
		startActivity(i);
            }
        };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_menu, menu);
        return true;
    }

    public void setupUI() {
	Button b = (Button) findViewById(R.id.comic_first);
	b.setOnClickListener(firstListener);
	b = (Button) findViewById(R.id.comic_prev);
	b.setOnClickListener(prevListener);
	b = (Button) findViewById(R.id.comic_next);
	b.setOnClickListener(nextListener);
	b = (Button) findViewById(R.id.comic_last);
	b.setOnClickListener(lastListener);
	b = (Button) findViewById(R.id.comic_random);
	b.setOnClickListener(randomListener);
	b = (Button) findViewById(R.id.comic_store);
	b.setOnClickListener(storeListener);
	b = (Button) findViewById(R.id.comic_alt);
	b.setVisibility(View.GONE);

	sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

}