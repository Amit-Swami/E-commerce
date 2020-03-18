package com.example.amazone_ecommerce2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.amazone_ecommerce2.Common.Common;
import com.example.amazone_ecommerce2.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class SignIn extends AppCompatActivity {

    EditText edtPhone,edtPassword;
    Button btnSignIn;

    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        edtPhone=findViewById(R.id.editph);
        edtPassword=findViewById(R.id.editpassw);
        btnSignIn=findViewById(R.id.btnSignIn);

        db=FirebaseDatabase.getInstance();
        users=db.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInuser(edtPhone.getText().toString(),edtPassword.getText().toString());
            }
        });
    }

    private void signInuser(String phone, String password) {
        final ProgressDialog progressDialog=new ProgressDialog(SignIn.this);
        progressDialog.setMessage("Please waiting...");
        progressDialog.show();

        final String localPhone=phone;
        final String localPassword=password;

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localPhone).exists())
                {
                    progressDialog.dismiss();
                    User user=dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);
                    if (Boolean.parseBoolean(user.getIsStaff()))
                    {
                        if (user.getPassword().equals(localPassword))
                        {
                            Intent login=new Intent(SignIn.this,Home.class);
                            Common.currentUser=user;
                            startActivity(login);
                            finish();
                        }
                        else
                            Toast.makeText(SignIn.this,"Wrong password",Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(SignIn.this,"Please login with Staff account",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.dismiss();
                    Toast.makeText(SignIn.this,"User not registered",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}