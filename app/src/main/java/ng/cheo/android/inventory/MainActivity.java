package ng.cheo.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ng.cheo.android.inventory.AssetContract.AssetEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView mListView;
    AssetCursorAdapter mAdapter;

    private static final int ASSET_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Setup adapter
        mAdapter = new AssetCursorAdapter(this, null);

        // Setup list view
        mListView = (ListView) findViewById(R.id.assets);
        mListView.setAdapter(mAdapter);

        // Setup empty view
        View emptyView = findViewById(R.id.empty_view);
        mListView.setEmptyView(emptyView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentAssetUri = ContentUris.withAppendedId(AssetEntry.CONTENT_URI, id);
                intent.setData(currentAssetUri);
                startActivity(intent);
            }
        });

        // Setup loader
        getLoaderManager().initLoader(ASSET_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertAsset();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllAssets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertAsset() {

        ArrayList<Asset> assets = new ArrayList<>();
        assets.add(new Asset("Sofa", 2, 1000.00, BitmapFactory.decodeResource(getResources(), R.raw.sofa)));
        assets.add(new Asset("Piano", 1, 4500.00, BitmapFactory.decodeResource(getResources(), R.raw.piano)));
        assets.add(new Asset("Chair", 3, 40.00, BitmapFactory.decodeResource(getResources(), R.raw.chair)));
        assets.add(new Asset("Dining Table", 1, 270.00, BitmapFactory.decodeResource(getResources(), R.raw.dining_table)));
        assets.add(new Asset("Bookshelf", 2, 125.00, BitmapFactory.decodeResource(getResources(), R.raw.bookshelf)));
        assets.add(new Asset("Side Table", 1, 70.00, BitmapFactory.decodeResource(getResources(), R.raw.side_table)));
        assets.add(new Asset("Table Lamp", 1, 35.00, BitmapFactory.decodeResource(getResources(), R.raw.table_lamp)));
        assets.add(new Asset("Vintage Television", 1, 869.00, BitmapFactory.decodeResource(getResources(), R.raw.tv)));
        assets.add(new Asset("Working Table", 1, 579.00, BitmapFactory.decodeResource(getResources(), R.raw.working_table)));

        for (Asset asset : assets) {
            ContentValues values = new ContentValues();
            values.put(AssetEntry.COLUMN_NAME, asset.getName());
            values.put(AssetEntry.COLUMN_PRICE, asset.getPrice());
            values.put(AssetEntry.COLUMN_QUANTITY, asset.getQuantity());
            values.put(AssetEntry.COLUMN_IMAGE, asset.getImageInBytes());

            Uri newUri = getContentResolver().insert(AssetEntry.CONTENT_URI, values);
        }
    }

    private void deleteAllAssets() {
        int rowsDeleted = getContentResolver().delete(AssetEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                AssetEntry._ID,
                AssetEntry.COLUMN_NAME,
                AssetEntry.COLUMN_QUANTITY,
                AssetEntry.COLUMN_QUANTITY_SOLD,
                AssetEntry.COLUMN_PRICE,
                AssetEntry.COLUMN_IMAGE
        };

        return new CursorLoader(this,
                AssetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
