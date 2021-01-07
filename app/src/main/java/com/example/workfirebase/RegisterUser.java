package com.example.workfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    private TextView banner, registeruser;

    private EditText editTextFullName, editTextAge, editTextPassword, editTextEmail, employeeCompanyName;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        banner = (TextView) findViewById(R.id.banner);
        banner.setOnClickListener(this);

        registeruser = (Button) findViewById(R.id.registerUser);
        registeruser.setOnClickListener(this);

        editTextFullName = (EditText) findViewById(R.id.fullName);
        editTextAge = (EditText) findViewById(R.id.age);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextEmail = (EditText) findViewById(R.id.email);
        employeeCompanyName = (EditText) findViewById(R.id.employeeCompanyName);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.banner:
                startActivity(new Intent(RegisterUser.this, Employee.class));
                break;
            case R.id.registerUser:
                registerUser();
                break;
        }

    }

    private void registerUser() {
        String email =  editTextEmail.getText().toString().trim();
        String password =  editTextPassword.getText().toString().trim();
        String fullName =  editTextFullName.getText().toString().trim();
        String age =  editTextAge.getText().toString().trim();
        String employeeCompany = employeeCompanyName.getText().toString().trim();

        if (employeeCompany.isEmpty()) {
            employeeCompanyName.setError("Company Name is required");
            employeeCompanyName.requestFocus();
            return;
        }



        if (fullName.isEmpty()) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            return;
        }

        if (age.isEmpty()) {
            editTextAge.setError("Age is required");
            editTextAge.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please provide valid email");
            editTextEmail.requestFocus();
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() <6) {
            editTextPassword.setError("Password of at least length of 6 is required");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("employee").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fName",fullName);
                            user.put("email",email);
                            user.put("age",age);
                            user.put("pwd",password);
                            user.put("company",employeeCompany);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterUser.this, "User created", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(RegisterUser.this, Employee.class);
                                    startActivity(intent);
                                }
                            });
                        } else{
                            Toast.makeText(RegisterUser.this, "Failed to register", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
}