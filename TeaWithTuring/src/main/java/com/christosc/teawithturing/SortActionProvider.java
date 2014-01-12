package com.christosc.teawithturing;

import android.content.Context;
import android.util.Log;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class SortActionProvider extends ActionProvider
        implements MenuItem.OnMenuItemClickListener {

    private static String tag = "INFO-SORT_ACTION";
    public static final int SORT_TITLE = 0;
    public static final int SORT_NAME = 1;
    public static final int SORT_MARKER = 2;

    /** Context for accessing resources. */
    private final Context mContext;

    public SortActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        // Inflate the action view to be shown on the action bar.
            /*LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.sort_action_provider, null);
            // Attach a click listener for launching the system settings.
            view.findViewById(R.id.sortByNameButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StoryList.sortBy("name");
                }
            });
            view.findViewById(R.id.sortByTitleButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StoryList.sortBy("title");
                }
            });
            view.findViewById(R.id.sortByMarkerButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StoryList.sortBy("marker");
                }
            });
            return view;*/
        return null;
    }

    @Override
    public boolean hasSubMenu(){
        return true;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu){
        subMenu.clear();
        subMenu.add(0, SORT_TITLE, Menu.NONE, R.string.button_sort_by_title);
        subMenu.add(0, SORT_NAME, Menu.NONE, R.string.button_sort_by_name);
        subMenu.add(0, SORT_MARKER, Menu.NONE, R.string.button_sort_by_marker);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item){
        Log.d(tag, "Item ID: " + item.getItemId());
        switch (item.getItemId()) {
            case SORT_TITLE:
                StoryList.sortBy("title");
                return true;
            case SORT_NAME:
                StoryList.sortBy("name");
                return true;
            case SORT_MARKER:
                StoryList.sortBy("marker");
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onPerformDefaultAction() {
        // This is called if the host menu item placed in the overflow menu of the
        // action bar is clicked and the host activity did not handle the click.
        // TODO What should I put here?
        Log.d(tag, "Default action");
        return true;
    }
}