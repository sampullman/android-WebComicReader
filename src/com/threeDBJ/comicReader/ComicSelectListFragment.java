package com.threeDBJ.comicReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import android.support.v4.app.ListFragment;

import com.threeDBJ.comicReader.ComicArrayAdapter.ComicListClicker;

public class ComicSelectListFragment extends ListFragment implements ComicListClicker {
    public static final String[] comicTitles =
        new String[] { "SMBC", "XKCD", "Cyanide & Happiness", "Questionable Content", "Dinosaur Comics",
                       "PHD Comics", "Buttersafe", "Ctrl Alt Delete", "Penny Arcade",
                       "Abstruse Goose", "Manly Guys", "Completely Serious", "Mongrel Designs" };
    public static final String[] savedTitles =
        new String[] { "SMBC", "XKCD", "Explosm", "Questionable Content", "Dinosaur Comics",
                       "PHD", "Buttersafe", "CtrlAltDelete", "PennyArcade",
                       "Abstruse Goose", "Manly Guys Doing Manly Things", "Completely Serious", "Mongrel Designs" };
    public static final Class[] comicClasses =
        new Class[] { SMBCReader.class, XKCDReader.class, ExplosmReader.class,
                      QuestionableContentReader.class, DinosaurReader.class, PHDReader.class,
                      ButtersafeReader.class, CtrlAltDeleteReader.class, PennyArcadeReader.class,
                      AbstruseGooseReader.class, ManlyGuysReader.class, CompletelySeriousReader.class,
                      MongrelDesignsReader.class };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ComicArrayAdapter adapter = new ComicArrayAdapter(getActivity(), comicTitles);
        adapter.setListClicker(this);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent;
        Activity activity = getActivity();
        intent = new Intent(activity, comicClasses[position]);
        intent.putExtra("load_last_viewed", false);
        activity.startActivityForResult(intent, 0);
    }

    @Override
    public void onSavedClick(int position) {
        Intent intent;
        Activity activity = getActivity();
        intent = new Intent(activity, SavedComicReader.class);
        intent.putExtra("comic", savedTitles[position]);
        intent.putExtra("index", position);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onLastViewedClick(int position) {
        Intent intent;
        Activity activity = getActivity();
        intent = new Intent(activity, comicClasses[position]);
        intent.putExtra("load_last_viewed", true);
        activity.startActivityForResult(intent, 0);
    }

}