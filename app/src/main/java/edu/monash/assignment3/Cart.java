
package edu.monash.assignment3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.monash.assignment3.Database.Database;
import edu.monash.assignment3.Interface.CartAdapter;
import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Order;
import edu.monash.assignment3.Model.Request;
import info.hoang8f.widget.FButton;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager cartlayoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(Current.PAYPAL_CLIENT_ID);

    String address, comment;
    private int PAYPAL_REQUEST_CODE = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //init paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        cartlayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(cartlayoutManager);

        txtTotalPrice = (TextView) findViewById(R.id.total);
        btnPlace = (Button) findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cart.size()>0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this,"Your cart is empty!",Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();


    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Where are you?");
        alertDialog.setMessage("Enter your address: ");

        final EditText editAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editAddress.setLayoutParams(lp);
        alertDialog.setView(editAddress);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                //show paypal to patment

                address = editAddress.getText().toString();
                String formatAmount = txtTotalPrice.getText().toString().
                                        replace("$","").replace(",","");

                PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),"AUD","Take Out Order",PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
                startActivityForResult(intent,PAYPAL_REQUEST_CODE);


            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PAYPAL_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirmation != null)
                {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                        //create order request
                        Request request = new Request(
                                Current.currentUser.getPhone(),
                                Current.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                jsonObject.getJSONObject("response").getString("state"),
                                cart
                        );

                        //send to firebase and key is timeMillis
                        requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);

                        new Database(getBaseContext()).cleanCart();
                        Toast.makeText(Cart.this,"Thank you", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(resultCode == Activity.RESULT_CANCELED)
                {
                    Toast.makeText(Cart.this,"Payment Cancelled", Toast.LENGTH_SHORT).show();
                }
                else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                {
                    Toast.makeText(Cart.this,"Invaild Payment", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadListFood() {

        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        int total = 0;
        for (Order order:cart){
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        }
        Locale locale = new Locale("en","AU");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(format.format(total));
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Current.DELETE)){
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int position) {

        cart.remove(position);

        new Database(this).cleanCart();

        for(Order item: cart)
            new Database(this).addToCart(item);

        loadListFood();
    }
}
