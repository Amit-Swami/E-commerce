package com.example.amazone_ecommerce2.Service;

import com.example.amazone_ecommerce2.Common.Common;
import com.example.amazone_ecommerce2.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken= FirebaseInstanceId.getInstance().getToken();
        updateToServer(refreshedToken);
    }

    private void updateToServer(String refreshedToken) {
        if(Common.currentUser!=null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference tokens = db.getReference("Tokens");
            Token data = new Token(refreshedToken, true);//it needs to be true
            tokens.child(Common.currentUser.getPhone()).setValue(data);
        }
    }
}