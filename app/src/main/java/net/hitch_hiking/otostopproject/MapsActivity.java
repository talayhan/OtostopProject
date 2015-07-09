package net.hitch_hiking.otostopproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.login.widget.ProfilePictureView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements GeoQueryEventListener, GoogleMap.OnCameraChangeListener {

    private static final GeoLocation INITIAL_CENTER = new GeoLocation(41.068478, 29.001695);
    private static final int INITIAL_ZOOM_LEVEL = 14;
    private static final String GEO_FIRE_REF = "https://geofiredata.firebaseio.com/coordinates/";
    private static final String FIRE_REF = "https://geofiredata.firebaseio.com/users/";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Circle searchCircle;
    private GeoFire geoFire;
    private GeoQuery geoQuery;

    private Firebase ref;

    private Map<String,Marker> markers;

    /* UI members */
    private DrawerLayout drawerLayout;
    private Toolbar toolbar = null;
    private ActionBarDrawerToggle drawerToggle;

    private FragmentManager fragmentManager;

    private TextView mUserName;
    private TextView mUserEmail;
    private ProfilePictureView mUserPhoto;
    private ImageButton mTravelButton;

    private User myInfo;

    public static int i = 0;

    public GoogleMap.OnInfoWindowClickListener getInfoWindowClickListener()
    {
        return new GoogleMap.OnInfoWindowClickListener()
        {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                Toast.makeText(getApplicationContext(), "Clicked a window with title..." + marker.getId() , Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void addUser(String userName){
        // 41.068632, 29.001866
        geoFire.setLocation(userName, new GeoLocation(41.068632, 29.001866));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_layout);
        setUpMapIfNeeded();

        setupToolbar();
        setupDrawerLayout();
        setupInit();
        setupHeaderViews();
        getUserInfoFromBundle();

        // setup map and camera position
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        this.mMap = mapFragment.getMap();
        LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);
        this.searchCircle = this.mMap.addCircle(new CircleOptions().center(latLngCenter).radius(1000));
        this.searchCircle.setFillColor(Color.argb(66, 255, 0, 255));
        this.searchCircle.setStrokeColor(Color.argb(66, 0, 0, 0));
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, INITIAL_ZOOM_LEVEL));
        this.mMap.setOnCameraChangeListener(this);
        this.mMap.setMyLocationEnabled(true);
        /* Marker click listener is here. */
        this.mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "Clicked a window with title..." + marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Firebase.setAndroidContext(this);

        // setup firebase
        this.ref = new Firebase(FIRE_REF);

        // setup GeoFire
        this.geoFire = new GeoFire(new Firebase(GEO_FIRE_REF));

        // radius in km
        this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);

        this.mTravelButton = (ImageButton) findViewById(R.id.seyahat_button);
        this.mTravelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add
                ref.child(myInfo.getUserId()).setValue(myInfo);
                // get geocode from context
                Location myLocation =  mMap.getMyLocation();
                if (myLocation != null) {
                    LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Toast.makeText(getApplicationContext(), "Latitude: " + myLatLng.latitude + "Longitude: " + myLatLng.longitude, Toast.LENGTH_LONG).show();
                    geoFire.setLocation(myInfo.getUserId(), new GeoLocation(myLatLng.latitude, myLatLng.longitude));
                }
            }
        });

        // Get a reference to our posts
        Firebase newRefTest = new Firebase(FIRE_REF+myInfo.getUserId() +"/isDriver");

        // Attach an listener to read the data at our posts reference
        newRefTest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
                //Toast.makeText(getApplicationContext(), "Snapshot: " + snapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().toString() == "true") {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setMessage("Would you like to chat someone?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // add chat activity
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        //setMockupLoc();
        // setup markers
        this.markers = new HashMap<>();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Hello World"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove all event listeners to stop updating in the background
        this.geoQuery.removeAllListeners();
        for (Marker marker: this.markers.values()) {
            marker.remove();
        }
        this.markers.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // add an event listener to start updating locations again
        this.geoQuery.addGeoQueryEventListener(this);
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        // Add a new marker to the map
        if (myInfo.getIsDriver()) {
            Marker marker = this.mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(new LatLng(location.latitude, location.longitude)));
            this.markers.put(key, marker);
        }else{
            Marker marker = this.mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.latitude, location.longitude)));
            this.markers.put(key, marker);
        }
    }

    @Override
    public void onKeyExited(String key) {
        // Remove any old marker
        Marker marker = this.markers.get(key);
        if (marker != null) {
            marker.remove();
            this.markers.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        // Move the marker
        Marker marker = this.markers.get(key);
        if (marker != null) {
            this.animateMarkerTo(marker, location.latitude, location.longitude);
        }
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // Update the search criteria for this geoQuery and the circle on the map
        LatLng center = cameraPosition.target;
        double radius = zoomLevelToRadius(cameraPosition.zoom);
        this.searchCircle.setCenter(center);
        this.searchCircle.setRadius(radius);
        this.geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
        // radius in km
        this.geoQuery.setRadius(radius / 1000);
    }

    /**
     * @param zoomLevel control zoomlevel 2^zoomLevel */
    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 16384000/Math.pow(2, zoomLevel);
    }

    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed / DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //final ActionBar actionBar = getSupportActionBar();
        /*
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        */

        final ToggleButton toggle = (ToggleButton) findViewById(R.id.togglebutton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // The toggle is enabled
                /* get car info from firebase
                * if info is null then SettingsActivity is opened. */
                    String info = myInfo.getCarInfo();
                    //Firebase car info is assigned to 'info'
                    if (info == ""){
                        android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
                        builder1.setMessage("Please fill the car information to switch on driver mode.");
                        builder1.setCancelable(true);
                        builder1.setPositiveButton("CONTINUE",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        Intent intentForCarInfo = new Intent(MapsActivity.this, SettingsActivity.class);
                                        MapsActivity.this.startActivity(intentForCarInfo);
                                        Log.i("Content ", " App layout ");
                                        dialog.cancel();
                                    }
                                });
                        builder1.setNegativeButton("CLOSE",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        toggle.setChecked(false);
                                        dialog.cancel();
                                    }
                                });

                        android.support.v7.app.AlertDialog alert11 = builder1.create();
                        alert11.show();
                    } else {
                    // The toggle is disabled
                    }
                }
            }
        });

        /* Settinge . */
        final ImageButton settingBtn = (ImageButton) findViewById(R.id.settingsButton);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSettings = new Intent(MapsActivity.this, SettingsActivity.class);
                MapsActivity.this.startActivity(intentSettings);
                Log.i("Content ", " App layout ");
            }
        });
    }

    private void makeToDriverRequestAlert(){
        android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
        String driverName = "Driver Name"; //should be obtained from firebase.
        String driverSurname = "Driver Surname"; //should be obtained from firebase.
        String driverBirthday = "Driver Birthday"; //should be obtained from firebase.
        String driverRating = "Driver Rating"; //should be obtained from firebase.
        String message = driverName+" "+driverSurname+", "+driverBirthday+", "+driverRating;
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("SEND",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    /* otostop request method should be here. */
                        dialog.cancel();
                    }
                });
        builder1.setNegativeButton("CLOSE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.support.v7.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void setupInit() {
        //Location url = getLocation(this);
        fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.content_frame, new MapFragment()).commit();
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass;
        Float elevation = getResources().getDimension(R.dimen.elevation_toolbar);
        fragmentClass = MapFragment.class;
        /* MapFragment burada set ediliyor.
        * Seyahat buttonu da buraya.*/
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.map, fragment);
        fragmentTransaction.addToBackStack("FRAGMENT");
        fragmentTransaction.commit();

        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setElevation(elevation);

        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void getUserInfoFromBundle(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String userName = extras.getString("user_name");
            String userEmail = extras.getString("user_email");
            String userID = extras.getString("user_id");
            fillUserPage(userName,userEmail,userID);
            myInfo = new User(1993, userName, 0, true, userEmail, userID,"");
        }else{
            Toast.makeText(this,"[-]Extras are gotten NULL!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillUserPage(String userName, String userEmail, String userID) {
        mUserName.setText(userName);
        mUserEmail.setText(userEmail);
        // get user profile picture from facebook developer console
        mUserPhoto.setProfileId(userID);
    }

    private void setupHeaderViews(){
        mUserName = (TextView) findViewById(R.id.user_name);
        mUserEmail = (TextView) findViewById(R.id.user_email);
        mUserPhoto = (ProfilePictureView) findViewById(R.id.user_photo);
    }
}
