package edu.monash.assignment3.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;


import java.util.ArrayList;
import java.util.List;

import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Order;

public class Database extends SQLiteOpenHelper {

    private static final String DB_NAME = "TakeOutDB";
    private static final int DB_VERSION = 1;

    public Database(Context context) {
        super(context, DB_NAME,null,DB_VERSION);
    }

    public List<Order> getCarts()
    {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder query = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ProductName","ProductId","Quantity","Price","Discount" };

        String sqlTable = "OrderDetail";

        query.setTables(sqlTable);
        Cursor c = query.query(db,sqlSelect,null,null,null,null,null);

        final List<Order> result = new ArrayList<>();
        if(c.moveToFirst()){

            do{
                result.add(new Order(c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantity")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Discount"))
                        ));
            }while (c.moveToNext());

        }

        return result;
    }


    public void addToCart(Order order){
        SQLiteDatabase db = getWritableDatabase();
        String query = String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantity,Price,Discount) VALUES('%s','%s','%s','%s','%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount());
        db.execSQL(query);
    }


    public void cleanCart(){
        SQLiteDatabase db = getWritableDatabase();
        String query = String.format("DELETE FROM OrderDetail");
        db.execSQL(query);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Order.CREATE_STATEMENT);
        db.execSQL("CREATE TABLE Favourites (FoodId TEXT UNIQUE PRIMARY KEY);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Order.TABLE_NAME);
        onCreate(db);
    }

    public void addToFav(String foodId){
        SQLiteDatabase db = getWritableDatabase();
        String query = String.format("INSERT INTO Favourites(FoodId) VALUES('%s');", foodId);
        db.execSQL(query);
    }


    public void removeFromFav(String foodId){
        SQLiteDatabase db = getWritableDatabase();
        String query = String.format("DELETE FROM Favourites WHERE FoodId='%s';", foodId);
        db.execSQL(query);
    }

    public boolean isFav(String foodId){
        SQLiteDatabase db = getWritableDatabase();
        String query = String.format("SELECT * FROM Favourites WHERE FoodId='%s';", foodId);
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount()<=0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

}
