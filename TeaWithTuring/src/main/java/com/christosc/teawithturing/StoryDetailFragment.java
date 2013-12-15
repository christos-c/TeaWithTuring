package com.christosc.teawithturing;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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

/**
* Created by Chris on 14/12/13.
*/
public class StoryDetailFragment extends Fragment {
    private ScrollView scrollView;
    private Bundle savedState = null;
    private StoryAudioFragment audioFragment;
    private static String audioFragmentTag = "audio_frag";
    private static StoryDetailFragment detailFragment;

    private static String storyText;

    private static RetrieveTextTask retrieveTextTask;
    private static View rootView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StoryDetailFragment newInstance() {
        if (detailFragment == null) {
            detailFragment = new StoryDetailFragment();
            Bundle args = new Bundle();
            args.putString(Story.ARG_TEXT_URL, Story.mTextURL);
            args.putString(Story.ARG_TEXT_LOCAL, Story.mTextLocal);
            detailFragment.setArguments(args);
        }
        return detailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveTextTask = new RetrieveTextTask();
        audioFragment = new StoryAudioFragment();
        Bundle args = new Bundle();
        args.putString(Story.ARG_AUDIO_URL, Story.mAudioURL);
        args.putString(Story.ARG_AUDIO_LOCAL, Story.mAudioLocal);
        args.putString(Story.ARG_STORY_ID, Story.mStoryID);
        audioFragment.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_story_text, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.story_detail);
        scrollView = (ScrollView) rootView.findViewById(R.id.story_scroller);
        FragmentManager fragmentManager = getFragmentManager();
        Fragment f = fragmentManager.findFragmentByTag(audioFragmentTag);
        if (f == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.story_detail_container, audioFragment, audioFragmentTag);
            transaction.commit();
        }
        String localStoryText;
        if (savedInstanceState != null || savedState != null) {
            if (savedInstanceState == null)
                savedInstanceState = savedState;
            localStoryText = savedInstanceState.getString("storyText");
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
            Bundle args = getArguments();
            retrieveTextTask.execute(args.getString(Story.ARG_TEXT_URL),
                    args.getString(Story.ARG_TEXT_LOCAL));
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = new Bundle();
        savedState.putString("storyText", storyText);
        savedState.putIntArray("scrollPosition",
                new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("storyText", storyText);
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
            String storyText = "";
            try {
                URL url= new URL(urls[0]);
                String local = urls[1];
                if (local != null){
                    Log.d("TEXT-LOAD", "Loading from file");
                    storyText = DataStorage.readTextFromFile(local,
                            getActivity().getApplicationContext());
                }
                else {
                    Log.d("TEXT-LOAD", "Loading from URL");
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(url.openStream()));
                    String str;
                    while ((str = in.readLine()) != null) {
                        storyText += str+"\n";
                    }
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return storyText;
        }

        @Override
        protected void onCancelled() {
            // Proxy the call to the Activity
        }

        protected void onPostExecute(String text) {
            //Create the name for the local file
            if (Story.mTextLocal == null){
                Story.mTextLocal = "TeaWithTuringStory"+Story.mStoryID+"-text";
                DataStorage.saveTextToFile(text, Story.mTextLocal,
                        getActivity().getApplicationContext());
                ContentValues values = new ContentValues();
                values.put(StoriesDatabase.StoryEntry.COLUMN_LOCAL_TEXT, Story.mTextLocal);
                Uri uri = Uri.withAppendedPath(StoriesProvider.CONTENT_URI, Story.mStoryID);
                assert uri != null;
                getActivity().getContentResolver().update(uri, values, null, null);
            }
            rootView.findViewById(R.id.text_loading_progress_bar).setVisibility(View.GONE);
            TextView textView = (TextView) rootView.findViewById(R.id.story_detail);
            textView.setText(text);
            storyText = text;
        }
    }
}
