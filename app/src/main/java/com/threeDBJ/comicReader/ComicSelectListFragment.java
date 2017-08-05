package com.threeDBJ.comicReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.threeDBJ.comicReader.reader.*;

public class ComicSelectListFragment extends ListFragment {
    public static final int EXTERNAL=0, SAVED=1;

    ComicArrayAdapter adapter;
    private int type = EXTERNAL;

    public static final String[] comicTitles =
            new String[]{"SMBC", "XKCD", "Cyanide & Happiness", "Questionable Content", "Dinosaur Comics",
                    "PHD Comics", "Buttersafe", "Ctrl Alt Delete", "Penny Arcade",
                    "Abstruse Goose", "Manly Guys", "Completely Serious", "Mongrel Designs"};
    public static final String[] savedTitles =
            new String[]{"SMBC", "XKCD", "Explosm", "Questionable Content", "Dinosaur Comics",
                    "PHD", "Buttersafe", "CtrlAltDelete", "PennyArcade",
                    "Abstruse Goose", "Manly Guys Doing Manly Things", "Completely Serious", "Mongrel Designs"};
    public static final Class[] comicClasses =
            new Class[]{SMBCReader.class, XKCDReader.class, ExplosmReader.class,
                    QuestionableContentReader.class, DinosaurReader.class, PHDReader.class,
                    ButtersafeReader.class, CtrlAltDeleteReader.class, PennyArcadeReader.class,
                    AbstruseGooseReader.class, ManlyGuysReader.class, CompletelySeriousReader.class,
                    MongrelDesignsReader.class};

    public static ComicSelectListFragment newInstance(int type) {
        ComicSelectListFragment fragment = new ComicSelectListFragment();
        Bundle args = new Bundle();
        args.putInt("comic_type", type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        adapter = new ComicArrayAdapter(getActivity(), comicTitles);
        Bundle b = getArguments();
        if(b != null) {
            this.type = b.getInt("comic_type", EXTERNAL);
        }

        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(type == SAVED) {
            savedComicsCount();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent;
        Activity activity = getActivity();

        if(type == SAVED) {
            intent = new Intent(activity, SavedComicReader.class);
            intent.putExtra("comic", savedTitles[position]);
            intent.putExtra("index", position);
        } else {
            intent = new Intent(activity, comicClasses[position]);
            intent.putExtra("load_last_viewed", true);
        }
        activity.startActivityForResult(intent, type);
    }

    void savedComicsCount() {
        int counts[] = new int[comicTitles.length];
        for(int i=0; i < comicTitles.length; i += 1) {
            counts[i] = 0;
        }
        adapter.setComicCounts(counts);
    }

    class ComicArrayAdapter extends ArrayAdapter<String> {
        private final Activity context;
        private final String[] names;
        private int[] comicCount; // For saved Comics

        class ViewHolder {
            public TextView title;
            public TextView count;
        }

        public ComicArrayAdapter(Activity context, String[] names) {
            super(context, R.layout.comic_row, names);
            this.context = context;
            this.names = names;
        }

        public void setComicCounts(int[] counts) {
            comicCount = counts;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if(rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.comic_row, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.title = (TextView) rowView.findViewById(R.id.comic_title);
                viewHolder.count = (TextView) rowView.findViewById(R.id.comic_row_text_right);
                rowView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.title.setText(names[position]);
            if(comicCount != null) {
                holder.count.setText(Integer.toString(comicCount[position]));
            }

            return rowView;
        }
    }

}
