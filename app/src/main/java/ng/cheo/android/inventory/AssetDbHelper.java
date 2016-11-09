package ng.cheo.android.inventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mickey on 7/11/16.
 */

import ng.cheo.android.inventory.AssetContract.AssetEntry;

public class AssetDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = AssetDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public AssetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_ASSETS_TABLE =  "CREATE TABLE " + AssetEntry.TABLE_NAME + " ("
                + AssetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AssetEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + AssetEntry.COLUMN_PRICE + " DOUBLE NOT NULL DEFAULT 0.0, "
                + AssetEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                + AssetEntry.COLUMN_QUANTITY_SOLD + " INTEGER NOT NULL DEFAULT 0,"
                + AssetEntry.COLUMN_IMAGE + " BLOB NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ASSETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Still at version 1, nothing to be done here.
    }
}
