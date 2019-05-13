
package edu.monash.assignment3;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import edu.monash.assignment3.Database.Database;
import edu.monash.assignment3.Interface.FoodViewHolder;
import edu.monash.assignment3.Interface.ItemClickListener;
import edu.monash.assignment3.Interface.MenuViewHolder;
import edu.monash.assignment3.Model.Food;

public class FoodList extends AppCompatActivity {

    String categoryId;

    FirebaseDatabase database;
    DatabaseReference foodList;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;


    //Search founction
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestionList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //favourite food local database
    Database localDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);


        //init fb
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");


        //init fav local db
        localDB = new Database(this);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //get restarunt id from intent
        if (getIntent() != null) {
            categoryId = getIntent().getStringExtra("CategoryList");
        }
        if (!categoryId.isEmpty() && categoryId != null) {
            loadFoodList(categoryId);
        }


        //Search
        materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        loadSuggest();

        materialSearchBar.setLastSuggestions(suggestionList);
        //show 6 items
        materialSearchBar.setCardViewElevation(6);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<String>();
                for (String search: suggestionList){
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //when search bar is close
                //restore original adapter
                if(!enabled){
//                    searchAdapter.stopListening();
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search finish
                //show result of search adapter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void startSearch(CharSequence text) {



        Query query = FirebaseDatabase.getInstance().getReference().child("Foods").orderByChild("Name").equalTo(text.toString());

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(query, Food.class).build();


        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.food_name.setText(model.getName());

                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.food_image);

                final Food local = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent food = new Intent(FoodList.this,FoodDetail.class);
                        food.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(food);

                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(view);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
    }



    //get the search suggestion items form firebase
    private void loadSuggest() {

        foodList.orderByChild("MenuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Food item = postSnapshot.getValue(Food.class);
                    suggestionList.add(item.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadFoodList(String categoryId) {

        Query query = FirebaseDatabase.getInstance().getReference().child("Foods").orderByChild("MenuId").equalTo(categoryId);

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(query, Food.class).build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {
                holder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.food_image);


                //add fav
                if(localDB.isFav(adapter.getRef(position).getKey())){
                    holder.fav_img.setImageResource(R.drawable.ic_favorite_black_24dp);
                }

                //click to change state of fav
                holder.fav_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!localDB.isFav(adapter.getRef(position).getKey())){
                            localDB.addToFav(adapter.getRef(position).getKey());
                            holder.fav_img.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+"was added to Favourist",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            localDB.removeFromFav(adapter.getRef(position).getKey());
                            holder.fav_img.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, ""+model.getName()+"was removed from Favourist",Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                final Food local = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent food = new Intent(FoodList.this,FoodDetail.class);
                        food.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(food);

                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(view);
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
}
