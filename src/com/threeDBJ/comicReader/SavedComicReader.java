package com.threeDBJ.comicReader;

import com.google.ads.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
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

public class SavedComicReader extends Reader {

    File[] images;
    int storeIndex;
    Random rand;

    ViewPager mViewPager;
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
	Intent intent = getIntent();
	this.title = intent.getExtras().getString("comic");
	rand = new Random();

	this.storeIndex = intent.getExtras().getInt("index");
	if(isStorageWritable()) {
	    String path = sdPath + "/comics/" + title + "/";
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
	mViewPager = (ViewPager) findViewById(R.id.reader_pager);
	mViewPager.setOnPageChangeListener(pageListener);
	mReaderPagerAdapter = new ReaderPagerAdapter(getSupportFragmentManager(), images.length);
	mViewPager.setAdapter(mReaderPagerAdapter);
	mViewPager.setCurrentItem(images.length - 1);
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
		mViewPager.setCurrentItem(rand.nextInt(images.length));
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

    public void setup() {
	rm = new RequestManager();

	Button b = (Button) findViewById(R.id.comic_first);
	b.setOnClickListener(firstListener);
	b = (Button) findViewById(R.id.comic_last);
	b.setOnClickListener(lastListener);
	b = (Button) findViewById(R.id.comic_random);
	b.setOnClickListener(randomListener);
	b = (Button) findViewById(R.id.comic_store);
	b.setOnClickListener(storeListener);
	b = (Button) findViewById(R.id.comic_alt);
	b.setVisibility(View.GONE);
	//b.setVisibility(View.INVISIBLE);

	sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

}