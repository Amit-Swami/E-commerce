package com.example.amazone_ecommerce;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.example.amazone_ecommerce.Common.Common;
import com.example.amazone_ecommerce.Database.Database;
import com.example.amazone_ecommerce.Interface.ItemClickListener;
import com.example.amazone_ecommerce.Model.Favorites;
import com.example.amazone_ecommerce.Model.Order;
import com.example.amazone_ecommerce.Model.Product;
import com.example.amazone_ecommerce.ViewHolder.ProductViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
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
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<Product, ProductViewHolder> adapter;

    FirebaseRecyclerAdapter<Product, ProductViewHolder> searchAdapter;
    List<String> suggestList=new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference productlist;

    //Favorites
    Database localDB;

    //Facebook share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    Target target=new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto photo=new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content=new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Init Facebook
        callbackManager=CallbackManager.Factory.create();
        shareDialog=new ShareDialog(this);


        database=FirebaseDatabase.getInstance();
        productlist=database.getReference("Subcategory");

        localDB=new Database(this);

        recyclerView=findViewById(R.id.recycler_search);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Search
        materialSearchBar=findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your product");
        loadSuggest();
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //when user type their text , we will change suggest list

                List<String> suggest=new ArrayList<String>();
                for (String search:suggestList)
                {
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
            public void onSearchStateChanged(boolean b) {
                //when search Bar is close
                //Restore original suggest adapter
                if (!b)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence charSequence) {
                //when search finish
                //Show result of search adapter
                startSearch(charSequence);
            }

            @Override
            public void onButtonClicked(int i) {

            }
        });

        //load all product
        loadAllProducts();

    }

    private void loadAllProducts() {
        Query searchByName=productlist;
        FirebaseRecyclerOptions<Product> productOptions=new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(searchByName,Product.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<Product, ProductViewHolder>(productOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ProductViewHolder viewHolder, final int position, @NonNull final Product model) {
                viewHolder.product_name.setText(model.getName());
                viewHolder.product_price.setText(String.format("%s",model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.product_image);

                //Quick cart
                viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isExists=new Database(getBaseContext()).checkProductExists(adapter.getRef(position).getKey(), Common.currentuser.getPhone());

                        if (!isExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentuser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()

                            ));

                        } else {
                            new Database(getBaseContext()).increaseCart(Common.currentuser.getPhone(), adapter.getRef(position).getKey());
                        }
                        Toast.makeText(SearchActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                    }
                });
                //Add Favorites
                if (localDB.isFavorite(adapter.getRef(position).getKey(),Common.currentuser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //click to share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                //click to change state of Favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites=new Favorites();
                        favorites.setProductId(adapter.getRef(position).getKey());
                        favorites.setProductName(model.getName());
                        favorites.setProductDescription(model.getDescription());
                        favorites.setProductDiscount(model.getDiscount());
                        favorites.setProductImage(model.getImage());
                        favorites.setProductMenuId(model.getMenuId());
                        favorites.setUserPhone(Common.currentuser.getPhone());
                        favorites.setProductPrice(model.getPrice());


                        if (!localDB.isFavorite(adapter.getRef(position).getKey(),Common.currentuser.getPhone()))
                        {
                            localDB.addToFavorites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(SearchActivity.this, "was added to Favorites", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(),Common.currentuser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(SearchActivity.this, "was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Product local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent productDetail=new Intent(SearchActivity.this,ProductDetail.class);
                        productDetail.putExtra("productId",adapter.getRef(position).getKey());
                        startActivity(productDetail);
                    }
                });
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView=LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.product_item,viewGroup,false);
                return new ProductViewHolder(itemView);
            }
        };
        adapter.startListening();
        //set Adapter
        recyclerView.setAdapter(adapter);

    }

    private void startSearch(CharSequence charSequence) {
        Query searchByName=productlist.orderByChild("name").equalTo(charSequence.toString());
        FirebaseRecyclerOptions<Product> productOptions=new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(searchByName,Product.class)
                .build();

        searchAdapter= new FirebaseRecyclerAdapter<Product, ProductViewHolder>(productOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder viewHolder, int position, @NonNull Product model) {
                viewHolder.product_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.product_image);

                final Product local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent productdetail=new Intent(SearchActivity.this,ProductDetail.class);
                        productdetail.putExtra("productId",searchAdapter.getRef(position).getKey());
                        startActivity(productdetail);
                    }
                });
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView= LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.product_item,viewGroup,false);
                return new ProductViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        productlist.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Product item=postSnapshot.getValue(Product.class);
                            suggestList.add(item.getName());
                        }

                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStop() {
        if (adapter != null)
            adapter.stopListening();
        if (searchAdapter != null)
            searchAdapter.stopListening();
        super.onStop();
    }
}
