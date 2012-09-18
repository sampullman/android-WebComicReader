package com.threeDBJ.comicReader;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.util.Log;

public class PageFragment extends Fragment {

    ViewGroup ll;

    public static PageFragment newInstance(Bitmap image) {
	PageFragment pageFragment = new PageFragment();
	Bundle bundle = new Bundle();
	bundle.putParcelable("image", image);
	pageFragment.setArguments(bundle);
	return pageFragment;
    }

    public static PageFragment newInstance() {
	return newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    }

    public void setImage(Bitmap image, MyViewPager mvp) {
	if(ll != null) {
	    TouchImageView comic = (TouchImageView) ll.getChildAt(0);
	    comic.setPager(mvp);
	    comic.setImageBitmap(image);
	}
    }

    public void setImage(Bitmap image) {
	MyViewPager mvp;
	try {
	    mvp = ((Reader)getActivity()).mViewPager;
	} catch (Exception e) {
	    mvp = null;
	}
	setImage(image, mvp);
    }

    public void clean() {
	if(ll != null) {
	    TouchImageView comic = (TouchImageView) ll.getChildAt(0);
	    comic.setImageBitmap(null);
	    comic.setPager(null);
	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.page_fragment, container, false);
	ll = (ViewGroup) view;
	Bitmap image = (Bitmap)getArguments().getParcelable("image");
	if(image != null) {
	    setImage(image);
	}
	return view;
    }
}