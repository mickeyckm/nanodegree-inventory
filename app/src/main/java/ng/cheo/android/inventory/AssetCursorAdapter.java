package ng.cheo.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import static ng.cheo.android.inventory.R.id.quantity_sold;


/**
 * Created by mickey on 8/11/16.
 */

public class AssetCursorAdapter extends CursorAdapter {

    private final static String LOG_TAG = AssetCursorAdapter.class.getSimpleName();
    public AssetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.asset_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView quantitySoldTextView = (TextView) view.findViewById(quantity_sold);
//        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        int nameColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry.COLUMN_NAME);
        int quantityColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry.COLUMN_QUANTITY);
        int quantitySoldColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry.COLUMN_QUANTITY_SOLD);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry.COLUMN_PRICE);
        int imageColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry.COLUMN_IMAGE);

        nameTextView.setText(cursor.getString(nameColumnIndex));
        quantityTextView.setText(Integer.toString(cursor.getInt(quantityColumnIndex)) + " left");
        quantitySoldTextView.setText(Integer.toString(cursor.getInt(quantitySoldColumnIndex)) + " sold");

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        byte[] image = cursor.getBlob(imageColumnIndex);
        imageView.setImageBitmap(ImageUtil.getImage(image));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        Double price = cursor.getDouble(priceColumnIndex);
        saleButton.setText(context.getText(R.string.sale_for) + " " + formatter.format(price));

        int idColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry._ID);
        saleButton.setTag(cursor.getInt(idColumnIndex));
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = (int) v.getTag();
                Uri currentAssetUri = ContentUris.withAppendedId(AssetContract.AssetEntry.CONTENT_URI, id);

                String[] projection = {
                    AssetContract.AssetEntry._ID,
                    AssetContract.AssetEntry.COLUMN_NAME,
                    AssetContract.AssetEntry.COLUMN_QUANTITY,
                    AssetContract.AssetEntry.COLUMN_QUANTITY_SOLD,
                    AssetContract.AssetEntry.COLUMN_PRICE,
                    AssetContract.AssetEntry.COLUMN_IMAGE
                };

                Cursor cursor = v.getContext().getContentResolver().query(currentAssetUri, projection, null, null, null);
                cursor.moveToFirst();

                int quantityColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry.COLUMN_QUANTITY);
                int quantitySoldColumnIndex = cursor.getColumnIndexOrThrow(AssetContract.AssetEntry.COLUMN_QUANTITY_SOLD);
                int quantity = cursor.getInt(quantityColumnIndex);
                int quantitySold = cursor.getInt(quantitySoldColumnIndex);

                if (quantity > 0) {
                    ContentValues values = new ContentValues();
                    values.put(AssetContract.AssetEntry.COLUMN_QUANTITY, quantity - 1);
                    values.put(AssetContract.AssetEntry.COLUMN_QUANTITY_SOLD, quantitySold + 1);
                    v.getContext().getContentResolver().update(currentAssetUri, values, null, null);

                    Toast.makeText(v.getContext(),
                            v.getContext().getString(R.string.sale_success),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(v.getContext(),
                            v.getContext().getString(R.string.sale_failed),
                            Toast.LENGTH_SHORT).show();
                }

                cursor.close();
            }
        });
    }
}
