package com.christosc.teawithturing.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.christosc.teawithturing.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class StoriesDatabase extends SQLiteOpenHelper{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Stories.db";
    private static Context appContext;

    public static abstract class StoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "stories";
        /** This corresponds to the markers (either desk, trees, tea) */
        public static final String COLUMN_STORY_TYPE = "storytype";
        public static final String TYPE_TREES = "trees";
        public static final String TYPE_DESK = "desk";
        public static final String TYPE_TEA = "tea";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_REMOTE_TEXT = "remotetext";
        public static final String COLUMN_REMOTE_AUDIO = "remoteaudio";
        public static final String COLUMN_REMOTE_VIDEO = "remotevideo";
        public static final String COLUMN_LOCAL_TEXT = "localtext";
        public static final String COLUMN_LOCAL_AUDIO = "localaudio";
        public static final String COLUMN_LOCAL_VIDEO = "localvideo";
        public static final String COLUMN_REMOTE_ESSAY = "remoteessay";
        public static final String COLUMN_LOCAL_ESSAY = "localessay";
        public static final String COLUMN_REMOTE_BIO = "remotebio";
        public static final String COLUMN_LOCAL_BIO = "localbio";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + StoryEntry.TABLE_NAME + " (" +
                    StoryEntry._ID + " INTEGER PRIMARY KEY," +
                    StoryEntry.COLUMN_STORY_TYPE + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_AUTHOR + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_REMOTE_TEXT + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_REMOTE_AUDIO + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_REMOTE_VIDEO + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_REMOTE_ESSAY + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_REMOTE_BIO + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_LOCAL_TEXT + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_LOCAL_AUDIO + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_LOCAL_VIDEO + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_LOCAL_ESSAY + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_LOCAL_BIO + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StoryEntry.TABLE_NAME;

    public StoriesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        appContext = context;
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        seedData(db);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void seedData(SQLiteDatabase db){
        InputStream inputStream;
        inputStream = appContext.getResources().openRawResource(R.raw.stories);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        ContentValues values = new ContentValues();
        try {
            while ((line = in.readLine()) != null) {
                // This is a new entry
                if (line.isEmpty()) {
                    if (values.size() > 1)
                        db.insert(StoryEntry.TABLE_NAME, null, values);
                    values.clear();
                }
                else {
                    String[] splits = line.split("\t");
                    String key = splits[0];
                    String value = (splits.length > 1 ? splits[1] : null);
                    if (key.equals("TITLE") && value != null)
                        values.put(StoryEntry.COLUMN_TITLE, splits[1]);
                    else if (key.equals("AUTHOR") && value != null)
                        values.put(StoryEntry.COLUMN_AUTHOR, splits[1]);
                    else if (key.equals("TYPE") && value != null)
                        values.put(StoryEntry.COLUMN_STORY_TYPE, splits[1]);
                    else if (key.equals("TEXT") && value != null)
                        values.put(StoryEntry.COLUMN_REMOTE_TEXT, splits[1]);
                    else if (key.equals("AUDIO") && value != null)
                        values.put(StoryEntry.COLUMN_REMOTE_AUDIO, splits[1]);
                    else if (key.equals("VIDEO") && value != null)
                        values.put(StoryEntry.COLUMN_REMOTE_VIDEO, splits[1]);
                    else if (key.equals("ESSAY") && value != null)
                        values.put(StoryEntry.COLUMN_REMOTE_ESSAY, splits[1]);
                    else if (key.equals("BIO") && value != null)
                        values.put(StoryEntry.COLUMN_REMOTE_BIO, splits[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}