package ng.cheo.android.inventory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import ng.cheo.android.inventory.AssetContract.AssetEntry;

/**
 * Created by mickey on 7/11/16.
 */

public class AssetProvider extends ContentProvider {

    public static final String LOG_TAG = AssetProvider.class.getSimpleName();

    private static final int ASSETS = 100;
    private static final int ASSET_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AssetContract.CONTENT_AUTHORITY, AssetContract.PATH_ASSETS, ASSETS);
        sUriMatcher.addURI(AssetContract.CONTENT_AUTHORITY, AssetContract.PATH_ASSETS + "/#", ASSET_ID);
    }

    private AssetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new AssetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ASSETS:
                cursor = database.query(AssetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ASSET_ID:
                selection = AssetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(AssetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ASSETS:
                return AssetEntry.CONTENT_LIST_TYPE;
            case ASSET_ID:
                return AssetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case ASSETS:
                return insertAsset(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int numAffectedRows;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ASSETS:
                // Delete all rows that match the selection and selection args
                numAffectedRows = database.delete(AssetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ASSET_ID:
                // Delete a single row given by the ID in the URI
                selection = AssetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                numAffectedRows = database.delete(AssetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Notify all listeners that the data has changed for asset content URI
        if (numAffectedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numAffectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ASSETS:
                return updateAsset(uri, values, selection, selectionArgs);
            case ASSET_ID:
                selection = AssetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateAsset(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private Uri insertAsset(Uri uri, ContentValues values) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(AssetEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Asset requires a name");
        }

        long id = db.insert(AssetEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for asset content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private int updateAsset(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(AssetEntry.COLUMN_NAME)) {
            String name = values.getAsString(AssetEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Asset requires a name");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int numAffectedRows = database.update(AssetEntry.TABLE_NAME, values, selection, selectionArgs);

        // Notify all listeners that the data has changed for asset content URI
        if (numAffectedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numAffectedRows;
    }
}
