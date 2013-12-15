package com.christosc.teawithturing.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class StoriesDatabase extends SQLiteOpenHelper{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Stories.db";

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
                    StoryEntry.COLUMN_LOCAL_TEXT + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_LOCAL_AUDIO + TEXT_TYPE + COMMA_SEP +
                    StoryEntry.COLUMN_LOCAL_VIDEO + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StoryEntry.TABLE_NAME;

    public StoriesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        ContentValues values = new ContentValues();
        values.put(StoryEntry.COLUMN_TITLE,"Story about Turing's work #1");
        values.put(StoryEntry.COLUMN_AUTHOR,"Christos Christodouloupoulos");
        values.put(StoryEntry.COLUMN_STORY_TYPE,StoryEntry.TYPE_DESK);
        values.put(StoryEntry.COLUMN_REMOTE_TEXT,"http://christos-c.com/test/test.txt");
        values.put(StoryEntry.COLUMN_REMOTE_AUDIO,"http://christos-c.com/papers/entscheidungsproblem.mp3");
        db.insert(StoryEntry.TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(StoryEntry.COLUMN_TITLE,"Story about Turing's life #1");
        values.put(StoryEntry.COLUMN_AUTHOR,"Christos Christodouloupoulos");
        values.put(StoryEntry.COLUMN_STORY_TYPE,StoryEntry.TYPE_TREES);
        values.put(StoryEntry.COLUMN_REMOTE_TEXT,"http://christos-c.com/test/test.txt");
        values.put(StoryEntry.COLUMN_REMOTE_AUDIO,"http://christos-c.com/papers/entscheidungsproblem.mp3");
        db.insert(StoryEntry.TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(StoryEntry.COLUMN_TITLE,"Story about Turing's work #2");
        values.put(StoryEntry.COLUMN_AUTHOR,"Christos Christodouloupoulos");
        values.put(StoryEntry.COLUMN_STORY_TYPE,StoryEntry.TYPE_DESK);
        values.put(StoryEntry.COLUMN_REMOTE_TEXT,"http://christos-c.com/test/test.txt");
        values.put(StoryEntry.COLUMN_REMOTE_AUDIO,"http://christos-c.com/papers/entscheidungsproblem.mp3");
        db.insert(StoryEntry.TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(StoryEntry.COLUMN_TITLE,"Story about Turing's life #2");
        values.put(StoryEntry.COLUMN_AUTHOR,"Christos Christodouloupoulos");
        values.put(StoryEntry.COLUMN_STORY_TYPE,StoryEntry.TYPE_TREES);
        values.put(StoryEntry.COLUMN_REMOTE_TEXT,"http://christos-c.com/test/test.txt");
        values.put(StoryEntry.COLUMN_REMOTE_AUDIO,"http://christos-c.com/papers/entscheidungsproblem.mp3");
        db.insert(StoryEntry.TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(StoryEntry.COLUMN_TITLE,"Story about tea #1");
        values.put(StoryEntry.COLUMN_AUTHOR,"Christos Christodouloupoulos");
        values.put(StoryEntry.COLUMN_STORY_TYPE,StoryEntry.TYPE_TEA);
        values.put(StoryEntry.COLUMN_REMOTE_TEXT,"http://christos-c.com/test/test.txt");
        values.put(StoryEntry.COLUMN_REMOTE_AUDIO,"http://christos-c.com/papers/entscheidungsproblem.mp3");
        db.insert(StoryEntry.TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(StoryEntry.COLUMN_TITLE,"Story about tea #2");
        values.put(StoryEntry.COLUMN_AUTHOR,"Christos Christodouloupoulos");
        values.put(StoryEntry.COLUMN_STORY_TYPE,StoryEntry.TYPE_TEA);
        values.put(StoryEntry.COLUMN_REMOTE_TEXT,"http://christos-c.com/test/test.txt");
//        values.put(StoryEntry.COLUMN_REMOTE_AUDIO,"http://christos-c.com/papers/entscheidungsproblem.mp3");
        db.insert(StoryEntry.TABLE_NAME, null, values);
    }
}