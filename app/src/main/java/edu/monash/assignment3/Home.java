package edu.monash.assignment3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import edu.monash.assignment3.Interface.ItemClickListener;
import edu.monash.assignment3.Interface.MenuViewHolder;
import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Resturant;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //get the firebase
    FirebaseDatabase database;
    DatabaseReference resturant;

    //using firebaseUI
    FirebaseRecyclerAdapter adapter;

    TextView txtFullNmae;
    ImageView imageView;


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);


        //firebase
        database = FirebaseDatabase.getInstance();
        resturant = database.getReference("Resturant");



        //switch to cart class
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
            }
        });


        //tap down to refresh
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //set user name in nav
        View headView = navigationView.getHeaderView(0);
        txtFullNmae = (TextView) headView.findViewById(R.id.txtFullName);
        txtFullNmae.setText(Current.currentUser.getName());



        //load menu
        recyclerView = (RecyclerView) findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadMenu();


        //Message access
        Intent message = new Intent(Home.this,Message.class);
        startService(message);
    }

    private void loadMenu() {

        Query query = FirebaseDatabase.getInstance().getReference().child("Resturant");

        FirebaseRecyclerOptions<Resturant> options = new FirebaseRecyclerOptions.Builder<Resturant>().setQuery(query, Resturant.class).build();

        //restarunt adapter list
        adapter = new FirebaseRecyclerAdapter<Resturant,MenuViewHolder>(options){

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_iten,parent,false);
                return new MenuViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Resturant model) {
                holder.txtMenuName.setText(model.getName());

                //set the picture
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.menuImage);
                final Resturant clickItem = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodlist = new Intent(Home.this,FoodList.class);
                        foodlist.putExtra("CategoryList",adapter.getRef(position).getKey());
                        startActivity(foodlist);
                    }
                });
            }

        };
        recyclerView.setAdapter(adapter);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {

            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
        } else if (id == R.id.nav_orders) {
            Intent orderStatusIntent = new Intent(Home.this,OrderStatus.class);
            startActivity(orderStatusIntent);
        } else if (id == R.id.nav_log_out) {
            Intent signin = new Intent(Home.this,SignIn.class);
            signin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signin);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
