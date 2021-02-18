package com.example.workfirebase;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BeaconService extends Service implements BluetoothAdapter.LeScanCallback{
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static final String TAG = BeaconService.class.getSimpleName();
    public static ArrayList<String> myList = new ArrayList<String>();
    public static ArrayList<String> dangerList = new ArrayList<String>();
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private Timer myTimer;
    String userID;
    int time= 4;
    private final Handler mHandler = new Handler();
    public BeaconService(){
        super();
    }
    private BluetoothGatt btGatt;
    private BluetoothAdapter mBluetoothAdapter;
    String mUserName = "socialdistancing708@gmail.com", mPassword = "SocialDistance1";
    boolean mIsEmailSend = false;
    private void sendEmail(String recipients,String eName) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    GMailSender sender = new GMailSender(mUserName, mPassword);
                    sender.sendMail("Safe Distance Alert", eName + " is in danger zone "+" with " +myList.size()+ " person.", mUserName, recipients);
                    Log.v("MainActivity", "Your mail has been sent…");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static class GMailSender extends javax.mail.Authenticator {
        private String mailhost = "smtp.gmail.com";
        private String user;
        private String password;
        private Session session;
        private Multipart _multipart = new MimeMultipart();
        static {
            Security.addProvider(new JSSEProvider());
        }
        public GMailSender(String user, String password) {
            this.user = user;
            this.password = password;
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.host", mailhost);
            session = Session.getDefaultInstance(props, this);
        }
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
        }
        public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
            try {
                MimeMessage message = new MimeMessage(session);
                DataHandler handler = new DataHandler(new ByteArrayDataSource(
                        body.getBytes(),"text/plain"));
                message.setSender(new InternetAddress(sender));
                message.setSubject(subject);
                message.setDataHandler(handler);
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(body);
                _multipart.addBodyPart(messageBodyPart);
                message.setContent(_multipart);
                if (recipients.indexOf(',') > 0)
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(recipients));
                else
                    message.setRecipient(Message.RecipientType.TO,
                            new InternetAddress(recipients));
                Transport.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void addAttachment(String filename) throws Exception {
            BodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            _multipart.addBodyPart(messageBodyPart);
            BodyPart messageBodyPart2 = new MimeBodyPart();
            messageBodyPart2.setText("subject43");
            _multipart.addBodyPart(messageBodyPart2);
        }
        public class ByteArrayDataSource implements DataSource {
            private byte[] data;
            private String type;
            public ByteArrayDataSource(byte[] data, String type) {
                super();
                this.data = data;
                this.type = type;
            }
            public ByteArrayDataSource(byte[] data) {
                super();
                this.data = data;
            }
            public void setType(String type) {
                this.type = type;
            }
            public String getContentType() {
                if (type == null)
                    return "application/octet-stream";
                else
                    return type;
            }
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(data);
            }
            public String getName() {
                return "ByteArrayDataSource";
            }
            public OutputStream getOutputStream() throws IOException {
                throw new IOException("Not Supported”");
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        Log.i("On create","App created");
        writeLine("Automate service created...");
        getBTService();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        writeLine("Automate service start...");
        if (!isBluetoothSupported()) {
            stopSelf();
        }else{
            if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled()){
                writeLine("enabled...");
                writeLine("Bluetooth device name : "+mBluetoothAdapter.getName());
                startBLEscan();
            }else{
                writeLine("not...");
                stopSelf();
            }
        }
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
    }
    @Override
    public boolean stopService(Intent name) {
        stopBLEscan();
        writeLine("Automate service stop...");
        stopSelf();
        return super.stopService(name);
    }
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
            }
        }
    };
    public BluetoothAdapter getBTService(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter;
    }
    public boolean isBluetoothSupported() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    public void startBLEscan(){
        mBluetoothAdapter.startLeScan(this);
    }
    public void stopBLEscan(){
        mBluetoothAdapter.stopLeScan(this);
        System.exit(0);
    }
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            startBLEscan();
        } else {
            stopBLEscan();
        }
    }
    public static void enableDisableBluetooth(boolean enable){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if(enable) {
                bluetoothAdapter.enable();
            }else{
                bluetoothAdapter.disable();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
        btnDiscover();
        if(rssi > 52){
            if(!myList.contains(device.getAddress()+" "+device.getName())){
                /*Vibrator v;
                v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
              v.vibrate(50);*/
                long[] pattern = {0,80,800,80,800,80,800,80,800,80,800,80,800,80,800,80,800,80,800,80,800,80,800,80,800,80,800,80,800};
                Vibrator v;
                v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(pattern,-1);
                // Toast.makeText(BeaconService.this, "New device found near to you:  " + device.getName() + ": " + device.getAddress(), Toast.LENGTH_SHORT).show();
                myList.add(device.getAddress()+" "+device.getName());
                if(myList.size()>=1) {
                    new Handler().postDelayed(new Runnable(){
                        public void run(){
                            searchForAdmin();
                            uploadList(myList);
                        }
                    },120000);
                }
            }
        }
    }
    public static final class JSSEProvider extends Provider {
        private static final long serialVersionUID = 1L;
        public JSSEProvider() {
            super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
            AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    put("SSLContext.TLS", "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                    put("Alg.Alias.SSLContext.TLSv1", "TLS");
                    put("KeyManagerFactory.X509","org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                    put("TrustManagerFactory.X509", "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                    return null;
                }
            });
        }
    }
    BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            writeLine("Automate service connection state: "+ newState);
            if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED){
                writeLine("Automate service connection state: STATE_CONNECTED");
                Log.v("BLEService", "BLE Connected now discover services");
                Log.v("BLEService", "BLE Connected");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        writeLine("Automate service go for discover services");
                        gatt.discoverServices();
                    }
                }).start();
            }else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED){
                writeLine("Automate service connection state: STATE_DISCONNECTED");
                Log.v("BLEService", "BLE Disconnected");
            }
        }
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeLine("Automate service discover service: GATT_SUCCESS");
                Log.v("BLEService", "BLE Services onServicesDiscovered");
                //Get service
                List<BluetoothGattService> services = gatt.getServices();

            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };
    private void writeLine(final String message) {
        Handler h = new Handler(getApplicationContext().getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");
            //check BT permissions in manifest
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }
    public void searchForAdmin(){
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null){
            DocumentReference documentReference = fStore.collection("employee").document(userID);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()){
                            String companyName = documentSnapshot.getString("company");
                            String employeeName = documentSnapshot.getString("fName");
                            Task<QuerySnapshot> query = fStore.collection("admin").whereEqualTo("acompany", companyName).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    // Toast.makeText(BeaconService.this, "You are found in danger..Sending email to : "+document.get("aemail"), Toast.LENGTH_SHORT).show();
                                                    sendEmail(document.getString("aemail"),employeeName);
                                                }
                                            } else {
                                                Log.d("Function Query", "Error getting documents: ", task.getException());
                                                //Toast.makeText(BeaconService.this, "Could not find admin of your company.. Hence, cannot send mail", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }else{
                            Log.i("Function Snapshot","Document snapshot not exists");
                        }
                    }else{
                        Log.i("Function Error","Task unsuccessful");
                    }
                }
            });
        }else{
            Log.i("User : ","Null");
        }
    }
    public void uploadList(List<String> myList){
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null){
            DocumentReference documentReference = fStore.collection("employee").document(userID);
            Map<String,Object> user = new HashMap<>();
            user.put("dangerList : ",myList);
            documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    writeLine("Uploading Danger List");
                }
            });
        }
    }
}
