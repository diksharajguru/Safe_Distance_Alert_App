package com.example.workfirebase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
public class RegisterAdmin extends AppCompatActivity implements View.OnClickListener {
    private TextView banner, registerAdmin;
    private EditText FullName, Age, Password, Email, companyName;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_admin);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        banner = (TextView) findViewById(R.id.banner);
        banner.setOnClickListener(this);
        registerAdmin = (Button) findViewById(R.id.registerAdmin);
        registerAdmin.setOnClickListener(this);
        FullName = (EditText) findViewById(R.id.Name);
        Age = (EditText) findViewById(R.id.agee);
        Password = (EditText) findViewById(R.id.pwd);
        Email = (EditText) findViewById(R.id.Email);
        companyName = (EditText) findViewById(R.id.companyName);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.banner:
                startActivity(new Intent(this, Admin.class));
                break;
            case R.id.registerAdmin:
                registerAdmin();
                break;
        }
    }
    private void registerAdmin() {
        String email =  Email.getText().toString().trim();
        String password =  Password.getText().toString().trim();
        String fullName =  FullName.getText().toString().trim();
        String age =  Age.getText().toString().trim();
        String company = companyName.getText().toString().trim().toLowerCase();
        if (company.isEmpty()) {
            companyName.setError("Company Name is required");
            companyName.requestFocus();
            return;
        }
        if (fullName.isEmpty()) {
            FullName.setError("Full name is required");
            FullName.requestFocus();
            return;
        }
        if (age.isEmpty()) {
            Age.setError("Age is required");
            Age.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            Email.setError("Email is required");
            Email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email.setError("Please provide valid email");
            Email.requestFocus();
        }
        if (password.isEmpty()) {
            Password.setError("Password is required");
           Password.requestFocus();
            return;
        }
        if (password.length() <6) {
            Password.setError("Password of at least length of 6 is required");
            Password.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("admin").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("aName",fullName);
                            user.put("aemail",email);
                            user.put("aage",age);
                            user.put("apwd",password);
                            user.put("acompany",company);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
     /*Change 13*/                  //Toast.makeText(RegisterAdmin.this, "Registration Successful..Now please login and verify your emailID", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(RegisterAdmin.this, Admin.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else{
    /*Change 14*/                    Toast.makeText(RegisterAdmin.this, "Failed to register, Account with the given emailID already exists", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
}