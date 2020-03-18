package com.example.amazone_ecommerce2.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.amazone_ecommerce2.Interface.ItemClickListener;
import com.example.amazone_ecommerce2.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{
    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress,txtOrderDate;

    public Button btnEdit,btnRemove,btnDetail,btnDirection;


    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtOrderAddress=itemView.findViewById(R.id.order_address);
        txtOrderId=itemView.findViewById(R.id.order_id);
        txtOrderStatus=itemView.findViewById(R.id.order_status);
        txtOrderPhone=itemView.findViewById(R.id.order_phone);
        txtOrderDate=itemView.findViewById(R.id.order_date);

        btnEdit=itemView.findViewById(R.id.btnEdit);
        btnRemove=itemView.findViewById(R.id.btnRemove);
        btnDetail=itemView.findViewById(R.id.btnDetail);
        btnDirection=itemView.findViewById(R.id.btnDirection);


    }

}
