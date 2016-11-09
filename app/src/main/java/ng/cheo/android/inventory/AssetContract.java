package ng.cheo.android.inventory;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mickey on 7/11/16.
 */

public final class AssetContract {

    public static final String CONTENT_AUTHORITY = "ng.cheo.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ASSETS = "assets";


    // prevent accidental instantiation
    private AssetContract() {}

    public static class AssetEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ASSETS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ASSETS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ASSETS;

        public static final String TABLE_NAME = "assets";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_QUANTITY_SOLD = "quantity";
        public static final String COLUMN_IMAGE = "image";
    }
}
