package com.example.workfirebase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothManager;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class Employee extends AppCompatActivity implements View.OnClickListener {
    private TextView register, admin, forgotPassword;
    private EditText editTextEmail, editTextPassword;
    private Button signIn;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    BluetoothManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);
        manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);
        admin = (TextView) findViewById(R.id.admin);
        admin.setOnClickListener(this);
        signIn = (Button) findViewById(R.id.signIn);
        signIn.setOnClickListener(this);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.register:
                startActivity(new Intent(this,RegisterUser.class));
                break;
            case R.id.signIn:
                userLogin();
                break;
            case R.id.admin:
                startActivity(new Intent(this,Admin.class));
                break;
            case R.id.forgotPassword:
                startActivity(new Intent(this,ResetPasswordActivity.class));
                break;
        }
    }
    private void userLogin(){
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
                        startActivity(new Intent(Employee.this,MainActivity.class));
                        Toast.makeText(Employee.this, "Logged In Successfully", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(Employee.this, "An email has been sent to you...Check your email to verify your account !", Toast.LENGTH_LONG).show();
                    }


                }else{
                    Toast.makeText(Employee.this, "Failed to Login! Please check your credentials", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}