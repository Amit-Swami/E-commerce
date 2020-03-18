package com.example.amazone_ecommerce2.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.amazone_ecommerce2.Common.Common;
import com.example.amazone_ecommerce2.Interface.ItemClickListener;
import com.example.amazone_ecommerce2.R;

public class BannerViewHolder extends RecyclerView.ViewHolder implements
        View.OnCreateContextMenuListener
{
    public TextView banner_name;
    public ImageView banner_image;


    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);
        banner_name=itemView.findViewById(R.id.banner_name);
        banner_image=itemView.findViewById(R.id.banner_image);

        itemView.setOnCreateContextMenuListener(this);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");

        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}