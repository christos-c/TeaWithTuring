package com.christosc.teawithturing;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Story extends Activity {

    public static final String tag = "INFO-STORY";

    public static final String ARG_STORY_ID = "story_id";
    public static final String ARG_STORY_TITLE = "story_title";
    public static final String ARG_STORY_AUTHOR = "story_author";
    public static final String ARG_TEXT_URL = "text_url";
    public static final String ARG_TEXT_LOCAL = "text_local";
    public static final String ARG_AUDIO_URL = "audio_url";
    public static final String ARG_AUDIO_LOCAL = "audio_local";
    public static final String ARG_VIDEO_URL = "video_url";
    public static final String ARG_VIDEO_LOCAL = "video_local";
    public static final String ARG_ESSAY_URL = "essay_url";
    public static final String ARG_ESSAY_LOCAL = "essay_local";
    public static final String ARG_BIO_URL = "bio_url";
    public static final String ARG_BIO_LOCAL = "bio_local";

    protected static String mStoryID;
    protected static String mStoryTitle, mTextURL, mTextLocal, mAudioURL, mAudioLocal,
            mVideoURL, mVideoLocal, mEssayURL, mEssayLocal, mBioURL, mBioLocal, mStoryAuthor;

    /**
     * The integers corresponding to the different tabs of the story
     */
    protected static final int TAB_TEXT = 0;
    protected static final int TAB_VIDEO = 1;
    protected static final int TAB_ESSAY = 2;
    protected static final int TAB_BIO = 3;

    protected static int TEXT_HEIGHT = -1, TEXT_HEIGHT_LAND = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStoryID = getData(ARG_STORY_ID, savedInstanceState);
        mStoryTitle = getData(ARG_STORY_TITLE, savedInstanceState);
        mStoryAuthor = getData(ARG_STORY_AUTHOR, savedInstanceState);
        mTextLocal = getData(ARG_TEXT_LOCAL, savedInstanceState);
        mTextURL = getData(ARG_TEXT_URL, savedInstanceState);
        mAudioURL = getData(ARG_AUDIO_URL, savedInstanceState);
        mAudioLocal = getData(ARG_AUDIO_LOCAL, savedInstanceState);
        mVideoURL = getData(ARG_VIDEO_URL, savedInstanceState);
        mVideoLocal = getData(ARG_VIDEO_LOCAL, savedInstanceState);
        mEssayURL = getData(ARG_ESSAY_URL, savedInstanceState);
        mEssayLocal = getData(ARG_ESSAY_LOCAL, savedInstanceState);
        mBioURL = getData(ARG_BIO_URL, savedInstanceState);
        mBioLocal = getData(ARG_BIO_LOCAL, savedInstanceState);

        ActionBar ab = getActionBar();
        ab.setTitle(mStoryTitle);
        ab.setSubtitle("by " + mStoryAuthor);

        // Check which tabs should be active
        List<Integer> activeTabs = new ArrayList<Integer>();
        if (exists(mTextURL) || exists(mAudioURL)) activeTabs.add(TAB_TEXT);
        if (exists(mVideoURL)) activeTabs.add(TAB_VIDEO);
        if (exists(mEssayURL)) activeTabs.add(TAB_ESSAY);
        if (exists(mBioURL)) activeTabs.add(TAB_BIO);

        // Set up the action bar.
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for (int tabId : activeTabs) {
            switch (tabId) {
                case TAB_TEXT:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryDetailFragment>(
                                    this, "TAB" + tabId, StoryDetailFragment.class)));
                    break;
                case TAB_VIDEO:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryVideoFragment>(
                                    this, "TAB" + tabId, StoryVideoFragment.class)));
                    break;
                case TAB_ESSAY:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryEssayFragment>(
                                    this, "TAB" + tabId, StoryEssayFragment.class)));
                    break;
                case TAB_BIO:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryBioFragment>(
                                    this, "TAB" + tabId, StoryBioFragment.class)));
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (StoryAudioFragment.mediaPlayer != null) {
            if (StoryAudioFragment.mediaPlayer.isPlaying())
                StoryAudioFragment.mediaPlayer.stop();
            StoryAudioFragment.mediaPlayer.release();
            StoryAudioFragment.mediaPlayer = null;
        }
        if (StoryVideoFragment.mMediaPlayer != null) {
            if (StoryVideoFragment.mMediaPlayer.isPlaying())
                StoryVideoFragment.mMediaPlayer.stop();
            StoryVideoFragment.mMediaPlayer.release();
            StoryVideoFragment.mMediaPlayer = null;
        }
    }

    private String getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case TAB_TEXT:
                return getString(R.string.title_section_text).toUpperCase(l);
            case TAB_VIDEO:
                return getString(R.string.title_section_video).toUpperCase(l);
            case TAB_ESSAY:
                return getString(R.string.title_section_essay).toUpperCase(l);
            case TAB_BIO:
                return getString(R.string.title_section_bio).toUpperCase(l);
        }
        return null;
    }

    protected static boolean exists(String argURL) {
        return (argURL != null && !argURL.isEmpty());
    }

    @Override
    public void onSaveInstanceState(Bundle outBundle){
        super.onSaveInstanceState(outBundle);
        outBundle.putString(ARG_STORY_ID, mStoryID);
        outBundle.putString(ARG_STORY_TITLE, mStoryTitle);
        outBundle.putString(ARG_TEXT_LOCAL, mTextLocal);
        outBundle.putString(ARG_TEXT_URL, mTextURL);
        outBundle.putString(ARG_AUDIO_URL, mAudioURL);
        outBundle.putString(ARG_AUDIO_LOCAL, mAudioLocal);
        outBundle.putString(ARG_VIDEO_URL, mVideoURL);
        outBundle.putString(ARG_VIDEO_LOCAL, mVideoLocal);
        outBundle.putString(ARG_ESSAY_URL, mEssayURL);
        outBundle.putString(ARG_ESSAY_LOCAL, mEssayLocal);
        outBundle.putString(ARG_BIO_URL, mBioURL);
        outBundle.putString(ARG_BIO_LOCAL, mBioLocal);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mStoryID = getData(ARG_STORY_ID, savedInstanceState);
        mStoryTitle = getData(ARG_STORY_TITLE, savedInstanceState);
        mTextLocal = getData(ARG_TEXT_LOCAL, savedInstanceState);
        mTextURL = getData(ARG_TEXT_URL, savedInstanceState);
        mAudioURL = getData(ARG_AUDIO_URL, savedInstanceState);
        mAudioLocal = getData(ARG_AUDIO_LOCAL, savedInstanceState);
        mVideoURL = getData(ARG_VIDEO_URL, savedInstanceState);
        mVideoLocal = getData(ARG_VIDEO_LOCAL, savedInstanceState);
        mEssayURL = getData(ARG_ESSAY_URL, savedInstanceState);
        mEssayLocal = getData(ARG_ESSAY_LOCAL, savedInstanceState);
        mBioURL = getData(ARG_BIO_URL, savedInstanceState);
        mBioLocal = getData(ARG_BIO_LOCAL, savedInstanceState);

    }

    private String getData(String name, Bundle savedInstanceState){
        if (savedInstanceState != null) return savedInstanceState.getString(name);
        else return getIntent().getStringExtra(name);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Log.d(tag, "onWindowFocusChanged");
            resize();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(tag, "onConfigurationChanged");
        resize();
    }

    public void resize() {
        View audioView = findViewById(R.id.audio_panel);
        if (audioView == null) return;
        int audioHeight = 0;
        int screenHeight = 0;
        int actionBarHeight = 0;

        if (TEXT_HEIGHT == -1 || TEXT_HEIGHT_LAND == -1) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
            audioHeight = audioView.getMeasuredHeight();
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                        getResources().getDisplayMetrics());
            }
        }

        int textHeight;
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE) {
            if (TEXT_HEIGHT_LAND == -1)
                TEXT_HEIGHT_LAND = (int) (screenHeight - audioHeight - (Math.round(1.6*actionBarHeight)));
            textHeight = TEXT_HEIGHT_LAND;
        }
        else {
            if (TEXT_HEIGHT == -1)
                TEXT_HEIGHT = (int) (screenHeight - audioHeight - (Math.round(2.3*actionBarHeight)));
            textHeight = TEXT_HEIGHT;
        }
        Log.d(tag, "Changing height of text fragment to " + textHeight);
        View textView = findViewById(R.id.story_scroller);
        textView.getLayoutParams().height = textHeight;
        textView.requestLayout();
    }
}
