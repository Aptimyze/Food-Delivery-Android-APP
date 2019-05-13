package edu.monash.assignment3.ServerSide;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;

import edu.monash.assignment3.Interface.ItemClickListener;
import edu.monash.assignment3.Interface.OrderViewHolder;
import edu.monash.assignment3.MapsActivity;
import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Request;
import edu.monash.assignment3.R;

public class ManageActivity extends AppCompatActivity {


    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    MaterialSpinner spinner;

    ArrayList<String>states = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);


        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView) findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();
    }

    private void loadOrders() {

        Query query = FirebaseDatabase.getInstance().getReference().child("Requests");

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(query, Request.class).build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final OrderViewHolder holder, final int position, @NonNull final Request model) {
                holder.txtOrderId.setText(adapter.getRef(position).getKey());
                holder.txtOrderStatus.setText(converCodeToStatus(model.getStatus()));
                holder.txtOrderAddress.setText(model.getAddress());
                holder.txtOrderPhone.setText(model.getPhone());


                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent request = new Intent(ManageActivity.this, MapsActivity.class);
                        request.putExtra("RequestsId", adapter.getRef(position).getKey());
                        Current.currentRequest = model;
                        startActivity(request);

                    }


                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(String key, final Request item) {

        //create a inflater dialog
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageActivity.this);
        alertDialog.setTitle("Update");
        alertDialog.setMessage("Change status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = (MaterialSpinner) view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","Shipping","Shipped");

        alertDialog.setView(view);

        //update the value
        final String localKey = key;
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);
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

    private String converCodeToStatus(String status) {
        if(status.equals("0")){
            return "Placed";
        }else if(status.equals("1")){
            return "Shipping";
        }else {
            return "Shipped";
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
