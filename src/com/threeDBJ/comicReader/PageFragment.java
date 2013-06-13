package com.threeDBJ.comicReader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class PageFragment extends Fragment {
    ViewGroup vg;

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
	if(vg != null) {
	    TouchImageView imageView = (TouchImageView) vg.findViewById(R.id.comic);
	    imageView.setPager(mvp);
	    imageView.setImageBitmap(image);
	}
    }

    public void setComic(Comic comic, MyViewPager mvp) {
	Bitmap image = (comic == null) ? null : comic.getComic();
	setImage(image, mvp);
	if(vg != null) {
	    TextView text = (TextView)vg.findViewById(R.id.placeholder);
	    if(comic != null && image == null) {
		text.setVisibility(View.VISIBLE);
		final String url = ((Reader)getActivity()).getBase() + comic.getInd();
		text.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			    Intent i = new Intent(Intent.ACTION_VIEW);
			    
			    i.setData(Uri.parse(url));
			    getActivity().startActivity(i);
			}
		    });
	    } else {
		text.setVisibility(View.INVISIBLE);
		text.setOnClickListener(null);
	    }
	}
    }

    public void setComic(Comic comic) {
	MyViewPager mvp;
	try {
	    mvp = ((Reader)getActivity()).mViewPager;
	} catch (Exception e) {
	    mvp = null;
	}
	setComic(comic, mvp);
    }

    public void clean() {
	if(vg != null) {
	    TouchImageView comic = (TouchImageView) vg.findViewById(R.id.comic);
	    comic.setImageBitmap(null);
	    comic.setPager(null);
	    TextView text = (TextView)vg.findViewById(R.id.placeholder);
	    text.setVisibility(View.INVISIBLE);
	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.page_fragment, container, false);
	vg = (ViewGroup) view;
	Bitmap image = (Bitmap)getArguments().getParcelable("image");
	if(image != null) {
	    setImage(image, null);
	}
	return view;
    }
}