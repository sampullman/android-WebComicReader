package com.threeDBJ.comicReader.view;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ComicPager extends ViewPager {

    private boolean enabled, swipeEnabled;

    public ComicPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
        this.swipeEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(this.enabled && this.swipeEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(this.enabled && this.swipeEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setSwipeEnabled(boolean swipeEnabled) {
        this.swipeEnabled = swipeEnabled;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}