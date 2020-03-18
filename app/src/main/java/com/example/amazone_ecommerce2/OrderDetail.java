package com.example.amazone_ecommerce2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.amazone_ecommerce2.Common.Common;
import com.example.amazone_ecommerce2.ViewHolder.OrderDetailAdapter;

public class OrderDetail extends AppCompatActivity {

    TextView order_id,order_phone,order_address,order_total,order_comment;
    String order_id_value="";
    RecyclerView lstProducts;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        order_id=findViewById(R.id.order_id);
        order_phone=findViewById(R.id.order_phone);
        order_address=findViewById(R.id.order_address);
        order_total=findViewById(R.id.order_total);
        order_comment=findViewById(R.id.order_comment);

        lstProducts=findViewById(R.id.lstProducts);
        lstProducts.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        lstProducts.setLayoutManager(layoutManager);

        if (getIntent()!=null)
            order_id_value=getIntent().getStringExtra("OrderId");

        //Set value
        order_id.setText(order_id_value);
        order_phone.setText(Common.currentRequest.getPhone());
        order_total.setText(Common.currentRequest.getTotal());
        order_address.setText(Common.currentRequest.getAddress());
        order_comment.setText(Common.currentRequest.getComment());

        OrderDetailAdapter adapter=new OrderDetailAdapter(Common.currentRequest.getProducts());
        adapter.notifyDataSetChanged();
        lstProducts.setAdapter(adapter);
    }
}