package com.example.amazone_ecommerce.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.amazone_ecommerce.Model.User;
import com.example.amazone_ecommerce.Remote.APIService;
import com.example.amazone_ecommerce.Remote.GoogleRetrofitClient;
import com.example.amazone_ecommerce.Remote.IGoogleService;
import com.example.amazone_ecommerce.Remote.RetrofitClient;

public class Common {

    public static String topicName="News";

    public static User currentuser;

    public static String currentKey;

    public static String PHONE_TEXT ="userPhone";

    public static final String INTENT_PRODUCT_ID= "ProductId";

    private static final String BASE_URL="https://fcm.googleapis.com";

    private static final String GOOGLE_API_URL="https://maps.googleapis.com";

    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI()
    {
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String code)
    {
        if (code.equals("0"))
            return "Placed";
        else if (code.equals("1"))
            return "On my way";
        else if (code.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }

    public static final String DELETE="Delete";
    public static final String USER_KEY="User";
    public static final String PWD_KEY="Password";

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager!=null)
        {
            NetworkInfo[] info=connectivityManager.getAllNetworkInfo();
            if (info!=null)
            {
                for (int i=0;i<info.length;i++)
                {
                    if (info[i].getState()==NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
