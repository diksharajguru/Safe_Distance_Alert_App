package com.example.workfirebase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private TextView disFullName;
    Button logout;
    String userID;
    String msg = "Android : ";
    BluetoothAdapter mBluetoothAdapter;
    private LocationSettingsRequest.Builder builder;
    private final int REQUEST_CHECK_CODE = 8989;
    BluetoothManager manager;
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        disFullName = findViewById(R.id.displayFullName);
        logout = findViewById(R.id.logout);
        startService(new Intent(getBaseContext(), BeaconService.class));
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                stopService();
                startActivity(new Intent(MainActivity.this,Employee.class));
                finish();
            }
        });
        Log.d(msg, "The onCreate() event");
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
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null){
            userID = mAuth.getCurrentUser().getUid();
            DocumentReference documentReference = fStore.collection("employee").document(userID);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    disFullName.setText(value.getString("fName"));
                }
            });
        }
    }
    public void enableDisableBT(){
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }
    public void stopService() {
        Toast.makeText(this, "Stopping Bluetooth Scanning", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), BeaconService.class));
        mBluetoothAdapter.disable();
    }
}