package com.example.amazone_ecommerce2;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.amazone_ecommerce2.Common.Common;
import com.example.amazone_ecommerce2.Model.DataMessage;
import com.example.amazone_ecommerce2.Model.MyResponse;
import com.example.amazone_ecommerce2.Remote.APIService;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMessage extends AppCompatActivity {

    MaterialEditText edtMessage,edtTitle;
    FButton btnSend;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        mService= Common.getFCMClient();

        edtMessage=findViewById(R.id.edtMessage);
        edtTitle=findViewById(R.id.edtTitle);
        btnSend=findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create message
               // Notification notification=new Notification(edtMessage.getText().toString(),edtTitle.getText().toString());
                Map<String,String> dataSend=new HashMap<>();
                dataSend.put("title",edtTitle.getText().toString());
                dataSend.put("message",edtMessage.getText().toString());
                DataMessage dataMessage=new DataMessage(new StringBuilder("/topics/").append(Common.topicName).toString(),dataSend);


                mService.sendNotification(dataMessage)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if (response.isSuccessful())
                                    Toast.makeText(SendMessage.this, "Message Sent", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(SendMessage.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });
    }
}