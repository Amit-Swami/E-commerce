package com.example.amazone_ecommerce;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.amazone_ecommerce.Common.Common;
import com.example.amazone_ecommerce.Database.Database;
import com.example.amazone_ecommerce.Model.Order;
import com.example.amazone_ecommerce.Model.Product;
import com.example.amazone_ecommerce.Model.Rating;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.Arrays;

import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProductDetail extends AppCompatActivity implements RatingDialogListener {
    TextView product_name,product_price,product_description;
    ImageView product_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String productId="";

    FirebaseDatabase database;
    DatabaseReference products;
    DatabaseReference ratingTbl;

    FButton btnShowComment;

    Product currentProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        btnShowComment=findViewById(R.id.btnShowComment);
        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProductDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_PRODUCT_ID,productId);
                startActivity(intent);
            }
        });

        database=FirebaseDatabase.getInstance();
        products=database.getReference("Subcategory");
        ratingTbl=database.getReference("Rating");

        numberButton=findViewById(R.id.number_button);
        btnCart=findViewById(R.id.btnCart);
        btnRating=findViewById(R.id.btn_rating);
        ratingBar=findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        Common.currentuser.getPhone(),
                        productId,
                        currentProduct.getName(),
                        numberButton.getNumber(),
                        currentProduct.getPrice(),
                        currentProduct.getDiscount(),
                        currentProduct.getImage()

                ));

                Toast.makeText(ProductDetail.this,"Added to Cart",Toast.LENGTH_SHORT).show();
            }
        });

        btnCart.setCount(new Database(this).getCountCart(Common.currentuser.getPhone()));

        product_description=findViewById(R.id.product_description);
        product_name=findViewById(R.id.product_name);
        product_price=findViewById(R.id.product_price);
        product_image=findViewById(R.id.img_product);

        collapsingToolbarLayout=findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get food Id from Intent
        if(getIntent()!=null)
            productId=getIntent().getStringExtra("productId");
        if (!productId.isEmpty())
        {
            if (Common.isConnectedToInternet(getBaseContext()))
            {
                getDetailProduct(productId);
                getRatingProduct(productId);

            }
            else
            {
                Toast.makeText(this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void getRatingProduct(String productId) {

        Query productRating=ratingTbl.orderByChild("productId").equalTo(productId);

        productRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item=postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count !=0)
                {
                    float average = sum/count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this product")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Plese write your comment here...")
                .setHintTextColor(R.color.colorPrimaryDark)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(ProductDetail.this)
                .show();
    }

    private void getDetailProduct(String productId){
        products.child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentProduct=dataSnapshot.getValue(Product.class);

                Picasso.with(getBaseContext()).load(currentProduct.getImage())
                        .into(product_image);

                collapsingToolbarLayout.setTitle(currentProduct.getName());
                product_price.setText(currentProduct.getPrice());

                product_name.setText(currentProduct.getName());
                product_description.setText(currentProduct.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        //Get rating and upload to firebase
        final Rating rating=new Rating(Common.currentuser.getPhone(),productId,String.valueOf(value),comments);

        //Fix user can rate multiple times
        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProductDetail.this, "Thank you for submit rating !!!", Toast.LENGTH_SHORT).show();
                    }
                });
        /*
        ratingTbl.child(Common.currentuser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.child(Common.currentuser.getPhone()).exists())
              {
                  //Remove old value
                  ratingTbl.child(Common.currentuser.getPhone()).removeValue();
                  //Update new value
                  ratingTbl.child(Common.currentuser.getPhone()).setValue(rating);
              }
              else
              {
                  //Update new value
                  ratingTbl.child(Common.currentuser.getPhone()).setValue(rating);
              }
                Toast.makeText(ProductDetail.this, "Thank you for submit rating !!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }
}