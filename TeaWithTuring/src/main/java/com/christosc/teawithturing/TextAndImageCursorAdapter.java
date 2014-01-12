package com.christosc.teawithturing;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.christosc.teawithturing.data.StoriesDatabase;

public class TextAndImageCursorAdapter extends SimpleCursorAdapter {
    private LayoutInflater mLayoutInflater;
    private int layout;

    private class ViewHolder {
        TextView titleView, authorView;
        ImageView markerView;

        ViewHolder(View v) {
            titleView = (TextView) v.findViewById(R.id.story_title);
            authorView = (TextView) v.findViewById(R.id.story_author);
            markerView = (ImageView) v.findViewById(R.id.marker_image);
        }
    }

    public TextAndImageCursorAdapter(Context context, int layout, Cursor c,
                                     String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
        View vView = mLayoutInflater.inflate(layout, parent, false);
        vView.setTag( new ViewHolder(vView) );
        return vView;
        // no need to bind data here. you do in later
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        int colTitle = c.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_TITLE);
        int colAuthorSurname = c.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_AUTHOR_SURNAME);
        int colAuthorName = c.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_AUTHOR_NAME);
        int colMarker = c.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_STORY_TYPE);

        String titleText = c.getString(colTitle);
        String authorText = c.getString(colAuthorName) + " " + c.getString(colAuthorSurname);
        String markerText = c.getString (colMarker);

        ViewHolder vh = (ViewHolder) v.getTag();

        vh.titleView.setText(titleText);
        vh.authorView.setText(authorText);

        assert markerText != null;

        if (markerText.equals("desk"))
            vh.markerView.setImageResource(R.drawable.marker_desk);
        else if (markerText.equals("trees"))
            vh.markerView.setImageResource(R.drawable.marker_trees);
        else if (markerText.equals("tea"))
            vh.markerView.setImageResource(R.drawable.marker_tea);
    }
}
