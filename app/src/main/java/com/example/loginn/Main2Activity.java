package com.example.loginn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pd.chocobar.ChocoBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;

public class Main2Activity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mapAPI;
    SupportMapFragment mapFragment;
    FirebaseFirestore db;
    private FirebaseUser user;
    private Marker marker;
    private Marker markerH;
    private Boolean exists;
    Map<String, Object> map;
    String formattedDate;
    String formattedTime;
    boolean shown;
    boolean other;
    FirebaseAuth mAuth;
    Activity activity = getParent();
    ArrayList<String> actions;
    ArrayList<Marker> oldmarker = new ArrayList<>();

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);



        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        Log.i("id", user.getUid() + formattedDate + formattedTime);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapAPI);

        Button buttonH = findViewById(R.id.buttonH);
        Button buttonR = findViewById(R.id.buttonR);
        ImageView imageView2 = findViewById(R.id.imageView2);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAcc();
            }
        });

        buttonR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (marker != null){
                    double longit = marker.getPosition().longitude;
                    double latit = marker.getPosition().latitude;

                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+latit+","+longit);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }else{
                    Log.i("route", "Marker is null");
                }
            }
        });

        buttonH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shown){
                    for (Iterator<Marker> iterator = oldmarker.iterator(); iterator.hasNext(); ) {
                        Marker temp = iterator.next();
                        temp.remove();
                        iterator.remove();
                    }
                    shown = false;
                }else{
                    CollectionReference history = db.collection("history");
                    Query query = history.whereEqualTo("id", user.getUid());
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult() != null){
                                    for (QueryDocumentSnapshot document : task.getResult()){
                                        Log.i("why", "WHY");
                                        Map<String, Object> historyMap = new HashMap<>(document.getData());
                                        if (historyMap.get("latitude") != null && historyMap.get("longitude") != null){
                                            markerH = mapAPI.addMarker(new MarkerOptions()
                                                    .position(new LatLng(Double.valueOf(historyMap.get("latitude").toString()), Double.valueOf(historyMap.get("longitude").toString())))
                                                    .title("Previous marker")
                                                    .snippet("Snippet")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                        }
                                        oldmarker.add(markerH);
                                    }
                                }
                                Log.i("query", "Task successful");
                            }else{
                                Log.i("query", "Task not successful");
                            }
                        }
                    });
                    shown = true;
                }
            }
        });

        requestPermissions(INITIAL_PERMS, 0);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapAPI = googleMap;
        if (perms()){
            googleMap.setMyLocationEnabled(true);
        }else{
            while(!perms()){
                if (perms()){
                    googleMap.setMyLocationEnabled(true);
                }
            }
            if (perms()){
                googleMap.setMyLocationEnabled(true);
            }
        }

        DocumentReference docIdRef = db.collection("markers").document(user.getUid());
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null){
                        if (document.exists()) {
                            Log.d("User document", "Document exists!");
                            //Recreate marker
                            if (document.getData() != null){
                                Map<String, Object> readMap = new HashMap<>(document.getData());
                                marker = mapAPI.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.valueOf(readMap.get("latitude").toString()), Double.valueOf(readMap.get("longitude").toString())))
                                        .title("Previous marker")
                                        .snippet("Snippet")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                exists = true;
                            }
                        } else {
                            Log.d("User document", "Document does not exist!");
                            //Set boolean to false
                            exists = false;
                        }
                    }
                } else {
                    Log.d("User document", "Failed with: ", task.getException());
                }
            }
        });



        mapAPI.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker != null){
                    moveData(marker.getPosition());
                    marker.remove();
                }

                marker = mapAPI.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(latLng.latitude + "  " + latLng.longitude)
                        .snippet("Snippet")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                map = new HashMap<>();
                map.put("longitude", latLng.longitude);
                map.put("latitude", latLng.latitude);

                if (exists){
                    db.collection("markers").document(user.getUid()).update(map);
                }else{
                    db.collection("markers").document(user.getUid()).set(map);
                }
            }
        });
    }

    private void moveData(LatLng latLng){
        Map<String, Object> writeMap = new HashMap<>();
        writeMap.put("latitude", latLng.latitude);
        writeMap.put("longitude", latLng.longitude);
        writeMap.put("id", user.getUid());
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        formattedDate = df.format(c);
        Log.i("date", formattedDate);

        Date date = new Date();
        String strDateFormat = "hh:mm:ss";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        formattedTime = dateFormat.format(date);
        Log.i("time", formattedTime);

        writeMap.put("date", formattedDate);
        writeMap.put("time", formattedTime);
        db.collection("history").document(user.getUid() + formattedDate + formattedTime).set(writeMap);
    }

    private void reAuth(final int id){
        if (!other){
            if (id > 1){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }else{
                final Dialog d = new Dialog(Main2Activity.this);
                d.setTitle("Login");
                d.setContentView(R.layout.dialog_login);
                d.show();

                Button dbutton1 = d.findViewById(R.id.dbutton1);
                Button dbutton2 = d.findViewById(R.id.dbutton2);
                final EditText dedittext1 = d.findViewById(R.id.deditText1);
                final TextView textView = d.findViewById(R.id.textView4);

                dbutton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (user != null){
                            if (user.getEmail() != null){
                                if (dedittext1.getText() != null){
                                    AuthCredential credential = EmailAuthProvider
                                            .getCredential(user.getEmail(), dedittext1.getText().toString());
                                    user.reauthenticate(credential)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (id == 0){
                                                        passwdUpdate();
                                                    }else if (id == 1){
                                                        user.delete();
                                                        Toast.makeText(Main2Activity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                        FirebaseAuth.getInstance().signOut();
                                                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Authentication failed", LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                        d.dismiss();
                    }
                });

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        passwdReset();
                    }
                });

                dbutton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.cancel();
                    }
                });
            }
        }else{
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    private void showAcc(){
        final Dialog d = new Dialog(Main2Activity.this);
        d.setTitle("account");
        d.setContentView(R.layout.dialog_acc);
        d.show();

        final ListView dalistView = d.findViewById(R.id.dalistView);
        final TextView daTextView = d.findViewById(R.id.datextView);

        mAuth.fetchSignInMethodsForEmail(user.getEmail()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (!task.getResult().getSignInMethods().isEmpty()){
                    if (task.getResult().getSignInMethods().get(0).equals("password")){
                        Log.i("Check for email use", "used for email and password authentication");
                        actions = new ArrayList<>(Arrays.asList("Change password", "Delete account", "Sign out"));
                        other = false;
                        if (user.getEmail() != null){
                            daTextView.setText(user.getEmail());
                            Log.i("user display name", user.getEmail());
                        }else{
                            daTextView.setText("Your Account");
                            Log.i("textView", "null");
                        }
                    }else{
                        Log.i("Check for email use", "used for other auth methods");
                        actions = new ArrayList<>(Collections.singletonList("Sign out"));
                        other = true;
                        if (user.getDisplayName() != null) {
                            daTextView.setText(user.getDisplayName());
                            Log.i("user email", user.getDisplayName());
                        }else{
                            daTextView.setText("Your Account");
                            Log.i("textView", "null");
                        }
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, actions);
                    dalistView.setAdapter(arrayAdapter);
                }
            }
        });



        dalistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                reAuth((int) id);
            }
        });
    }

    private void passwdUpdate(){
        final Dialog d = new Dialog(Main2Activity.this);
        d.setTitle("passwd");
        d.setContentView(R.layout.dialog_passwd);
        d.show();

        final Button deButton1 = d.findViewById(R.id.debutton1);
        final Button deButton2 = d.findViewById(R.id.debutton2);
        final EditText deEditText1 = d.findViewById(R.id.deeditText);

        deButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.updatePassword(deEditText1.getText().toString());
            }
        });

        deButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
    }

    private void passwdReset(){
        final String email = user.getEmail();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Reset", "Email sent.");
                            Toast.makeText(getApplicationContext(), "Password reset email has been sent to " + email, LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean perms(){
        String requiredPermission = INITIAL_PERMS[0];
        int checkVal = this.checkCallingOrSelfPermission(requiredPermission);
        return checkVal == PackageManager.PERMISSION_GRANTED;
    }



}
