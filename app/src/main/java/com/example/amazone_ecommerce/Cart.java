package com.example.amazone_ecommerce;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amazone_ecommerce.Common.Common;
import com.example.amazone_ecommerce.Common.Config;
import com.example.amazone_ecommerce.Database.Database;
import com.example.amazone_ecommerce.Helper.RecyclerItemTouchHelper;
import com.example.amazone_ecommerce.Interface.RecyclerItemTouchHelperListener;
import com.example.amazone_ecommerce.Model.DataMessage;
import com.example.amazone_ecommerce.Model.MyResponse;
import com.example.amazone_ecommerce.Model.Order;
import com.example.amazone_ecommerce.Model.Request;
import com.example.amazone_ecommerce.Model.Token;
import com.example.amazone_ecommerce.Remote.APIService;
import com.example.amazone_ecommerce.Remote.IGoogleService;
import com.example.amazone_ecommerce.ViewHolder.CartAdapter;
import com.example.amazone_ecommerce.ViewHolder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerItemTouchHelperListener {

    private static final int PAYPAL_REQUEST_CODE = 9999;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart=new ArrayList<>();
    CartAdapter adapter;


    Place shippingAddress;

    RelativeLayout rootLayout;

    //Paypal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)//change it to Production if you go live
            .clientId(Config.PAYPAL_CLIENT_ID);
    String address,comment;

    //Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL=5000;
    private static final int FASTEST_INTERVAL=3000;
    private static final int DISPLACEMENT=10;

    private static final int LOCATION_REQUEST_CODE=9999;
    private static final int PLAY_SERVICES_REQUEST=9997;

    IGoogleService mGoogleMapService;
    APIService mService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mGoogleMapService = Common.getGoogleMapAPI();

        //Runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },LOCATION_REQUEST_CODE);
        }
        else
        {
            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //Init Paypal
        Intent intent=new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        rootLayout=findViewById(R.id.rootLayout);

        mService=Common.getFCMService();

        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        recyclerView=findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback=new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice=findViewById(R.id.total);
        btnPlace=findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if (cart.size()>0)
                   showAlertDialog();
               else
                   Toast.makeText(Cart.this, "Your cart is empty !!", Toast.LENGTH_SHORT).show();
            }
        });

        loadListProduct();


    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            else
            {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        LayoutInflater inflater=this.getLayoutInflater();
        View order_address_comment=inflater.inflate(R.layout.order_address_comment,null);

       // final MaterialEditText edtAddress=order_address_comment.findViewById(R.id.edtAddress);
        final PlaceAutocompleteFragment edtAddress= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //Hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //Set Hint for Autocomplete Edit Text
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address");
        //set Text Size
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);
        //get address from place autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress=place;
            }

            @Override
            public void onError(Status status) {
              Log.e("ERROR",status.getStatusMessage());
            }
        });
        final MaterialEditText edtComment=order_address_comment.findViewById(R.id.edtComment);

        //Radio
        final RadioButton rdiShipToAddress= order_address_comment.findViewById(R.id.rdiShipToThisAddress);
        final RadioButton rdiHomeAddress=order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiCOD= order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal=order_address_comment.findViewById(R.id.rdiPaypal);


        //Event Radio
        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               if (Common.currentuser.getHomeAddress() != null ||
                      !TextUtils.isEmpty(Common.currentuser.getHomeAddress()))
               {
                   address=Common.currentuser.getHomeAddress();
                   ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                           .setText(address);
               }
               else
               {
                   Toast.makeText(Cart.this, "Please update your Home Address", Toast.LENGTH_SHORT).show();
               }
            }
        });

        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //ship to this address features
                if (b)
                {
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false&key=AIzaSyAk2_ohELRrOepgIYNLHxu2o9nC93T-C3Y",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //If fetch API ok
                                    try {
                                        JSONObject jsonObject=new JSONObject(response.body().toString());

                                        JSONArray resultArray = jsonObject.getJSONArray("results");

                                        JSONObject firstObject=resultArray.getJSONObject(0);

                                        address = firstObject.getString("formatted_address");

                                        //set the address with edit text address
                                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //add check condition here
                if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {

                    //if both radio not selected
                    if (shippingAddress != null)
                        address = shippingAddress.getAddress().toString();
                    else
                    {
                        Toast.makeText(Cart.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();
                        //Fix crash fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                }

                if (TextUtils.isEmpty(address))
                {
                    Toast.makeText(Cart.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();
                    //Fix crash fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }
                comment=edtComment.getText().toString();

                //Check payment
                if (!rdiCOD.isChecked() && !rdiPaypal.isChecked())
                {
                    Toast.makeText(Cart.this, "Please select Payment option", Toast.LENGTH_SHORT).show();
                    //Fix crash fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }
                else if (rdiPaypal.isChecked()) {

                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace("$", "")
                            .replace(",", "");

                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                            "USD",
                            "E Commerce App Order",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                }
                else if (rdiCOD.isChecked())
                {
                    //copy code from onActivity result
                    //create new request
                    Request request=new Request(
                            Common.currentuser.getPhone(),
                            Common.currentuser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",
                            comment,
                            "COD",
                            "Unpaid",
                            String.format("%s,%s",mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                            cart
                    );

                    //submit to Firebase
                    //we will using System.currentMilli to key
                    String order_number=String.valueOf(System.currentTimeMillis());
                    requests.child(order_number)
                            .setValue(request);
                    //Delete cart
                    new Database(getBaseContext()).cleanCart(Common.currentuser.getPhone());
                    sendNotificationOrder(order_number);
                  //  Toast.makeText(Cart.this,"Thank you , Order Placed",Toast.LENGTH_SHORT).show();
                   // finish();

                }

                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                PaymentConfirmation confirmation=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null)
                {
                    try{
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                //create new request
                Request request=new Request(
                        Common.currentuser.getPhone(),
                        Common.currentuser.getName(),
                        address,
                        txtTotalPrice.getText().toString(),
                        "0",
                         comment,
                        "Paypal",
                        jsonObject.getJSONObject("response").getString("state"),
                        String.format("%s,%s",mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                        cart
                );

                //submit to Firebase
                //we will using System.currentMilli to key
                String order_number=String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart(Common.currentuser.getPhone());
                sendNotificationOrder(order_number);
                Toast.makeText(Cart.this,"Thank you , Order Placed",Toast.LENGTH_SHORT).show();
                finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(this, "Payment cancel", Toast.LENGTH_SHORT).show();
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Invalid payment", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query data=tokens.orderByChild("isServiceToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
               {
                   Token serverToken=postSnapShot.getValue(Token.class);

               //    Notification notification=new Notification("Amazone ecommerce","You have new order "+order_number);
               //    Sender content=new Sender(serverToken.getToken(),notification);
                   Map<String,String> dataSend = new HashMap<>();
                   dataSend.put("title","Amazone ecommerce");
                   dataSend.put("message","You have new order "+ order_number);
                   DataMessage dataMessage=new DataMessage(serverToken.getToken(),dataSend);

                   mService.sendNotification(dataMessage)
                           .enqueue(new Callback<MyResponse>() {
                               @Override
                               public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                if (response.code()==200) {
                                    if (response.body().sucess == 1) {
                                        Toast.makeText(Cart.this, "Thank you , Order Place", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(Cart.this, "Thank you , Order Place!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
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

    private void loadListProduct(){
      cart=new Database(this).getCarts(Common.currentuser.getPhone());
      adapter=new CartAdapter(cart,this);
      adapter.notifyDataSetChanged();
      recyclerView.setAdapter(adapter);

      //Calculate total price
        int total=0;
        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale=new Locale("en","US");
        NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE));
             deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        new Database(this).cleanCart(Common.currentuser.getPhone());

        for (Order item:cart)
            new Database(this).addToCart(item);
        loadListProduct();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder)
        {
            String name=((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem=((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex=viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentuser.getPhone());

            //update txttotal....
            int total=0;
            List<Order> orders=new Database(getBaseContext()).getCarts(Common.currentuser.getPhone());
            for (Order item:orders)
                total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
            Locale locale=new Locale("en","US");
            NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(fmt.format(total));

            Snackbar snackbar=Snackbar.make(rootLayout,name + " removed from cart!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  adapter.restoreItem(deleteItem,deleteIndex);
                  new Database(getBaseContext()).addToCart(deleteItem);

                    //update txttotal....
                    int total=0;
                    List<Order> orders=new Database(getBaseContext()).getCarts(Common.currentuser.getPhone());
                    for (Order item:orders)
                        total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    Locale locale=new Locale("en","US");
                    NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);

                    txtTotalPrice.setText(fmt.format(total));

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
        {
            Log.d("LOCATION","Your location : "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        }
        else
        {
            Log.d("LOCATION","Could not get your location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
      mLastLocation = location;
      displayLocation();
    }
}