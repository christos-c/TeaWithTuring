package com.christosc.teawithturing;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import com.christosc.teawithturing.data.StoriesDatabase;
import com.christosc.teawithturing.data.StoriesProvider;
import com.christosc.teawithturing.storyScan.StoryScanActivity;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ViewTarget;

public class StoryList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    String[] PROJECTION = { StoriesDatabase.StoryEntry._ID,
            StoriesDatabase.StoryEntry.COLUMN_TITLE,
            StoriesDatabase.StoryEntry.COLUMN_AUTHOR };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO Modify this accordingly (add it to Story.java)
        View showcasedView = findViewById(android.R.id.content);
        ViewTarget target = new ViewTarget(showcasedView);
        ShowcaseView.insertShowcaseView(target, this,
                R.string.showcase_title, R.string.showcase_message);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {StoriesDatabase.StoryEntry.COLUMN_TITLE,
                StoriesDatabase.StoryEntry.COLUMN_AUTHOR};
        int[] toViews = {R.id.story_title, R.id.story_author};

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.activity_list_item, null,
                fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.story_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                //TODO complete this
                return true;
            case R.id.action_scan:
                Intent intent = new Intent(this, StoryScanActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, StoriesProvider.CONTENT_URI,
                PROJECTION, null, null, null);
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent detailIntent = new Intent(this, Story.class);
        Bundle args = getData(String.valueOf(id));
        detailIntent.putExtras(args);
        startActivity(detailIntent);
    }

    private Bundle getData(String storyId){
        Bundle args = new Bundle();
        String projection[] = { StoriesDatabase.StoryEntry._ID,
                StoriesDatabase.StoryEntry.COLUMN_AUTHOR,
                StoriesDatabase.StoryEntry.COLUMN_TITLE,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_TEXT,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_TEXT,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_AUDIO,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_AUDIO,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_VIDEO,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_VIDEO,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_ESSAY,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_ESSAY,
                StoriesDatabase.StoryEntry.COLUMN_REMOTE_BIO,
                StoriesDatabase.StoryEntry.COLUMN_LOCAL_BIO
        };
        Uri uri = Uri.withAppendedPath(StoriesProvider.CONTENT_URI, storyId);
        assert uri != null;
        Cursor storyCursor = getContentResolver().query(uri, projection,
                null, null, null);
        assert storyCursor != null;
        if (storyCursor.moveToFirst()) {
            String storyID = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry._ID));
            args.putString(Story.ARG_STORY_ID, storyID);
            String storyAuthor = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_AUTHOR));
            args.putString(Story.ARG_STORY_AUTHOR, storyAuthor);

            String storyTitle = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_TITLE));
            args.putString(Story.ARG_STORY_TITLE, storyTitle);

            String value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_TEXT));
            if (value != null && !value.equals("")) args.putString(Story.ARG_TEXT_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_TEXT));
            if (value != null && !value.equals("")) args.putString(Story.ARG_TEXT_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_AUDIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_AUDIO_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_AUDIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_AUDIO_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_VIDEO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_VIDEO_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_VIDEO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_VIDEO_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_ESSAY));
            if (value != null && !value.equals("")) args.putString(Story.ARG_ESSAY_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_ESSAY));
            if (value != null && !value.equals("")) args.putString(Story.ARG_ESSAY_LOCAL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_REMOTE_BIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_BIO_URL, value);

            value = storyCursor.getString(
                    storyCursor.getColumnIndex(StoriesDatabase.StoryEntry.COLUMN_LOCAL_BIO));
            if (value != null && !value.equals("")) args.putString(Story.ARG_BIO_LOCAL, value);

        }
        storyCursor.close();
        return args;
    }
}
