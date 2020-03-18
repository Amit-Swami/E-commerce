package com.example.amazone_ecommerce2;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.amazone_ecommerce2.Common.Common;
import com.example.amazone_ecommerce2.Model.DataMessage;
import com.example.amazone_ecommerce2.Model.MyResponse;
import com.example.amazone_ecommerce2.Model.Request;
import com.example.amazone_ecommerce2.Model.Token;
import com.example.amazone_ecommerce2.Remote.APIService;
import com.example.amazone_ecommerce2.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase db;
    DatabaseReference requests;

    MaterialSpinner spinner,shipperSpinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        db=FirebaseDatabase.getInstance();
        requests=db.getReference("Requests");

        mService=Common.getFCMClient();

        recyclerView=findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();

    }

    private void loadOrders() {

        FirebaseRecyclerOptions<Request> options=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(requests,Request.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                //New Event button
                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(),
                                adapter.getItem(position));
                    }
                });

                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });

                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent orderDetail=new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest=model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

                viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrder=new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest=model;
                        startActivity(trackingOrder);
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };

        adapter.startListening();

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void deleteOrder(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater=this.getLayoutInflater();
        final View view=inflater.inflate(R.layout.update_order_layout,null);

        spinner=view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","On my way","Shipping");
        shipperSpinner=view.findViewById(R.id.shipperSpinner);

        //Load all shipper phone to spinner
        final List<String> shipperList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.SHIPPERS_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot shipperSnapshot:dataSnapshot.getChildren())
                            shipperList.add(shipperSnapshot.getKey());
                        shipperSpinner.setItems(shipperList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        alertDialog.setView(view);

        final String localKey=key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                if (item.getStatus().equals("2"))
                {

                    //copy item to table "orderNeedShip"
                    FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                            .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);

                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();

                    sendOrderStatusToUser(localKey, item);
                    sendOrderShipRequestToShipper(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString(),item);
                }
                else {
                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();

                    sendOrderStatusToUser(localKey, item);
                }
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();


    }

    private void sendOrderShipRequestToShipper(String shipperPhone, Request item) {
        DatabaseReference tokens=db.getReference("Tokens");

        tokens.child(shipperPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists())
                       {
                           Token token=dataSnapshot.getValue(Token.class);

                           Map<String,String> dataSend=new HashMap<>();
                           dataSend.put("title","Amazone ecommerce");
                           dataSend.put("message","You have new order need ship");
                           DataMessage dataMessage=new DataMessage(token.getToken(),dataSend);

                           mService.sendNotification(dataMessage)
                                   .enqueue(new Callback<MyResponse>() {
                                       @Override
                                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                           if (response.body().sucess==1)
                                           {
                                               Toast.makeText(OrderStatus.this, "Sent to Shippers", Toast.LENGTH_SHORT).show();
                                           }
                                           else
                                           {
                                               Toast.makeText(OrderStatus.this, " failed to send notification", Toast.LENGTH_SHORT).show();
                                           }
                                       }

                                       @Override
                                       public void onFailure(Call<MyResponse> call, Throwable t) {
                                           Log.e("ERROR",t.getMessage());

                                       }
                                   });
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendOrderStatusToUser(final String key,final Request item) {
        DatabaseReference tokens=db.getReference("Tokens");
        tokens.child(item.getPhone())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists())
                       {
                           Token token=dataSnapshot.getValue(Token.class);

                           Map<String,String> dataSend=new HashMap<>();
                           dataSend.put("title","Amazone ecommerce");
                           dataSend.put("message","Your order "+key+" was updated");
                           DataMessage dataMessage=new DataMessage(token.getToken(),dataSend);

                           mService.sendNotification(dataMessage)
                                   .enqueue(new Callback<MyResponse>() {
                                       @Override
                                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                           if (response.body().sucess==1)
                                           {
                                               Toast.makeText(OrderStatus.this, "Order was updated !", Toast.LENGTH_SHORT).show();
                                           }
                                           else
                                           {
                                               Toast.makeText(OrderStatus.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();
                                           }
                                       }

                                       @Override
                                       public void onFailure(Call<MyResponse> call, Throwable t) {
                                           Log.e("ERROR",t.getMessage());

                                       }
                                   });
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter!=null)
            adapter.startListening();

    }
}