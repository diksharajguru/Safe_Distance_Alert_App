package com.example.workfirebase;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static java.security.AccessController.getContext;
public class Employee extends AppCompatActivity implements View.OnClickListener {
    private TextView register, admin, forgotPassword;
    private EditText editTextEmail, editTextPassword;
    private Button signIn;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private ProgressBar progressBar;
    BluetoothManager manager;
    BluetoothAdapter mBluetoothAdapter;
    private LocationSettingsRequest.Builder builder;
    private final int REQUEST_CHECK_CODE = 8989;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);
        checkLocation();
        LocationRequest request = new LocationRequest().setFastestInterval(1500).setInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder= new LocationSettingsRequest.Builder().addLocationRequest(request);
        Task<LocationSettingsResponse> result= LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()){
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(Employee.this,REQUEST_CHECK_CODE);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            } catch (ClassCastException ex){

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:{
                            break;
                        }
                    }
                }
            }
        });
        manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enabledisableBT();
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
    public void enabledisableBT(){
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.register:
                startActivity(new Intent(Employee.this, RegisterUser.class));
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
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() <6) {
      /*Change 9*/   editTextPassword.setError("Password of length of 6(at least) is required");
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
                        Task<QuerySnapshot> query = fStore.collection("employee").whereEqualTo("email", email).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Toast.makeText(Employee.this, "Logged In Successfully", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                startActivity(new Intent(Employee.this,MainActivity.class));
                                                finish();
                                            }
                                        } else {
                                            Toast.makeText(Employee.this, "Failed to Login! Please check your credentials", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
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