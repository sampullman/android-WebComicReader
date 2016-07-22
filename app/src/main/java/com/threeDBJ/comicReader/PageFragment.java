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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PageFragment extends Fragment {
    @BindView(R.id.comic) TouchImageView imageView;
    @BindView(R.id.placeholder) TextView placeholder;
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
        if(imageView != null) {
            imageView.setPager(mvp);
            imageView.setImageBitmap(image);
        } else {
            DebugLog.e("Could not set image, view not inflated");
        }
    }

    public void setComic(Comic comic, MyViewPager mvp) {
        Bitmap image = (comic == null) ? null : comic.getComic();
        setImage(image, mvp);
        if(placeholder != null) {
            if(comic != null && image == null) {
                placeholder.setVisibility(View.VISIBLE);
                final String url = ((Reader)getActivity()).getBase() + comic.getInd();
                placeholder.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Intent i = new Intent(Intent.ACTION_VIEW);

                            i.setData(Uri.parse(url));
                            getActivity().startActivity(i);
                        }
                    });
            } else {
                placeholder.setVisibility(View.INVISIBLE);
                placeholder.setOnClickListener(null);
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
        if(imageView != null) {
            imageView.setImageBitmap(null);
            imageView.setPager(null);
            placeholder.setVisibility(View.INVISIBLE);
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state != null) image = state.getParcelable("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_fragment, container, false);
        ButterKnife.bind(this, view);
        vg = (ViewGroup) view;
        if(image != null) {
            DebugLog.e("Page fragment created with bitmap");
            setImage(image, null);
            image = null;
        }
        return view;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
    }
}
