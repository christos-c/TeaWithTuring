package com.christosc.teawithturing;

import android.app.Fragment;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.christosc.teawithturing.data.DataStorage;
import com.christosc.teawithturing.data.StoriesDatabase;
import com.christosc.teawithturing.data.StoriesProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class StoryEssayFragment extends Fragment {
    private ScrollView scrollView;
    private Bundle savedState = null;

    private static String mEssayText;

    private static RetrieveTextTask retrieveTextTask;
    private static View rootView;

    private static String tag = "INFO-ESSAY";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    /*public static StoryEssayFragment newInstance(String remoteText, String localText,
                                                 String storyID) {
        mEssayURL = remoteText;
        mEssayFileLocal = localText;
        mStoryID = storyID;
        if (essayFragment == null) {
            essayFragment = new StoryEssayFragment();
            Bundle args = new Bundle();
            args.putString(Story.ARG_ESSAY_URL, mEssayURL);
            args.putString(Story.ARG_ESSAY_LOCAL, mEssayFileLocal);
            essayFragment.setArguments(args);
        }
        return essayFragment;
    }*/

    public StoryEssayFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "Creating fragment");
        retrieveTextTask = new RetrieveTextTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(tag, "Creating view");
        rootView = inflater.inflate(R.layout.fragment_story_text, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.story_detail);
        scrollView = (ScrollView) rootView.findViewById(R.id.story_scroller);
        String localStoryText;
        if (savedInstanceState != null || savedState != null) {
            if (savedInstanceState == null)
                savedInstanceState = savedState;
            localStoryText = savedInstanceState.getString("mEssayText");
            final int[] position = savedInstanceState.getIntArray("scrollPosition");
            assert position != null;
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.scrollTo(position[0], position[1]);
                }
            });
            textView.setText(localStoryText);
        }
        else {
            retrieveTextTask.execute(Story.mEssayURL, Story.mEssayLocal);
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = new Bundle();
        savedState.putString("mEssayText", mEssayText);
        savedState.putIntArray("scrollPosition",
                new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mEssayText", mEssayText);
        outState.putIntArray("scrollPosition",
                new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
    }

    class RetrieveTextTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            // Shows the loading indicator at start
            rootView.findViewById(R.id.text_loading_progress_bar).setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... urls) {
            String essayText = "";
            try {
                URL url= new URL(urls[0]);
                String local = urls[1];
                if (local != null){
                    Log.d(tag, "Loading from file");
                    essayText = DataStorage.readTextFromFile(local,
                            getActivity().getApplicationContext());
                }
                else {
                    Log.d(tag, "Loading from URL");
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(url.openStream()));
                    String str;
                    while ((str = in.readLine()) != null) {
                        essayText += str+"\n";
                    }
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return essayText;
        }

        @Override
        protected void onCancelled() {
            // Proxy the call to the Activity
        }

        protected void onPostExecute(String text) {
            //Create the name for the local file
            if (Story.mEssayLocal == null){
                Story.mEssayLocal = "TeaWithTuringStory"+Story.mStoryID+"-essay";
                DataStorage.saveTextToFile(text, Story.mEssayLocal,
                        getActivity().getApplicationContext());
                ContentValues values = new ContentValues();
                values.put(StoriesDatabase.StoryEntry.COLUMN_LOCAL_ESSAY, Story.mEssayLocal);
                Uri uri = Uri.withAppendedPath(StoriesProvider.CONTENT_URI, Story.mStoryID);
                assert uri != null;
                getActivity().getContentResolver().update(uri, values, null, null);
            }
            rootView.findViewById(R.id.text_loading_progress_bar).setVisibility(View.GONE);
            TextView textView = (TextView) rootView.findViewById(R.id.story_detail);
            textView.setText(text);
            mEssayText = text;
        }
    }
}
