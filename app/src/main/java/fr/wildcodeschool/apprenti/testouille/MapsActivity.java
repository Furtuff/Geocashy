package fr.wildcodeschool.apprenti.testouille;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        LatLng toulouse = new LatLng(43.601253, 1.442236);
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(toulouse,15);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        // Retrieve caches from firebase DB and place them onto the map
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("geocaches").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        Iterable<DataSnapshot> caches = dataSnapshot.getChildren();
                        for (DataSnapshot snap : caches) {
                            Cache cache = snap.getValue(Cache.class);
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(cache.getLat(), cache.getLon()))
                                    .snippet(cache.getHint())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.interro))
                            );
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("marker error", databaseError.toException());
                    }
                });

        // musique dents de la mer
        Intent svc=new Intent(this, Music.class);
        startService(svc);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // Get LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


            // Create a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Get the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Get Current Location
            Location myLocation = locationManager.getLastKnownLocation(provider);

            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            // Show the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i("requestCode", Integer.toString(requestCode));
        if (requestCode == 1) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
            } else {
                LatLng toulouse = new LatLng(43.601253, 1.442236);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(toulouse));
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
       Intent svc=new Intent(this, Music.class);
       stopService(svc);




                Intent ficheIntent = new Intent(MapsActivity.this,HintActivity.class);
                ficheIntent.putExtra(Constants.IMG_URL, marker.getSnippet());
                startActivity(ficheIntent);


        return false;
    }
    protected void onResume(){
        super.onResume();
        startService(new Intent(this,Music.class));

    }

    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(this,Music.class));

    }
}
