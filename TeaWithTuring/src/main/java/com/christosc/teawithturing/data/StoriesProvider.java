package com.christosc.teawithturing.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class StoriesProvider extends ContentProvider {
    private StoriesDatabase db;
    private static final String AUTHORITY = "com.christosc.teawithturing.data.StoriesProvider";
    private static final String STORIES_BASE_PATH = "stories";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+STORIES_BASE_PATH);
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/mt-story";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/mt-story";
    public static final int STORIES = 100;
    public static final int STORY_ID = 110;

    @Override
    public boolean onCreate(){
        db = new StoriesDatabase(getContext());
        return true;
    }

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, STORIES_BASE_PATH, STORIES);
        sURIMatcher.addURI(AUTHORITY, STORIES_BASE_PATH + "/#", STORY_ID);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(StoriesDatabase.StoryEntry.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case STORY_ID:
                queryBuilder.appendWhere(StoriesDatabase.StoryEntry._ID
                        + "=" + uri.getLastPathSegment());
                break;
            case STORIES:
                // no filter
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(db.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.moveToFirst();
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        int rowsAffected = 0;
        switch (uriType) {
            case STORIES:
                rowsAffected = sqlDB.delete(StoriesDatabase.StoryEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case STORY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(StoriesDatabase.StoryEntry.TABLE_NAME,
                            StoriesDatabase.StoryEntry._ID + "=" + id, null);
                } else {
                    rowsAffected = sqlDB.delete(StoriesDatabase.StoryEntry.TABLE_NAME,
                            selection + " and " + StoriesDatabase.StoryEntry._ID + "=" + id,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case STORIES:
                return CONTENT_TYPE;
            case STORY_ID:
                return CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        if (uriType != STORIES) {
            throw new IllegalArgumentException("Invalid URI for insert");
        }
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        long newID = sqlDB.insert(StoriesDatabase.StoryEntry.TABLE_NAME, null, values);
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            getContext().getContentResolver().notifyChange(uri, null);
            return newUri;
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = db.getWritableDatabase();

        int rowsAffected;

        switch (uriType) {
            case STORY_ID:
                String id = uri.getLastPathSegment();
                StringBuilder modSelection = new StringBuilder(StoriesDatabase.StoryEntry._ID+"="+id);

                if (!TextUtils.isEmpty(selection)) {
                    modSelection.append(" AND ").append(selection);
                }
                rowsAffected = sqlDB.update(StoriesDatabase.StoryEntry.TABLE_NAME,
                        values, modSelection.toString(), null);
                break;
            case STORIES:
                rowsAffected = sqlDB.update(StoriesDatabase.StoryEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }
}