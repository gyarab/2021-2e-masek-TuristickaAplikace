package com.example.sightseer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener /*LocationListener*/ {
    ImageButton bt;
    Button signin;
    Button take1;
    Button take2;
    Button take3;
    ImageButton map1;
    TextView latText;
    TextView lngText;
    FusedLocationProviderClient fLPC;
    List<Address> addresses;
    RelativeLayout rl;
    ImageView iw1;
    ImageView iw2;
    ImageView iw3;
    double latitude;
    double longitude;
    double[] lats = new double[3];
    double[] lngs = new double[3];
    String[] jmena = new String[3];
    String[] adresy = new String[3];
    double[] vysledky = new double[3];
    Bitmap bmp;
    DatabaseReference databaseRef;

    //private ActivityMainBinding binding;
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "GOOGLE_SIGN_IN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);*/
        latText = findViewById(R.id.lat);
        lngText = findViewById(R.id.lng);
        bt = findViewById(R.id.button);
        signin = findViewById(R.id.signin);
        take1 = findViewById(R.id.take1);
        take2 = findViewById(R.id.take2);
        take3 = findViewById(R.id.take3);
        map1 = findViewById(R.id.map1);
        rl = findViewById(R.id.rl);
        iw1 = findViewById(R.id.imageView1);
        iw2 = findViewById(R.id.imageView2);
        iw3 = findViewById(R.id.imageView3);
        fLPC = LocationServices.getFusedLocationProviderClient(this);
        lats[0] = 50.080306;
        lats[1] = 50.099;
        lats[2] = 50.087;
        lngs[0] = 14.429349;
        lngs[1] = 14.359;
        lngs[2] = 14.421;
        jmena[0] = "Svatý Václav";
        jmena[1] = "Okrasná lavička";
        jmena[2] = "Pražský Orloj";
        adresy[0] = "https://cdn.discordapp.com/attachments/714896462957379637/968958574485196840/20220203_134154.jpg";
        adresy[1] = "https://cdn.discordapp.com/attachments/714896462957379637/968958589999935558/20220203_131650.jpg";
        adresy[2] = "https://cdn.discordapp.com/attachments/714896462957379637/968958600422764554/20220203_140345.jpg";
        bt.setOnClickListener(this);
        take1.setOnClickListener(this);
        take2.setOnClickListener(this);
        take3.setOnClickListener(this);
        signin.setOnClickListener(this);
        map1.setOnClickListener(this);

        //Nastaveni Google prihlaseni
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //Ted Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
            sortLocations();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 44);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLocation();
                sortLocations();
                /*ImageView lol = new ImageView(getApplicationContext());
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(300, 700, 0, 0);
                lol.setLayoutParams(lp);*/
            } else /*if (v.getId() == R.id.take)*/ {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        } else if (v.getId() == R.id.map1) {
            if (vysledky[0] < vysledky[1] && vysledky[0] < vysledky[2]) {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[0]);
                b.putDouble("long", lngs[0]);
                intent.putExtras(b);
                startActivity(intent);
            } else if (vysledky[1] < vysledky[0] && vysledky[1] < vysledky[2]) {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[1]);
                b.putDouble("long", lngs[1]);
                intent.putExtras(b);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[2]);
                b.putDouble("long", lngs[2]);
                intent.putExtras(b);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.map2) {
            if ((vysledky[0] < vysledky[1] && vysledky[0] > vysledky[2]) || (vysledky[0] > vysledky[1] && vysledky[0] < vysledky[2])) {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[0]);
                b.putDouble("long", lngs[0]);
                intent.putExtras(b);
                startActivity(intent);
            } else if ((vysledky[1] < vysledky[0] && vysledky[1] > vysledky[2]) || (vysledky[1] > vysledky[0] && vysledky[1] < vysledky[2])) {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[1]);
                b.putDouble("long", lngs[1]);
                intent.putExtras(b);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[2]);
                b.putDouble("long", lngs[2]);
                intent.putExtras(b);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.map3) {
            if (vysledky[0] > vysledky[1] && vysledky[0] > vysledky[2]) {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[0]);
                b.putDouble("long", lngs[0]);
                intent.putExtras(b);
                startActivity(intent);
            } else if (vysledky[1] > vysledky[0] && vysledky[1] > vysledky[2]) {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[1]);
                b.putDouble("long", lngs[1]);
                intent.putExtras(b);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, Mapa.class);
                Bundle b = new Bundle();
                b.putDouble("lat", lats[2]);
                b.putDouble("long", lngs[2]);
                intent.putExtras(b);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.signin) {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        } else if (v.getId() == R.id.take1) {
            //Picasso.get().load("https://play-lh.googleusercontent.com/8ddL1kuoNUB5vUvgDVjYY3_6HwQcrg1K2fd_R8soD-e2QYj8fT9cfhfh3G0hnSruLKec").into(iw1);
//            Picasso.get().load(adresy[2]).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    // Set it in the ImageView
//                    bmp = bitmap;
//                }
//
//                @Override
//                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                }
//            });
            databaseRef.child("test").setValue("take1");
            BitmapDrawable drawable = (BitmapDrawable) iw1.getDrawable();
            bmp = drawable.getBitmap();
            OpenCVLoader.initDebug();
            //Bitmap bmp = getBitmapFromURL(adresy[0]);
            Mat test = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bmp, test);
            Intent a = new Intent(this, Take.class);
            Bundle bundle = new Bundle();
            bundle.putLong("addr", test.getNativeObjAddr());
            a.putExtras(bundle);
            startActivity(a);
        } else if (v.getId() == R.id.take2) {
            BitmapDrawable drawable = (BitmapDrawable) iw2.getDrawable();
            bmp = drawable.getBitmap();
            OpenCVLoader.initDebug();
            //Bitmap bmp = getBitmapFromURL(adresy[0]);
            Mat test = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bmp, test);
            Intent a = new Intent(this, Take.class);
            Bundle bundle = new Bundle();
            bundle.putLong("addr", test.getNativeObjAddr());
            a.putExtras(bundle);
            startActivity(a);
        } else if (v.getId() == R.id.take3) {
            BitmapDrawable drawable = (BitmapDrawable) iw3.getDrawable();
            bmp = drawable.getBitmap();
            OpenCVLoader.initDebug();
            //Bitmap bmp = getBitmapFromURL(adresy[0]);
            Mat test = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bmp, test);
            Intent a = new Intent(this, Take.class);
            Bundle bundle = new Bundle();
            bundle.putLong("addr", test.getNativeObjAddr());
            a.putExtras(bundle);
            startActivity(a);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fLPC.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //textView.setText("Latitude: " + addresses.get(0).getLatitude() + " Longitude: " + addresses.get(0).getLongitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    latText.setText(Double.toString(location.getLatitude()));
                    lngText.setText(Double.toString(location.getLongitude()));
                }
            }
        });
    }

    /*public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public void sortLocations() {
        for (int i = 0; i < 3; i++) {
            vysledky[i] = Math.sqrt((Math.abs(latitude - lats[i]) * Math.abs(latitude - lats[i])) + (Math.abs(longitude - lngs[i]) * Math.abs(longitude - lngs[i])));
        }
        if (vysledky[1] < vysledky[2] && vysledky[1] < vysledky[0]) {
            take1.setText(jmena[1]);
            Picasso.get().load(adresy[1]).into(iw1);
            if (vysledky[2] < vysledky[0]) {
                take2.setText(jmena[2]);
                Picasso.get().load(adresy[2]).into(iw2);
                take3.setText(jmena[0]);
                Picasso.get().load(adresy[0]).into(iw3);
            } else {
                take2.setText(jmena[0]);
                Picasso.get().load(adresy[0]).into(iw2);
                take3.setText(jmena[2]);
                Picasso.get().load(adresy[2]).into(iw3);
            }
        } else if (vysledky[2] < vysledky[1] && vysledky[2] < vysledky[0]) {
            take1.setText(jmena[2]);
            Picasso.get().load(adresy[2]).into(iw1);
            if (vysledky[1] < vysledky[0]) {
                take2.setText(jmena[1]);
                Picasso.get().load(adresy[1]).into(iw2);
                take3.setText(jmena[0]);
                Picasso.get().load(adresy[0]).into(iw3);
            } else {
                take2.setText(jmena[0]);
                Picasso.get().load(adresy[0]).into(iw2);
                take3.setText(jmena[1]);
                Picasso.get().load(adresy[1]).into(iw3);
            }
        }
        else {
            take1.setText(jmena[0]);
            Picasso.get().load(adresy[0]).into(iw1);
            if (vysledky[1] < vysledky[2]) {
                take2.setText(jmena[1]);
                Picasso.get().load(adresy[1]).into(iw2);
                take3.setText(jmena[2]);
                Picasso.get().load(adresy[2]).into(iw3);
            } else {
                take2.setText(jmena[2]);
                Picasso.get().load(adresy[2]).into(iw2);
                take3.setText(jmena[1]);
                Picasso.get().load(adresy[1]).into(iw3);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                firebaseAuthWithGoogleAccount(account);
            } catch (Exception e) {
                System.out.println("Login failed!");
            }
        }
    }

    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        System.out.println("Firebase signin successful");
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();
                        String email = firebaseUser.getEmail();
                        if (authResult.getAdditionalUserInfo().isNewUser()) {
                            Toast.makeText(MainActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                        }
                        signin.setText(email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Firebase signin failed");
                    }
                });
    }
}