package com.example.amazone_ecommerce.Service;

import com.example.amazone_ecommerce.Common.Common;
import com.example.amazone_ecommerce.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenRefreshed=FirebaseInstanceId.getInstance().getToken();
        if (Common.currentuser!=null)
              updateTokenToFirebase(tokenRefreshed);
    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(tokenRefreshed,false);//false because this token send from client app
        tokens.child(Common.currentuser.getPhone()).setValue(token);
    }
}