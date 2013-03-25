package com.threeDBJ.comicReader;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ComicArrayAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] names;
    private ComicListClicker listClicker;

    static class ViewHolder {
        public TextView text;
        public ImageButton saved;
        public ImageButton last;
    }

    public static interface ComicListClicker {
        public void onSavedClick(int position);
        public void onLastViewedClick(int position);
    }

    public ComicArrayAdapter(Activity context, String[] names) {
        super(context, R.layout.comic_row, names);
        this.context = context;
        this.names = names;
    }

    public void setListClicker(ComicListClicker clicker) {
        this.listClicker = clicker;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.comic_row, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.comic_title);
            viewHolder.saved = (ImageButton) rowView.findViewById(R.id.comic_saved);
            viewHolder.last = (ImageButton) rowView.findViewById(R.id.comic_last);
            rowView.setTag(viewHolder);
        }
        final int rowNum = position;
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.text.setText(names[position]);
        holder.saved.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if(listClicker != null) {
                        listClicker.onSavedClick(rowNum);
                    }
                }
            });
        holder.last.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if(listClicker != null) {
                        listClicker.onLastViewedClick(rowNum);
                    }
                }
            });

        return rowView;
    }
} 