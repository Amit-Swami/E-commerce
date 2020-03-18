package com.example.amazone_ecommerce2.Remote;

import com.example.amazone_ecommerce2.Model.DataMessage;
import com.example.amazone_ecommerce2.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAVdJXhbI:APA91bF8N0xWYfR41djKwcYgEhqJoUA6N3LH-c7a0KHeLiiYSwQb3Fng7pA-TFY5goZDeAFa6FP-yNuPBtKCJ1yLjX_a8UBUmUA3sbT67wQaSgKc7dW6DKP00bUyeBsMpzdVsNyutZPV"

            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}
