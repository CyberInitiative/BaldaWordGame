package com.example.baldawordgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.baldawordgame.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private Button confirmBtn;
    private EditText editTextTextEmailAddressSignup;
    private EditText editTextTextUsernameLogin;
    private EditText editTextTextPasswordSignup;
    private EditText editTextTextPasswordSignupConfirm;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        init();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReferenceFromUrl("https://baldawordgame-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.child("users"); //list of all users in database;

        confirmBtn = findViewById(R.id.confirmBtnSignupActivity);
        editTextTextEmailAddressSignup = findViewById(R.id.editTextTextEmailAddressSignup);
        editTextTextUsernameLogin = findViewById(R.id.editTextTextUsernameLogin);
        editTextTextPasswordSignup = findViewById(R.id.editTextTextPasswordSignup);
        editTextTextPasswordSignupConfirm = findViewById(R.id.editTextTextPasswordSignupConfirm);
        confirmBtn.setOnClickListener(event -> {
            createUser();
        });
    }

    private void createUser() {
        boolean editTextTextEmailAddressSignupIsNotEmpty = false;
        boolean editTextTextPasswordSignupIsNotEmpty = false;
        boolean editTextTextPasswordSignupConfirmIsNotEmpty = false;
        boolean editTextTextUsernameLoginIsNotEmpty = false;
        boolean passwordsAreEqual = false;

        String email = editTextTextEmailAddressSignup.getText().toString();
        String username = editTextTextUsernameLogin.getText().toString();
        String password = editTextTextPasswordSignup.getText().toString();
        String confirmedPassword = editTextTextPasswordSignupConfirm.getText().toString();

        if (TextUtils.isEmpty(email)) {
            editTextTextEmailAddressSignup.setError("Поле не может быть пустым!");
        } else {
            editTextTextEmailAddressSignupIsNotEmpty = true;
        }
        if (TextUtils.isEmpty(username)) {
            editTextTextUsernameLogin.setError("Поле не может быть пустым!");
        } else {
            editTextTextUsernameLoginIsNotEmpty = true;
        }
        if (TextUtils.isEmpty(password)) {
            editTextTextPasswordSignup.setError("Поле не может быть пустым!");
        } else {
            editTextTextPasswordSignupIsNotEmpty = true;
        }
        if (TextUtils.isEmpty(confirmedPassword)) {
            editTextTextPasswordSignupConfirm.setError("Поле не может быть пустым!");
        } else {
            editTextTextPasswordSignupConfirmIsNotEmpty = true;
        }
        if (!password.equals(confirmedPassword)) {
            editTextTextPasswordSignupConfirm.setError("Пароли не совпадают!");
        } else {
            passwordsAreEqual = true;
        }
        if (editTextTextEmailAddressSignupIsNotEmpty && editTextTextUsernameLoginIsNotEmpty && editTextTextPasswordSignupIsNotEmpty
                && editTextTextPasswordSignupConfirmIsNotEmpty && passwordsAreEqual) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (firebaseAuth.getCurrentUser() != null) {
//                        User createdUser = new User(email, username, firebaseAuth.getCurrentUser().getUid());
                        try {
                            User createdUser = new User(email, username);
                            usersRef.child(firebaseAuth.getCurrentUser().getUid()).setValue(createdUser);
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        } catch (DatabaseException databaseException) {
                            Toast.makeText(SignupActivity.this, "Database error: " + databaseException
                                    , Toast.LENGTH_SHORT).show();
                        }
//                        database.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                database.setValue(createdUser);
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                Toast.makeText(SignupActivity.this, "Database error: " + error
//                                        , Toast.LENGTH_SHORT).show();
//                            }
//                        });

                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Registration Error: " + task.getException().getMessage()
                            , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}