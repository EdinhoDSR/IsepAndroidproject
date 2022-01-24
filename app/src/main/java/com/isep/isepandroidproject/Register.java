package com.isep.isepandroidproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText mETMail,mETUsername, mETPassword, mETPasswordAgain ;
    private Button mBtn;
    private FirebaseAuth fAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mETMail = findViewById(R.id.register_edittext_mail);
        mETUsername = findViewById(R.id.register_edittext_username);
        mETPassword = findViewById(R.id.register_edittext_password);
        mETPasswordAgain = findViewById(R.id.register_edittext_passwordagain);
        mBtn = findViewById(R.id.register_btn);

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();




        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mETMail.getText().toString().trim();
                String username = mETUsername.getText().toString().trim();
                String password = mETPassword.getText().toString().trim();
                String passwordAgain = mETPasswordAgain.getText().toString().trim();


                if(TextUtils.isEmpty(email)){
                    mETMail.setError("email required");
                    return;
                }
                if(TextUtils.isEmpty(username)){
                    mETUsername.setError("username required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mETPassword.setError("password required");
                    return;
                }
                if(TextUtils.isEmpty(passwordAgain)){
                    mETPasswordAgain.setError("password required");
                    return;
                }
                if(!TextUtils.equals(password, passwordAgain)){
                    mETPassword.setError("The passwords are different");
                    mETPasswordAgain.setError("The passwords are different");
                    return;
                }




                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            db.collection("users")
                                    .add(createUserForDB(username,email))
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("Registration", "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Registration", "Error adding document", e);
                                        }
                                    });
                            Toast.makeText(Register.this, "User created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Register.this, MainActivity.class));
                        }
                        else{
                            Toast.makeText(Register.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    public void login(View view){

        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
    public Map<String, Object> createUserForDB(String pUsername, String pMail){
        Map<String, Object> user = new HashMap<>();
        user.put("Username", pUsername);
        user.put("Mail", pMail);
        user.put("Score", 0);
        return user;
    }
}