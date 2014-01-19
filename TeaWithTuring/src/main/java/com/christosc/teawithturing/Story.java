package com.christosc.teawithturing;

import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Story extends Activity {
// implements ActionBar.TabListener {

//    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
//    ViewPager mViewPager;

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
            mVideoURL, mVideoLocal, mEssayURL, mEssayLocal, mBioURL, mBioLocal;

    /**
     * The integers corresponding to the different tabs of the story
     */
    protected static final int TAB_TEXT = 0;
    protected static final int TAB_VIDEO = 1;
    protected static final int TAB_ESSAY = 2;
    protected static final int TAB_BIO = 3;

    protected static int TEXT_HEIGHT = -1, TEXT_HEIGHT_LAND = -1;
    private static View containerView;

    private static ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_story_tab);
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

        setTitle(mStoryTitle);

        // Check which tabs should be active
        List<Integer> activeTabs = new ArrayList<Integer>();
        if (exists(mTextURL) || exists(mAudioURL)) activeTabs.add(TAB_TEXT);
        if (exists(mVideoURL)) activeTabs.add(TAB_VIDEO);
        if (exists(mEssayURL)) activeTabs.add(TAB_ESSAY);
        if (exists(mBioURL)) activeTabs.add(TAB_BIO);

        // Set up the action bar.
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the sections of the activity.
//        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
//        mViewPager = (ViewPager) findViewById(R.id.pager);
//        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        /*mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // NB Added this extra logic to show the video controllers
                if (activeTabs.get(position) == TAB_VIDEO) StoryVideoFragment.showVideo();
                actionBar.setSelectedNavigationItem(position);
            }
        });*/

        // For each of the sections in the app, add a tab to the action bar.
        /*for (int position : activeTabs) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(position))
                            .setTabListener(this));
        }*/

        for (int tabId : activeTabs) {
            switch (tabId) {
                case TAB_TEXT:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryDetailFragment>(
                                    this, "TAB"+tabId, StoryDetailFragment.class)));
                    break;
                case TAB_VIDEO:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryVideoFragment>(
                                    this, "TAB"+tabId, StoryVideoFragment.class)));
                    break;
                case TAB_ESSAY:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryEssayFragment>(
                                    this, "TAB"+tabId, StoryEssayFragment.class)));
                    break;
                case TAB_BIO:
                    actionBar.addTab(actionBar.newTab()
                            .setText(getPageTitle(tabId))
                            .setTabListener(new StoryTabListener<StoryBioFragment>(
                                    this, "TAB"+tabId, StoryBioFragment.class)));
            }

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
        if (hasFocus)
            resize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resize();
    }

    protected void resize() {
        View textView = findViewById(R.id.story_scroller);
        View audioView = findViewById(R.id.audio_panel);
        if (containerView == null)
            containerView = findViewById(android.R.id.content);
        if (audioView != null) {
            int tempTextHeight;
            if (getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE) {
                if (TEXT_HEIGHT_LAND == -1) {
                    int screenWidth = containerView.getMeasuredWidth();
                    int actionBarHeight = actionBar.getHeight();
                    // If I use tempAudioHeight the text gets clipped
                    tempTextHeight = screenWidth - actionBarHeight;
                    TEXT_HEIGHT_LAND = tempTextHeight;
                }
                else {
                    tempTextHeight = TEXT_HEIGHT_LAND;
                }
            }
            else {
                if (TEXT_HEIGHT == -1) {
                    int tempAudioHeight = audioView.getMeasuredHeight();
                    int screenHeight = containerView.getMeasuredHeight();
                    tempTextHeight = screenHeight - tempAudioHeight;
                    TEXT_HEIGHT = tempTextHeight;
                }
                else {
                    tempTextHeight = TEXT_HEIGHT;
                }
            }
            int textHeight = tempTextHeight;
            Log.d(tag, "Changing height of text fragment to " + textHeight);
            textView.getLayoutParams().height = textHeight;
        }
    }

    //TODO Remove these if satisfied with static tabs without swipe
    /**
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        int position = tab.getPosition();
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            int activeTabPosition = activeTabs.get(position);
            if (activeTabPosition == TAB_TEXT) {
                return StoryDetailFragment.newInstance(mTextURL, mTextLocal,
                        mAudioURL, mAudioLocal, mStoryID);
            }
            else if (activeTabPosition == TAB_VIDEO) {
                return StoryVideoFragment.newInstance(mVideoURL, mVideoLocal);
            }
            else if (activeTabPosition == TAB_ESSAY) {
                return StoryEssayFragment.newInstance(mEssayURL, mEssayLocal, mStoryID);
            }
            else if (activeTabPosition == TAB_BIO) {
                return StoryBioFragment.newInstance(mBioURL, mBioLocal, mStoryID);
            }
            else
                return null;
        }

        @Override
        public int getCount() {
            // Show the number of active tabs depending on the database fields
            return activeTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
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
    }
        */
}
