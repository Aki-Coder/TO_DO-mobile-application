package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private EditText loginEmail, loginPass;
    private Button button;
    private TextView loginQn;

    private FirebaseAuth mAuth;
    private ProgressDialog loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        toolbar = findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");

        mAuth = FirebaseAuth.getInstance();
        loader = new ProgressDialog(this);


        loginEmail = findViewById(R.id.loginEmail);
        loginPass = findViewById(R.id.loginPassword);
        button = findViewById(R.id.loginButton);
        loginQn = findViewById(R.id.loginPageQuestion);

        loginQn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString().trim();
                String password = loginPass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    loginEmail.setError("Email required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    loginPass.setError("Password is required");
                    return;
                }
                if(password.length() < 6 ){
                    loginPass.setError("Password must be >= 6 characters");
                    return;
                }

                    loader.setMessage("Login in progress");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()){
                               Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                               startActivity(intent);
                               finish();
                           }else{
                               String error = task.getException().toString();
                               Toast.makeText(LoginActivity.this, "Login failed " + error, Toast.LENGTH_SHORT).show();
                           }
                            loader.dismiss();
                        }
                    });




            }
        });
    }
}
