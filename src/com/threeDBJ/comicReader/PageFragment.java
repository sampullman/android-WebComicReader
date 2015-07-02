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

import com.threeDBJ.comicReader.reader.Reader;

public class PageFragment extends Fragment {
    ViewGroup vg;
    Bitmap image;

    public static PageFragment newInstance(Bitmap image) {
        DebugLog.e("Made PageFragment");
        PageFragment pageFragment = new PageFragment();
        //Bundle bundle = new Bundle();
        //bundle.putParcelable("image", image);
        //pageFragment.setArguments(bundle);
        pageFragment.image = image;
        return pageFragment;
    }

    public static PageFragment newInstance() {
        return newInstance(null);
    }

    public void setImage(Bitmap image, MyViewPager mvp) {
        if(vg != null) {
            TouchImageView imageView = (TouchImageView) vg.findViewById(R.id.comic);
            imageView.setPager(mvp);
            imageView.setImageBitmap(image);
        } else {
            DebugLog.e("Could not set image, view not inflated");
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
            mvp = ((Reader)getActivity()).getViewPager();
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
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state != null) image = state.getParcelable("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_fragment, container, false);
        vg = (ViewGroup) view;
        if(image != null) {
            DebugLog.e("Page fragment created with bitmap");
            setImage(image, null);
            image = null;
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
