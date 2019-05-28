package com.example.nochances;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nochances.Model.Intents;
import com.example.nochances.Services.TrackingService;
import com.example.nochances.fragments.settingsPrefFragment;
import com.example.nochances.utils.constant;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    /** FIELDS
     *  1. LOCATION_PERMISSION_ACCESS: code for permission for
     *  ACCESS_FINE_LOCATION which will be used by the onRequestPermissionsResult
     *  method.
     *  2. mMap: the GoogleMap we will display
     *  3. TAG: tag for debugging
     *  4. geoFenceLimits: a circle which denotes the limit of a specific geofence!
     *  5. userCurrPosMarker: a marker which will point to the position of the user.
     *  6. serviceConnection: a connection to the TrackingService. Helps us bind to the service...
     *  7. clientSender: a Messenger which points to the server
     *  8. clientReceiver: a Messenger which points to the current context and will receive messages.
     *  9. isBound: are we bound to the service?
     *  10. waitingForSuccessfulUnregistering: are we waiting for a signal from the TrackingService
     *  that this Activity has unbounded so we can retry bounding to it?
     *  11. enemyMarkers: an arraylist of markers that will signify the enemies
     *  12. circleColor: the color of the inside of the circle, to be decided by the user's
     *  set generalAlarmLevel and the average of the alarm levels of the enemies
     *  13. collectiveAlarmLevel: the average of the alarm levels of the approaching enemies
     *  14. middleCircle: the middle circle in the map
     *  15. innerCircle: the inner circle in the map
     *  16. sharedPreferences: a preferences object for this current user in this app!
     */
    private static final int LOCATION_PERMISSION_ACCESS = 1;
    private static final String TAG = "TagMapsActivity";

    private GoogleMap mMap;
    private Circle geoFenceLimits;
    private Marker userCurrPosMarker;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected()");
            // get a pointer (Messenger) to the server
            clientSender = new Messenger(iBinder);
            // send a message to TrackingService of registering the client
            try {
                // get a blank message to TrackingService which will register a client
                Message registeringClientMsg = Message.obtain(null, TrackingService.MSG_REGISTER_CLIENT);
                // when the server gets this message, it will register the client but it need to know
                // who to reply to, so we provide a "sender" address with our message
                registeringClientMsg.replyTo = clientReceiver;
                // send the message
                if(clientSender != null) { // a sudden disconnection could occur!
                    clientSender.send(registeringClientMsg);
                } else {
                    Log.w(TAG, "clientSender suddenly became null!");
                }
            } catch(RemoteException e){
                // there was some problem in sending the register message!
                Log.w(TAG, "Problem sending a register message!");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            // there is no server to send to anymore!
            clientSender = null;
        }
    };
    private Messenger clientSender;
    private final Messenger clientReceiver = new Messenger(new IncomingMessageHandler());
    private boolean isBound = false;
    private boolean waitingForSuccessfulUnregistering = false;
    private ArrayList<Marker> enemyMarkers = new ArrayList<>();
    private int circleColor;
    private int collectiveAlarmLevel;
    private Circle middleCircle, innerCircle;
    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // initially the circle color is green
        circleColor = constant.valueToAlarmLevelColor(1);

        // initialize shared preferences
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MapsActivity.this);

        // check if we have permissions for location access and if not request them.
        checkPermissions();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d(TAG, "onMapReady()");
        // when the map is ready, just focus on the current (last known) location!
        getCurrentLocation();
    }

    /**
     * Activity Lifecycle method.
     * Invoking getCurrentLocation to focus on the user's current whereabouts!
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        getCurrentLocation();
        // if we have the necessary permissions, we can also try to start and bind to the
        // service if he haven't done so already!
        if(ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // start the TrackingService if it isn't running alraedy
            if (!TrackingService.isRunning && !sharedPreferences.getBoolean(
                    settingsPrefFragment.ENABLE_TRACKING, false)) {
                startForegroundService(new Intent(MapsActivity.this, TrackingService.class));
            }
            // bind to the TrackingService
            if (!waitingForSuccessfulUnregistering) {
                if (!isBound) {
                    Log.d(TAG, "onResume() binding!");
                    // we can only attempt binding if we are NOT waiting for
                    // a message of successful unregistering / unbinding AND
                    // if we are NOT already bound to it!
                    bindService(new Intent(MapsActivity.this, TrackingService.class),
                            mConnection, Context.BIND_AUTO_CREATE);
                    isBound = true; // we are now bound to the service!
                }
            }
        }
    }

    /**
     * When the activity goes on Pause, we should unbind from the service.
     * We don't kill the service necessarily though, as we should still be sending
     * updates to the cloud!
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        try {
            // unbind from service!
            doUnbindService();
        } catch (Throwable t) {
            // if we fail to unbind, issue a warning!
            Log.w(TAG, "Failed to unbind from service!");
        }
    }

    /**
     * Check for permissions (location permissions)
     * If these aren't granted, request them!
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // (at least) one of the permissions isn't granted
            // so request permissions!
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_ACCESS);
        } else {
            // start the TrackingService if it isn't running alraedy
            if(!TrackingService.isRunning && !sharedPreferences.getBoolean(
                    settingsPrefFragment.ENABLE_TRACKING, false)) {
                startForegroundService(new Intent(MapsActivity.this, TrackingService.class));
            }
            // bind to the TrackingService
            if(!waitingForSuccessfulUnregistering && !isBound) {
                Log.d(TAG, "onCreate binding!");
                // we can only attempt binding if we are NOT waiting for
                // a message of successful unregistering / unbinding AND
                // if we are NOT alraedy bound to it!
                bindService(new Intent(MapsActivity.this, TrackingService.class),
                        mConnection, Context.BIND_AUTO_CREATE);
                isBound = true; // we are now bound to the service!
            }
        }
    }

    /**
     * @param requestCode: the code for the permission we asked for
     * @param permissions: a list of strings representing the permissions we asked for
     * @param grantResults: a list of outcomes, whether the permissions have been granted or not
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ACCESS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // the permission has been granted! We have access to location!
                    Toast toast = Toast.makeText(this,
                            "Permissions Granted!",
                            Toast.LENGTH_LONG);
                    toast.show();
                    // show a relevant toast!
                    Log.d(TAG, "Permissions granted!");
                    // first, get the current location and point the map there, so that even
                    // if something goes wrong in the Service machinery, the user will just still
                    // see where they are in the map and just that!
                    getCurrentLocation();
                    // start the TrackingService if the service isn't already running!
                    if (!TrackingService.isRunning && !sharedPreferences.getBoolean(
                            settingsPrefFragment.ENABLE_TRACKING, false)) {
                        startForegroundService(new Intent(MapsActivity.this, TrackingService.class));
                    }
                    // bind to the TrackingService
                    if (!waitingForSuccessfulUnregistering && !isBound) {
                        // we can only attempt binding if we are NOT waiting to for
                        // a message of successful unregistering / unbinding AND
                        // if we haven't bound to the service already!
                        bindService(new Intent(MapsActivity.this, TrackingService.class),
                                mConnection, Context.BIND_AUTO_CREATE);
                        isBound = true; // we are now bound to the service!
                    }
                }
            } else {
                // we didn't get the permissions!
                // show a relevant toast
                Toast.makeText(MapsActivity.this,
                        "Permissions Denied!",
                        Toast.LENGTH_LONG).show();
                // has the user clicked on the "Don't show again" button?
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (!showRationale) {
                    openSettingsDialog();
                }
            }
            /*
             * Other 'case' lines to follow!
             * We will be requesting more permissions!
             */
        }
    }

    /**
     * In case the user has clicked on the "don't show again button",
     * we show a dialog prompting them to go to the settings and add
     * the necessary permissions manually!
     */
    private void openSettingsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app needs your location information to help you avoid the people you don't like! Grant this permission in the settings!");
        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * Inflating the menus!
     * @param menu: The menu which we will inflate
     * @return: true if all goes good.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * What happens when we click on a menu item?
     * @param item: a menu item which will be clicked on.
     * @return: true if all goes good.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.EnemiesList) {
            try {
                // unbind from service!
                doUnbindService();
            } catch (Throwable t) {
                // if we fail to unbind, issue a warning!
                Log.w(TAG, "Failed to unbind from service!");
            }
            Intent intent = Intents.MapsActivityToEnemiesList(MapsActivity.this);
            startActivity(intent);
            return true;
        }
        if(id == R.id.settings) {
            Intent i = Intents.MapsActivityToSettingsActivity(MapsActivity.this);
            startActivity(i);
        }
        return false;
    }

    /**
     * Gets the current location and updates the map!
     */
    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // if we have the permission to get location information from the user....
            // instantiate a location manager object
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(locationManager != null) {
                // if the locationManager exists...
                // pick the most suitable provider based on some criteria
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                String provider = locationManager.getBestProvider(criteria, true);

                // get the user's current (last known) location and update the map
                Location current_location = locationManager.getLastKnownLocation(provider);
                if(current_location != null) {
                    LatLng current_lat_lng = new LatLng(current_location.getLatitude(),
                            current_location.getLongitude());

                    updateMap(current_lat_lng);
                }
            } else {
                // issue a warning
                Log.w(TAG, "The location manager is null!");
            }
        }
    }

    /**
     * Updates the Google map by putting a focus on a location and a circle of specified radius
     * around it.
     * @param curr_loc: the location which will be the circle's center
     */
    public void updateMap(LatLng curr_loc) {
        if (curr_loc != null && mMap != null) {
            // if there already exists a marker for the user's location, first remove it.
            if (userCurrPosMarker != null) {
                userCurrPosMarker.remove();
            }
            userCurrPosMarker = mMap.addMarker(new MarkerOptions().position(curr_loc).title("You are here!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curr_loc));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curr_loc, 18));
        }
        drawGeofence(curr_loc);
    }
    /**
     * Drawing geofence on the GoogleMap.
     * If there already is a geofence drawn, remove it.
     */
    private void drawGeofence(LatLng locationLatLng) {
        // we can't draw on a null map!
        if(mMap == null)
            return;
        Log.d(TAG, "drawGeofence()");

        // if there is a circle already, remove the circle!
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        // make the OUTER circle
        // the outer circle color is decided by how many enemies are in the circle,
        // what their alarm levels are and what is the user's general alarm level...
        CircleOptions OuterCircleOptions = new CircleOptions()
                .center(locationLatLng)
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor(circleColor)
                .radius(constant.outerRadiusInMeters(this) );
        // TODO: modify strokeColor a bit too!
        // add the circle to the map!
        geoFenceLimits = mMap.addCircle( OuterCircleOptions );


        // add the middle and inner circles on the map!
        if( middleCircle != null)
            middleCircle.remove();

        CircleOptions MiddleCircleOptions = new CircleOptions()
                .center(locationLatLng)
                .strokeColor(Color.rgb(0,0,0))
                .fillColor(circleColor)
                .radius(constant.middleRadiusInMeters(this));
        middleCircle = mMap.addCircle(MiddleCircleOptions);

        if( innerCircle != null)
            innerCircle.remove();

        CircleOptions PhoneCallCircleOptions = new CircleOptions()
                .center(locationLatLng)
                .strokeColor(Color.rgb(0,0,0))
                .fillColor(circleColor)
                .radius(constant.phoneCallRadiusInMeters(this));
        innerCircle = mMap.addCircle(PhoneCallCircleOptions);

    }

    /**
     * Send a message of unregistering a client and unbind the service.
     */
    private void doUnbindService(){
        Log.d(TAG, "doUnbindService()");
        // if this activity is bound to TrackingService and connected to it...
        if(isBound && clientSender != null) {
            // try to send a message of unregistering the client!
            try {
                Message unregisteringClientMsg = Message.obtain(null, TrackingService.MSG_UNREGISTER_CLIENT);
                clientSender.send(unregisteringClientMsg);
                waitingForSuccessfulUnregistering = true;
                unbindService(mConnection);
                isBound = false; // we are not bound anymore!
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to send message of unregistering!");
            }
        }
    }

    /**
     * MESSAGING METHODS / CLASSES
     *      1. IncomingMessageHandler: a class which will handle incoming messages from the
     *      server. Those messages will be:
     *          a. updates on the user's location!
     */
    @SuppressLint("HandlerLeak")
    private class IncomingMessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TrackingService.MSG_LOCATION_UPDATE:
                    // a message saying that the location has been updated has arrived!
                    // We want to update the map!

                    // First open the message and get the new location coordinates out of the bundle!
                    double [] newLatLng = msg.getData().getDoubleArray(TrackingService.LOCATION_UPDATE);
                    // also get a list of the approaching enemies locations
                    double [] approachingEnemiesLocations = msg.getData().getDoubleArray
                            (TrackingService.APPROACHING_ENEMIES);
                    // get the average of their alarm levels!
                    collectiveAlarmLevel = msg.getData().getInt(TrackingService.COLLECTIVE_ALARM_LEVEL);
                    Log.d(TAG, "collectiveAlarmLevel = "+collectiveAlarmLevel);
                    // see if the collective alarm level exceeds the user's picked general
                    // alarm level threshold
                    circleInsideAlarmColor();
                    // has someone entered the inner circle for the first time?
                    // TODO: add inner vs outer circle in preferences!
                    boolean enemyInInnerCircleForTheFirstTime = msg.getData().getBoolean
                            (TrackingService.INNER_CIRCLE_ENTRY);
                    // get if an enemy has entered the inner circle for the first time
                    if(enemyInInnerCircleForTheFirstTime) {
                        // if so, vibrate the phone!
                        vibratePhone();
                    }
                    if(newLatLng != null && newLatLng.length >= 2) {
                        Log.d(TAG, "about to update map with new location!");
                        // if our array has as much data as we need, we get the coordinates out
                        // and update our map!
                        updateMap(new LatLng(newLatLng[0], newLatLng[1]));
                    }
                    if(constant.DBG_APPROACHING_ENEMIES_MARK && approachingEnemiesLocations != null) {
                        markApproachingEnemies(approachingEnemiesLocations);
                    }
                    // do we have to issue a fake phone call?
                    boolean makePhoneCall = msg.getData().getBoolean(TrackingService.MAKE_PHONE_CALL);
                    if(makePhoneCall) {
                        fakeCall();
                    }
                    break;
                case TrackingService.MSG_SUCCESSFUL_UNREGISTERING:
                    Log.d(TAG, "received message of successful unregistering!");
                    // a message saying that we have successfully unregistered
                    // now we can safely start trying to bind back into the service again!
                    waitingForSuccessfulUnregistering = false;
                    break;
            }
        }
    }

    /**
     * Puts a marker on the map on the locations of approaching enemies
     * @param approachingEnemiesLocations: the locations of approaching enemies
     */
    private void markApproachingEnemies(double [] approachingEnemiesLocations){
        // the size of the enemyLocations array has to be even, as it consists of pairs of latitude
        // and longitude!
        if(approachingEnemiesLocations.length % 2 != 0) {
            Log.w(TAG, "locations of enemies irregular!");
            return ;
        }
        if(enemyMarkers.size() == 0) {
            Log.d(TAG, "no enemies in the vicinity!");
        }
        // first, clear all existing markers!
        for(int i=0; i<enemyMarkers.size(); i++) {
            enemyMarkers.get(i).remove();
        }
        enemyMarkers.clear();
        // for each enemy, add a new marker
        for(int i=0; i<approachingEnemiesLocations.length-1; i++) {
            double enemyLatitude = approachingEnemiesLocations[i];
            double enemyLongitude = approachingEnemiesLocations[i+1];
            // add the marker to the map AND also to the list of markers!
            enemyMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(enemyLatitude, enemyLongitude))
                    .title("Enemy")));
        }
    }

    /**
     * Figures out what the color of the inside of the circle will be
     * the first time the general alarm level threshold is violated!
     * @return: the color the inside of the circle will be painted with.
     */
    private void circleInsideAlarmColor() {
        // get the general alarm level of the user through the preferences!
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
        String generalAlarmLevelStringColor = sharedPreferences.getString("general_alarm_level", "green");
        // turn the general alarm level color into an integer value
        // refer to the documentation of generalAlarmLevelToValue for details
        int generalAlarmLevel = constant.generalAlarmLevelToValue
                (generalAlarmLevelStringColor);
        if (generalAlarmLevel == -1) {
            // is case something went wrong in feeding the string to
            // generalAlarmLevelToValue, then that function will return -1
            // and so the color we set the inside of the circle will be grayish.
            circleColor = Color.argb(100, 150, 150, 150);
        }
        // the general alarm level acts as a threshold in a reverse way:
        // if its, let's say RED, then it has the value 0, so anything
        // triggers it. If it's green, then nothing does.
        // the alarm level of our enemies exceeds our threshold, then we
        // have to color the circle the color of that collective alarm level.
        if (collectiveAlarmLevel > generalAlarmLevel) {

            circleColor = constant.valueToAlarmLevelColor(collectiveAlarmLevel);// if it is the first time we exceed this threshold...
        } else if(collectiveAlarmLevel == 0) {
            // if there are no enemies in the outer circle, then color it green!
            circleColor = constant.valueToAlarmLevelColor(1); // green
        }
    }

    /**
     * Vibrates the phone when the general alarm level gets breached!
     */
    private void vibratePhone(){
        Log.d(TAG, "vibratePhone()");
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 1000 milliseconds = 1 second
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(v != null) {
                v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } else {
            //deprecated in API 26
            if(v != null) {
                v.vibrate(1000);
            }
        }
    }

    /**
     * Issues a fake call by unbinding from the service! Then the service will take care
     * of issuing the call! A small delay in that happening might occur!
     */
    private void fakeCall(){
        try {
            // unbind from service!
            doUnbindService();
        } catch (Throwable t) {
            // if we fail to unbind, issue a warning!
            Log.w(TAG, "Failed to unbind from service!");
        }
    }
}
