package com.christosc.teawithturing;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class StoryTabListener<T extends Fragment> implements ActionBar.TabListener {
    private Fragment mFragment, mSecondFragment;
    private final Activity mActivity;
    private final String mTag;
    private final Class<T> mClass;

    private static String tag = "INFO-TABS";

    /** Constructor used each time a new tab is created.
     * @param activity  The host Activity, used to instantiate the fragment
     * @param tag  The identifier tag for the fragment
     * @param clz  The fragment's Class, used to instantiate the fragment
     */
    public StoryTabListener(Activity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
    }

    /* The following are each of the ActionBar.TabListener callbacks */

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            Log.d(tag, "Creating fragment " + mTag);
            mFragment = Fragment.instantiate(mActivity, mClass.getName());
            ft.add(android.R.id.content, mFragment, mTag);
            // Attach the audio tab to the text
            if (mTag.equals("TAB"+Story.TAB_TEXT) && Story.exists(Story.mAudioURL)) {
                mSecondFragment = Fragment.instantiate(mActivity,
                        StoryAudioFragment.class.getName());
                ft.add(android.R.id.content, mSecondFragment, mTag);
            }
        } else {
            // If it exists, simply attach it in order to show it
            Log.d(tag, "Attaching fragment " + mTag);
            ft.attach(mFragment);
            if (mTag.equals("TAB"+Story.TAB_TEXT) && Story.exists(Story.mAudioURL)) {
                // Second fragment should exist
                ft.attach(mSecondFragment);
            }
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        if (mFragment != null) {
            Log.d(tag, "Detaching fragment " + mTag);
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
            if (mTag.equals("TAB"+Story.TAB_TEXT) && Story.exists(Story.mAudioURL)) {
                // Second fragment should exist
                ft.detach(mSecondFragment);
            }
        }
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // User selected the already selected tab. Usually do nothing.
    }
}