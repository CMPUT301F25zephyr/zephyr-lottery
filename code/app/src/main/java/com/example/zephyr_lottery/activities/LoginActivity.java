package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference accountsRef;
    private Button signin_button;
    private Button createacc_button;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //set spinner contents (dropdown menu)
        Spinner spinner = (Spinner) findViewById(R.id.account_type_spinner);
        //create ArrayAdapter from string array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_type_spinner_array,
                android.R.layout.simple_spinner_item
        );
        //pick the default layout for spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //apply adapter
        spinner.setAdapter(adapter);

        //firebase stuff?
        db = FirebaseFirestore.getInstance();
        accountsRef = db.collection("accounts");
        mAuth = FirebaseAuth.getInstance();

        //create account button listener
        createacc_button = findViewById(R.id.button_signup);
        createacc_button.setOnClickListener(view -> {
            //get text from edit text fields
            String username_text = ((EditText) findViewById(R.id.signup_user)).getText().toString();
            String password_text = ((EditText) findViewById(R.id.signup_pass)).getText().toString();
            String email_text = ((EditText) findViewById(R.id.signup_email)).getText().toString();
            String type_text = spinner.getSelectedItem().toString();

            //add to database of profiles (not passwords, those are done through the authenticator)
            UserProfile profile = new UserProfile(username_text,email_text,type_text);
            DocumentReference docRef = accountsRef.document(profile.getEmail());
            docRef.set(profile);

            //create account and immediately sign in the user
            createAccount(email_text, password_text);

            //empty the text fields
            ((EditText) findViewById(R.id.signup_user)).setText("");
            ((EditText) findViewById(R.id.signup_pass)).setText("");
            ((EditText) findViewById(R.id.signup_email)).setText("");

        });

        //sign in button listener
        signin_button = findViewById(R.id.button_signin);
        signin_button.setOnClickListener(view -> {
            //get text from edit text fields
            String email_text = ((EditText) findViewById(R.id.signin_email)).getText().toString();
            String password_text = ((EditText) findViewById(R.id.signin_pass)).getText().toString();

            //get type of account from database
            DocumentReference docRef = accountsRef.document(email_text);
            // String type = docRef.get();

            signIn(email_text, password_text, docRef);
        });
    }

    //function for creating account
    private void createAccount(String email, String password) {
        String TAG = "EmailPassword";
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(LoginActivity.this, "Account created!",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign up fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //function for signing into account
    private void signIn(String email, String password, DocumentReference ref) {
        String TAG = "EmailPassword";
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //get type of this account from reference
                            ref.get().addOnSuccessListener(documentSnapshot -> {
                                String type = documentSnapshot.getString("type");

                                //switch activity to next one depending on user type
                                //NEED TO REPLACE PLACEHOLDER ACTIVITIES LATER
                                Intent intent;
                                if (type.equals("Organizer")){
                                    intent = new Intent(LoginActivity.this,HomeOrgActivity.class);
                                } else if (type.equals("Entrant")) {
                                    intent = new Intent(LoginActivity.this,HomeEntActivity.class);
                                } else { //(type == "Admin")
                                    intent = new Intent(LoginActivity.this,HomeAdmActivity.class);
                                }
                                startActivity(intent);
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}