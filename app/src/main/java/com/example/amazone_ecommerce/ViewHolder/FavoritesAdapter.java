package com.example.amazone_ecommerce.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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
import com.example.amazone_ecommerce.ProductDetail;
import com.example.amazone_ecommerce.Productlist;
import com.example.amazone_ecommerce.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

    private Context context;
    private List<Favorites> favoritesList;

    public FavoritesAdapter(Context context, List<Favorites> favoritesList) {
        this.context = context;
        this.favoritesList = favoritesList;
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(context)
                .inflate(R.layout.favorites_item,parent,false);
        return new FavoritesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FavoritesViewHolder viewHolder, final int position) {
        viewHolder.product_name.setText(favoritesList.get(position).getProductName());
        viewHolder.product_price.setText(String.format("%s",favoritesList.get(position).getProductPrice().toString()));
        Picasso.with(context).load(favoritesList.get(position).getProductImage())
                .into(viewHolder.product_image);

        //Quick cart
        viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isExists=new Database(context).checkProductExists(favoritesList.get(position).getProductId(), Common.currentuser.getPhone());

                if (!isExists) {
                    new Database(context).addToCart(new Order(
                            Common.currentuser.getPhone(),
                            favoritesList.get(position).getProductId(),
                            favoritesList.get(position).getProductName(),
                            "1",
                            favoritesList.get(position).getProductPrice(),
                            favoritesList.get(position).getProductDiscount(),
                            favoritesList.get(position).getProductImage()

                    ));

                } else {
                    new Database(context).increaseCart(Common.currentuser.getPhone(),
                            favoritesList.get(position).getProductId());
                }
                Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });



        final Favorites local=favoritesList.get(position);
        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                Intent productDetail=new Intent(context, ProductDetail.class);
                productDetail.putExtra("productId",favoritesList.get(position).getProductId());
                context.startActivity(productDetail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    public void removeItem(int position)
    {
        favoritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorites item,int position)
    {
        favoritesList.add(position,item);
        notifyItemInserted(position);
    }

    public Favorites getItem(int position)
    {
        return favoritesList.get(position);
    }
}
