package com.example.baldawordgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextTextEmailAddressLogin;
    private EditText editTextTextPasswordLogin;

    private Button logInBtn;
    private Button signInBtn;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        editTextTextEmailAddressLogin = findViewById(R.id.editTextTextEmailAddressLogin);
        editTextTextPasswordLogin = findViewById(R.id.editTextTextPasswordLogin);
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

        String email = editTextTextEmailAddressLogin.getText().toString();
        String password = editTextTextPasswordLogin.getText().toString();

        if (TextUtils.isEmpty(email)) {
            editTextTextEmailAddressLogin.setError("Поле не может быть пустым!");
        } else {
            editTextTextEmailAddressLoginIsNotEmpty = true;
        }
        if (TextUtils.isEmpty(password)) {
            editTextTextPasswordLogin.setError("Поле не может быть пустым!");
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