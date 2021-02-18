package com.example.workfirebase;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
    BluetoothAdapter mBluetoothAdapter;
    private LocationSettingsRequest.Builder builder;
    private final int REQUEST_CHECK_CODE = 8989;
    BluetoothManager manager;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mAuth = FirebaseAuth.getInstance();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
        editTextFullName.setOnClickListener(this);
        checkLocation();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Thread thread = new Thread() {
            public void run() {
                while (true){
                    try {
                        enableDisableBT();
                        Thread.sleep(4000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkLocation() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
        }
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
    public void enableDisableBT(){
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }
    private void registerUser() {
        String email =  editTextEmail.getText().toString().trim();
        String password =  editTextPassword.getText().toString().trim();
        String fullName =  editTextFullName.getText().toString().trim();
        String age =  editTextAge.getText().toString().trim();
        String employeeCompany = employeeCompanyName.getText().toString().trim().toLowerCase();
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
     /*Change 7*/  editTextPassword.setError("Password of length of 6(at least) is required");
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
        /*Change 5*///                Toast.makeText(RegisterUser.this, "Registration Successful..Now please login and verify your emailID", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(RegisterUser.this, Employee.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else{
        /*Change 6*/                 Toast.makeText(RegisterUser.this, "Failed to register, Account with the given emailID already exists", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
}