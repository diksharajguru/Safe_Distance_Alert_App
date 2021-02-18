package com.example.workfirebase;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
//Change 16
public class Admin_Design extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String userID;
    FirebaseFirestore fStore;
    Button logout2;
    private TextView disFullName3;
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__design);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        logout2 = findViewById(R.id.logout2);
        disFullName3 = findViewById(R.id.displayFullName3);
        logout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(Admin_Design.this,Admin.class));
                finish();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null){
            userID = mAuth.getCurrentUser().getUid();
            DocumentReference documentReference = fStore.collection("admin").document(userID);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    disFullName3.setText(value.getString("aName"));
                }
            });
        }
    }
}