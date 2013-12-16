package com.christosc.teawithturing;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Story extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

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

    protected static String mStoryID, mStoryTitle, mTextURL, mTextLocal,
            mAudioURL, mAudioLocal, mVideoURL, mVideoLocal, mEssayURL, mEssayLocal,
            mBioURL, mBioLocal;

    /**
     * The integers corresponding to the different tabs of the story
     */
    private static final int TAB_TEXT = 0;
    private static final int TAB_VIDEO = 1;
    private static final int TAB_ESSAY = 2;
    private static final int TAB_BIO = 3;

    private static List<Integer> activeTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_tab);
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
        activeTabs = new ArrayList<Integer>();
        if (exists(mTextURL, mTextLocal)) activeTabs.add(TAB_TEXT);
        if (exists(mVideoURL, mVideoLocal)) activeTabs.add(TAB_VIDEO);
        if (exists(mEssayURL, mEssayLocal)) activeTabs.add(TAB_ESSAY);
        if (exists(mBioURL, mBioLocal)) activeTabs.add(TAB_BIO);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == TAB_VIDEO) StoryVideoFragment.showVideo();
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int position : activeTabs) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(position))
                            .setTabListener(this));
        }
    }

    protected static boolean exists(String argURL, String argLocal) {
        return ((argURL != null && !argURL.isEmpty()) ||
                (argLocal != null && !argLocal.isEmpty()));
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //TODO change this to activeTabs
            // getItem is called to instantiate the fragment for the given page.
            if (position == TAB_TEXT) {
                return StoryDetailFragment.newInstance();
            }
            else if (position == TAB_VIDEO) {
                return StoryVideoFragment.newInstance();
            }
            else
                //TODO create new Fragments for Essay and Bio
                // Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 4 total pages: Story, Video, Essay and Bio.
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_story_generic, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
