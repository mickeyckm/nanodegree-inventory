package ng.cheo.android.inventory;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.app.LoaderManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int EXISTING_ASSET_LOADER = 0;

    private ImageView mImageView;
    private Button mImportImage;
    private EditText mNameEditText;
    private TextView mQuantityText;
    private TextView mQuantitySoldText;
    private Button mQuantityIncreaseButton;
    private Button mQuantityDecreaseButton;
    private EditText mPriceEditText;

    private Uri mCurrentAssetUri;
    private boolean mAssetHasChanged = false;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get intent data
        Intent intent = getIntent();
        mCurrentAssetUri = intent.getData();

        // Setup fields
        mImageView = (ImageView) findViewById(R.id.edit_image);
        mImportImage = (Button) findViewById(R.id.import_image);
        mImportImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mQuantityText = (TextView) findViewById(R.id.qty_value);
        mQuantityIncreaseButton = (Button) findViewById(R.id.qty_increase_button);
        mQuantityIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView quantityText = (TextView) findViewById(R.id.qty_value);
                Integer quantity = Integer.valueOf(quantityText.getText().toString());
                quantityText.setText(Integer.toString(quantity + 1));
            }
        });

        mQuantityDecreaseButton = (Button) findViewById(R.id.qty_decrease_button);
        mQuantityDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView quantityText = (TextView) findViewById(R.id.qty_value);
                Integer quantity = Integer.valueOf(quantityText.getText().toString());
                if (quantity > 0) {
                    quantityText.setText(Integer.toString(quantity - 1));
                }
            }
        });

        mQuantitySoldText = (TextView) findViewById(R.id.qty_sold_value);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);


        // Display based on create/update
        if (mCurrentAssetUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_asset));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_asset));
            getLoaderManager().initLoader(EXISTING_ASSET_LOADER, null, this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentAssetUri == null) {
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            orderMenuItem.setVisible(false);
            MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
            deleteMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveAsset();
                return true;
            // Respond to a click on the "Save" menu option
            case R.id.action_order:
                orderAsset();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mAssetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mAssetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    // Loader methods

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                AssetContract.AssetEntry._ID,
                AssetContract.AssetEntry.COLUMN_IMAGE,
                AssetContract.AssetEntry.COLUMN_NAME,
                AssetContract.AssetEntry.COLUMN_QUANTITY,
                AssetContract.AssetEntry.COLUMN_QUANTITY_SOLD,
                AssetContract.AssetEntry.COLUMN_PRICE };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, mCurrentAssetUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {

            int imageColumnIndex = data.getColumnIndex(AssetContract.AssetEntry.COLUMN_IMAGE);
            int nameColumnIndex = data.getColumnIndex(AssetContract.AssetEntry.COLUMN_NAME);
            int quantityColumnIndex = data.getColumnIndex(AssetContract.AssetEntry.COLUMN_QUANTITY);
            int quantitySoldColumnIndex = data.getColumnIndex(AssetContract.AssetEntry.COLUMN_QUANTITY_SOLD);
            int priceColumnIndex = data.getColumnIndex(AssetContract.AssetEntry.COLUMN_PRICE);

            byte[] image = data.getBlob(imageColumnIndex);
            String name = data.getString(nameColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            int quantitySold = data.getInt(quantitySoldColumnIndex);
            double price = data.getDouble(priceColumnIndex);

            mImageView.setImageBitmap(ImageUtil.getImage(image));
            mNameEditText.setText(name);
            mQuantityText.setText(Integer.toString(quantity));
            mQuantitySoldText.setText(Integer.toString(quantitySold));
            mPriceEditText.setText(Double.toString(price));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityText.setText("0");
        mQuantitySoldText.setText("0");
        mPriceEditText.setText("");
    }


    // Dialogs

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAsset();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Private methods

    private void saveAsset() {

        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        Bitmap imageBitmap = null;

        if (mImageView.getDrawable() != null) {
            imageBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        }

        if (mCurrentAssetUri == null && (TextUtils.isEmpty(nameString) ||  TextUtils.isEmpty(priceString) || imageBitmap == null)) {
            if (TextUtils.isEmpty(nameString) ) {
                Toast.makeText(this, getString(R.string.edit_name_blank), Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(priceString) ) {
                Toast.makeText(this, getString(R.string.edit_price_blank), Toast.LENGTH_SHORT).show();
            }
            else if (imageBitmap == null ) {
                Toast.makeText(this, getString(R.string.edit_image_blank), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ContentValues values = new ContentValues();
        values.put(AssetContract.AssetEntry.COLUMN_NAME, nameString);
        values.put(AssetContract.AssetEntry.COLUMN_QUANTITY, quantityString);
        values.put(AssetContract.AssetEntry.COLUMN_PRICE, priceString);
        values.put(AssetContract.AssetEntry.COLUMN_IMAGE, ImageUtil.getBytes(imageBitmap));

        if (mCurrentAssetUri == null) {
            Uri newUri = getContentResolver().insert(AssetContract.AssetEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_asset_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_asset_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentAssetUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_asset_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_asset_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void deleteAsset() {
        if (mCurrentAssetUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentAssetUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_asset_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_asset_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void orderAsset() {
        if (mCurrentAssetUri != null) {
            String assetName = mNameEditText.getText().toString();
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","supplier@inventory.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.supplier_subject) + assetName);
            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.supplier_body_one) + assetName + getString(R.string.supplier_body_two));
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }
    }
}
