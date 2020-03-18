package com.example.amazone_ecommerce2.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.amazone_ecommerce2.Interface.ItemClickListener;
import com.example.amazone_ecommerce2.R;

import info.hoang8f.widget.FButton;

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView shipper_name,shipper_phone;
    public FButton btn_edit,btn_remove;
    private ItemClickListener itemClickListener;

    public ShipperViewHolder(View itemView) {
        super(itemView);

        shipper_name=itemView.findViewById(R.id.shipper_name);
        shipper_phone=itemView.findViewById(R.id.shipper_phone);
        btn_edit=itemView.findViewById(R.id.btnEdit);
        btn_remove=itemView.findViewById(R.id.btnRemove);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
