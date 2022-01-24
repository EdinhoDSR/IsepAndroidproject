package com.isep.isepandroidproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private EditText mETMail, mETPassword;
    private Button mBtn;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mETMail = findViewById(R.id.login_edittext_mail);
        mETPassword = findViewById(R.id.login_edittext_password);
        mBtn = findViewById(R.id.login_btn);
        fAuth = FirebaseAuth.getInstance();

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mETMail.getText().toString().trim();
                String password = mETPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mETMail.setError("email required");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mETPassword.setError("password required");
                    return;
                }

                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "successful login", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, MainActivity.class));
                        }
                        else{
                            Toast.makeText(Login.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });


    }
    public void register(View view){

        startActivity(new Intent(getApplicationContext(), Register.class));
        finish();
    }
}