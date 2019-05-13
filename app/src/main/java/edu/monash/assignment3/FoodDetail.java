package edu.monash.assignment3;

import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

import edu.monash.assignment3.Database.Database;
import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Food;
import edu.monash.assignment3.Model.Order;
import edu.monash.assignment3.Model.Rating;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name, food_price, food_description;
    ImageView img_food;

    //sepacial layout using
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRting;

    //for add and minues number
    ElegantNumberButton numberButton;

    //for rating
    RatingBar ratingBar;

    String foodId = "";
    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTable;

    Food food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTable = database.getReference("Rating");


        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = (FloatingActionButton) findViewById(R.id.btnCart);

        btnRting = (FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        food.getName(),
                        numberButton.getNumber(),
                        food.getPrice(),
                        food.getDiscount()

                ));

                Toast.makeText(FoodDetail.this,"Added to Cart",Toast.LENGTH_SHORT).show();
            }
        });

        food_description = (TextView) findViewById(R.id.food_description);
        food_name = (TextView) findViewById(R.id.food_name);
        food_price = (TextView) findViewById(R.id.food_price);
        img_food = (ImageView) findViewById(R.id.img_food);

        collapsingToolbarLayout  = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);


        //get food id
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodId");
        }
        if (!foodId.isEmpty()) {
             getDetailFood(foodId);
             getRatingFood(foodId);
        }
    }

    private void getRatingFood(String foodId) {

        Query foodRating = ratingTable.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {

            int count = 0,sum = 0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Rating rating = snapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(rating.getRateValue());
                    count++;
                }

                if(count !=0)
                {
                    float avg = sum/count;
                    ratingBar.setRating(avg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very bad","Bad","Ok","Good","Very Good"))
                .setDefaultRating(1)
                .setTitle("Rating")
                .setDescription("Please give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Write your comment here")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                food = dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(food.getImage()).into(img_food);

                collapsingToolbarLayout.setTitle(food.getName());

                food_price.setText(food.getPrice());

                food_name.setText(food.getName());

                food_description.setText(food.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onPositiveButtonClicked(int i, String s) {
        final Rating rating = new Rating(Current.currentUser.getPhone(),
                foodId,String.valueOf(i),s);
        ratingTable.child(Current.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Current.currentUser.getPhone()).exists())
                {
                    //remove old rating and update new one
                    ratingTable.child(Current.currentUser.getPhone()).removeValue();
                    ratingTable.child(Current.currentUser.getPhone()).setValue(rating);
                }
                else {
                    ratingTable.child(Current.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(FoodDetail.this,"Thank you for submit rating!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
