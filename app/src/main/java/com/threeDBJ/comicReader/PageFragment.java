package com.threeDBJ.comicReader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.threeDBJ.comicReader.reader.Reader;
import com.threeDBJ.comicReader.view.ComicPager;
import com.threeDBJ.comicReader.view.TouchImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PageFragment extends Fragment {
    @BindView(R.id.comic) TouchImageView imageView;
    @BindView(R.id.placeholder) TextView placeholder;
    ViewGroup vg;
    Bitmap image;

    public static PageFragment newInstance(Bitmap image) {
        PageFragment pageFragment = new PageFragment();
        //Bundle bundle = new Bundle();
        //bundle.putParcelable("image", image);
        //pageFragment.setArguments(bundle);
        pageFragment.image = image;
        return pageFragment;
    }

    public void setImage(Bitmap image, ComicPager mvp) {
        if(imageView != null) {
            imageView.setPager(mvp);
            imageView.setImageBitmap(image);
        } else {
            Timber.e("Could not set image, view not inflated");
        }
    }

    public void setComic(Comic comic, ComicPager mvp) {
        Bitmap image = (comic == null) ? null : comic.getComic();
        setImage(image, mvp);
        if(placeholder != null) {
            if(comic != null && image == null) {
                placeholder.setVisibility(View.VISIBLE);
                final String url = ((Reader) getActivity()).getBase() + comic.getInd();
                placeholder.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);

                        i.setData(Uri.parse(url));
                        if(getActivity() != null) {
                            getActivity().startActivity(i);
                        }
                    }
                });
            } else {
                placeholder.setVisibility(View.INVISIBLE);
                placeholder.setOnClickListener(null);
            }
        }
    }

    public void setComic(Comic comic) {
        ComicPager mvp;
        try {
            mvp = ((Reader) getActivity()).getViewPager();
        } catch(Exception e) {
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

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state != null) image = state.getParcelable("image");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_fragment, container, false);
        ButterKnife.bind(this, view);
        vg = (ViewGroup) view;
        if(image != null) {
            Timber.d("Page fragment created with bitmap");
            setImage(image, null);
            image = null;
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
