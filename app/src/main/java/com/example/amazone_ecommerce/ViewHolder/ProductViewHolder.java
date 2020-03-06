package com.example.amazone_ecommerce.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.amazone_ecommerce.Interface.ItemClickListener;
import com.example.amazone_ecommerce.R;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView product_name,product_price;
    public ImageView product_image,fav_image,share_image,quick_cart;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        product_name=itemView.findViewById(R.id.product_name);
        product_image=itemView.findViewById(R.id.product_image);
        fav_image=itemView.findViewById(R.id.fav);
        share_image=itemView.findViewById(R.id.btnShare);
        product_price=itemView.findViewById(R.id.product_price);
        quick_cart=itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);

    }
}
