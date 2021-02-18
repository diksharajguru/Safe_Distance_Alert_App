package com.example.workfirebase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Admin extends AppCompatActivity implements View.OnClickListener {
    private TextView register, forgotPassword;
    private EditText editTextEmail, editTextPassword;
    private Button signIn;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);
        signIn = (Button) findViewById(R.id.signIn);
        signIn.setOnClickListener(this);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.register:
                startActivity(new Intent(this, RegisterAdmin.class));
                break;
            case R.id.signIn:
                userLogin();
                break;
            case R.id.forgotPassword:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                break;
        }
    }
    private void userLogin() {
        String email =  editTextEmail.getText().toString().trim();
        String password =  editTextPassword.getText().toString().trim();
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
            editTextPassword.setError("Passowrd is required");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() <6) {
            editTextPassword.setError("Password of at least length of 6 is required");
            editTextPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()) {
                        Task<QuerySnapshot> query = fStore.collection("admin").whereEqualTo("aemail", email).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Toast.makeText(Admin.this, "Logged In Successfully", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                startActivity(new Intent(Admin.this,Admin_Design.class));
                                                finish();
                                            }
                                        } else {
                                            Toast.makeText(Admin.this, "Failed to Login! Please check your credentials", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(Admin.this, "An email has been sent to you...Check your email to verify your account !", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Admin.this, "Failed to Login! Please check your credentials", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
