package ng.cheo.android.inventory;

import android.graphics.Bitmap;

/**
 * Created by mickey on 7/11/16.
 */

public class Asset {

    private String mName;
    private Double mPrice;
    private Integer mQuantity;
    private Integer mQuantitySold;
    private String mSupplier;
    private String mSupplierPhone;
    private String mSupplierEmail;
    private Bitmap mImage;

    public Asset(String name) {
        mName = name;
        mPrice = 0.0;
        mQuantity = 0;
        mQuantitySold = 0;
        mSupplier = "";
        mSupplierEmail = "";
        mSupplierPhone = "";
    }

    public Asset(String name, Integer quantity, Double price, Bitmap image) {
        mName = name;
        mPrice = price;
        mQuantity = quantity;
        mQuantitySold = 0;
        mSupplier = "";
        mSupplierEmail = "";
        mSupplierPhone = "";
        mImage = image;
    }

    public String getName() {
        return mName;
    }

    public Integer getQuantity() {
        return mQuantity;
    }

    public Double getPrice() {
        return mPrice;
    }

    public Integer getQuantitySold() {
        return mQuantitySold;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public byte[] getImageInBytes() {
        return ImageUtil.getBytes(mImage);
    }
}
