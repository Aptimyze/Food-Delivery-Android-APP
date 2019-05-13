package edu.monash.assignment3.Model;

public class Order {

    private String ProductId;
    private String ProductName;
    private String Quantity;
    private String Price;
    private String Discount;

    //create db
    public static final String TABLE_NAME = "OrderDetail";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_PRODUCTID ="ProductId";
    public static final String COLUMN_PRODUCTNAME = "ProductName";
    public static final String COLUMN_QUANTITY = "Quantity";
    public static final String COLUMN_PRICE = "Price";
    public static final String COLUMN_DISCOUNT = "Discount";

    public static final String CREATE_STATEMENT = "CREATE TABLE " + TABLE_NAME +
            "("+COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
            COLUMN_PRODUCTID+" TEXT, " + COLUMN_PRODUCTNAME+" TEXT, " +
            COLUMN_QUANTITY+" TEXT, " + COLUMN_PRICE+" TEXT, " +
            COLUMN_DISCOUNT+" TEXT" +
            ")";

    public Order() {
    }

    public Order(String productId, String productName, String quantity, String price, String discount) {
        ProductId = productId;
        ProductName = productName;
        Quantity = quantity;
        Price = price;
        Discount = discount;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }
}
