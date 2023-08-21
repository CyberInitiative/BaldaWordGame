package com.example.baldawordgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private Button logInBtn;
    private Button signInBtn;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

    }

    private void init() {
        emailEditText = findViewById(R.id.editTextTextEmailAddressLogin);
        passwordEditText = findViewById(R.id.editTextTextPasswordLogin);
        logInBtn = findViewById(R.id.logInBtn);
        signInBtn = findViewById(R.id.signUpBtn);

        signInBtn.setOnClickListener(event -> {
            Intent signUpIntent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(signUpIntent);
        });

        logInBtn.setOnClickListener(event -> {
            loginUser();
        });
    }

    private void loginUser() {
        boolean editTextTextEmailAddressLoginIsNotEmpty = false;
        boolean editTextTextPasswordLoginIsNotEmpty = false;

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Поле не может быть пустым!");
        } else {
            editTextTextEmailAddressLoginIsNotEmpty = true;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Поле не может быть пустым!");
        } else {
            editTextTextPasswordLoginIsNotEmpty = true;
        }
        if (editTextTextEmailAddressLoginIsNotEmpty && editTextTextPasswordLoginIsNotEmpty) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Registration Error: " + task.getException().getMessage()
                            , Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}