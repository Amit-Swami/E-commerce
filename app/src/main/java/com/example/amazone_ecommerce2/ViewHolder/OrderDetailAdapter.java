package com.example.amazone_ecommerce2.ViewHolder;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.amazone_ecommerce2.Model.Order;
import com.example.amazone_ecommerce2.R;

import java.util.List;

class MyViewHolder extends RecyclerView.ViewHolder{

    public TextView name,quantity,price,discount;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name=itemView.findViewById(R.id.product_name);
        quantity=itemView.findViewById(R.id.product_quantity);
        price=itemView.findViewById(R.id.product_price);
        discount=itemView.findViewById(R.id.product_discount);
    }
}
public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> myOrder;

    public OrderDetailAdapter(List<Order> myOrder) {
        this.myOrder = myOrder;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.order_detail_layout,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
      Order order=myOrder.get(i);
      myViewHolder.name.setText(String.format("Name : %s",order.getProductName()));
      myViewHolder.quantity.setText(String.format("Quantity : %s",order.getQuantity()));
      myViewHolder.price.setText(String.format("Price : %s",order.getPrice()));
      myViewHolder.discount.setText(String.format("Discount : %s",order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return myOrder.size();
    }
}
