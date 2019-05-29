package com.example.nochances.Services;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.nochances.FakePhoneCall;
import com.example.nochances.Model.Intents;
import com.example.nochances.fragments.settingsPrefFragment;
import com.example.nochances.utils.constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TrackingService extends Service {

    /** FIELDS
     *      1. TAG: tag for debugging
     *      2. serverReceiver: a receiver that will receive messages from clients
     *      (actually only from MapsActivity)
     *      3. serverSender: a Messenger which points to our client.
     *      WE ONLY HAVE ONE POSSIBLE CLIENT!
     *      4. MSG_REGISTER_CLIENT: a code for messages wanting to register clients
     *      5. MSG_LOCATION_UPDATE: a code for messages notifying the clients of location updates!
     *      6. MSG_UNREGISTER_CLIENT: a code for messages wanting to unregister a client.
     *      7. MSG_SUCCESSFUL_UNREGISTERING: a code for messages indicating successful client unregistering
     *      8. locationListener: a listener object which will react to changes in the user's location.
     *      9. LOCATION_UPDATE: when opening the message titled MSG_LOCATION_UPDATE, the client
     *      finds a Bundle with LOCATION_UPDATE on it. That bundle contains the new location!
     *      10. isRunning: boolean that tells us if the service is running or not.
     *      11. APPROACHING_ENEMIES: when opening the message title MSG_LOCATION_UPDATE, the client
     *      finds a Bundle with APPROACHING_ENEMIES on it. That bundle contains a list
     *      of the locations of the enemies which are currently within the client's circle!
     *      12. approachingEnemiesLocations: an ArrayList which contains a list of the
     *      approaching enemies (inside the user's radius)
     *      13. heardBackFromEnemies: when we are trying to get the enemies locations and
     *      figure out which ones are close to us, we employ the inherently asynchronous
     *      ValueEventListeners of Firebase (god knows why it works like this). So we can't
     *      send a message until we've heard back from all of them!
     *      14. totalEnemyNumbers: how many enemies do we have?
     *      15. CHANNEL_ID: code string for the notification channel ID
     *      16. FOREGROUND_ID: an identification integer for our foreground service: 1234
     *      17. COLLECTIVE_ALARM_LEVEL: code for packaging up the collective alarm level for all
     *      the approaching enemies and sending it to the client
     *      18. collectiveAlarmLevel: the average of the alarm levels of all approaching enemies
     *      19. enemyInInnerForFirstTime: is it the first time that an enemy has
     *      breached the inner circle?
     *      20. INNER_CIRCLE_ENTRY: code for packaging and sending to the client the information
     *      that some enemy breached the inner circle and so the phone must vibrate!
     *      21. phoneCall: did an enemy breach the most inner circle?
     *      22. locationUpdatesUntilPhoneCall: how many more location updates until we can
     *      issue a fake phone call again?
     *      23. MAKE_PHONE_CALL: code for telling the client that a fake phone call needs
     *      to be initiated.
     *      24. ringtone: a ringtone playing!
     */
    private static final String TAG = "TrackingService";
    private final Messenger serverReceiver = new Messenger(new IncomingMessageHandler());
    private Messenger serverSender = null;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_LOCATION_UPDATE = 2;
    public static final int MSG_UNREGISTER_CLIENT = 3;
    public static final int MSG_SUCCESSFUL_UNREGISTERING = 4;
    private LocationListener locationListener;
    public static final String LOCATION_UPDATE = "Location update!";
    public static boolean isRunning = false;
    public static final String APPROACHING_ENEMIES = "Approaching enemies!";
    private ArrayList<Double> approachingEnemiesLocations = new ArrayList<>();
    private long heardBackFromEnemies;
    private long totalEnemyNumbers;
    public static final String COLLECTIVE_ALARM_LEVEL = "collective alarm level";
    private int collectiveAlarmLevel;
    private boolean enemyInInnerForFirstTime;
    public static final String INNER_CIRCLE_ENTRY = "inner circle entry";
    private boolean phoneCall;
    private int locationUpdatesUntilPhoneCall;
    public static final String MAKE_PHONE_CALL = "make fake phone call|!";
    public static Ringtone ringtone;

    /**
     * Basic Service Methods:
     *      1. Constructor
     *      2. onCreate()
     *      3. onDestroy()
     *      4. onStartCommand(): Don't forget to START_STICKY!
     *      5. onBind(): return the binder to the serverReceiver
     */
    public TrackingService() {
    }

    /**
     * Sets up location manager and provider
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        // instantiate the locationListener object
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                if(current_user == null) { // if the user is signed out, kill this service!
                    stopForeground(true);
                    stopSelf();
                } else {
                    Log.d(TAG, current_user.getEmail());
                }
                Log.d(TAG, "location update received!");
                // when we get a new location, we do three things:
                //      1) we upload it to the cloud
                //      2) we send it back to the maps activity!
                //      3) we browse through the locations of all our enemies are report back
                //         to MapsActivity those that lie in our radius!
                uploadLocation(location); // (1)
                if(serverSender != null) {
                    // we only send a message to the client if the client is there.
                    // the client could have disconnected but we still have to keep uploading
                    // updated location information to the cloud. We won't be sending messages though!

                    // first, clear the list of approaching enemies
                    approachingEnemiesLocations.clear();

                    // now, we get all the enemies which are in the radius we have set!
                    // After we're done (because its an asynchronous process) AND ONLY THEN,
                    // send a message to the client about their location and the enemies near them.
                    getApproachingEnemiesLocation(location); // (3) + (eventually) (2)
                } else {
                    Log.d(TAG, "serverSender is null when trying to notify of location changes!");
                    if(phoneCall) {
                        fakeCall();
                    }
                }

            }

            // The three methods below don't need to do anything
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }
        };
        // when the Service is first created, we can instantly call if we need to
        locationUpdatesUntilPhoneCall = 0;
        super.onCreate();
    }


    /**
     * When service is destroyed.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // the service isn't running any more!
        isRunning = false;
        super.onDestroy();
    }

    /**
     * @param intent: the intent that starts the service
     * @param flags: we won't probably use this
     * @param startId: we won't probably use this
     * @return: START_STICKY!
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null &&
                intent.getAction().equals(Intents.STOP_FOREGROUND_SERVICE)) {
            Log.d(TAG, "Signing out. Ending foreground service!");
            stopForeground(true);
            stopSelf();
        }

        Log.d(TAG, "onStartCommand()");
        // the service is now running!
        isRunning = true;
        Notification notification = buildPersistentNotification();
        if(notification != null) {
            int FOREGROUND_ID = 1001;
            startForeground(FOREGROUND_ID, notification);
        }
        return START_STICKY;
    }

    /**
     * Return the binder to the serverReceiver.
     * @param intent: the intent which binds MapsActivity to this Service
     * @return: the IBinder of the serverReceiver messenger, so that MapsActivity knows where
     *          to send messages to.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return serverReceiver.getBinder();
    }

    /**
     * MESSENGING METHODS / CLASSES
     *      1. IncomingMessageHandler:
     *      Private class which handles messages which are incoming to the server
     *      The server only really gets two kinds of messages: registering a client and
     *      unregistering a client.
     *      2. sendMessageOfLocationUpdate: when we receive a new location update, we have
     *      to notify the MapsActivity so that it changes the map layout!
     */
    @SuppressLint("HandlerLeak")
    private class IncomingMessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.d(TAG, "Client register message has arrived!");
                    // this client wants to register! So we're recording who to send messages to...
                    // we are adopting a client ONLY IF we don't already have one!
                    if(serverSender == null) {
                        serverSender = msg.replyTo;
                        // we registered our client! We shall now start requesting location updates
                        // and 1) send them to the cloud and 2) send them to the client! Simple!
                        getLocationUpdates();
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    Log.d(TAG, "message of unregistering a client!");
                    // this client wants to unregister! Just make serverSender null!
                    // But be careful! The client might come and register again before this message
                    // gets here. Then you'll just be setting serverSender to null and ruin
                    // all future communication for no reason!
                    // To deal with this a-synchronization issue, back in MapsActivity, we have
                    // a boolean variable that doesn't let us bind back to the service until
                    // a message of successful unregistering has been received!
                    sendMessageOfSuccessfulUnregistering();
                    serverSender = null;
                    break;
            }
        }
    }

    /**
     * Sends a message to the client that they unregistered safely. Only then can the client
     * try to bind back into the service!
     */
    private void sendMessageOfSuccessfulUnregistering() {
        Log.d(TAG, "sending message of successful unregistering");
        try {
            // try to send a message of successful unregistering!
            Message successfulUnregisteringMessage = Message.obtain(null, MSG_SUCCESSFUL_UNREGISTERING);
            if(serverSender != null) {
                serverSender.send(successfulUnregisteringMessage);
            }
        } catch (RemoteException e){
            Log.w (TAG, "Failed sending successful unregistering message!");
            e.printStackTrace();
        }
    }

    /**
     * When we receive a new location update, we have
     * to notify the MapsActivity so that it changes the map layout!
     * @param l: the new location the user is currently in!
     */
    private void sendMessageOfLocationUpdate(Location l) {
        Log.d(TAG, "sendMessageOfLocationUpdate()");
        try {
            // make a message whose purpose is to notify the client of a location update!
            Message locationUpdateMessage = Message.obtain(null, MSG_LOCATION_UPDATE);
            // get the latitude and longitude of the new location and store them in
            // an array of doubles!
            double [] latlng = {l.getLatitude(), l.getLongitude()};
            // make a bundle to put into the message.
            Bundle newLocationBundle = new Bundle();
            // put the latlng array in the bundle
            newLocationBundle.putDoubleArray(LOCATION_UPDATE, latlng);

            // Next, we should go through all the enemies and find the ones which are within the
            // user's radius. We send a list of their locations back to MainActivity, where the
            // Map will be updated!
            // CONVENTION: the approachingEnemiesLocations array below has 2n entries for n enemies
            // detected. In each pair, the first part is the latitude and the second is the longitude.

            double [] approachingEnemiesLocationsPrimitive = new double[approachingEnemiesLocations.size()];
            for (int i=0; i<approachingEnemiesLocationsPrimitive.length; i++) {
                approachingEnemiesLocationsPrimitive[i] = approachingEnemiesLocations.get(i);
            }
            newLocationBundle.putDoubleArray(APPROACHING_ENEMIES, approachingEnemiesLocationsPrimitive);

            // you need to put in the collectiveAlarmLevel
            // simple division is good right now!
            if(collectiveAlarmLevel != 0 && approachingEnemiesLocationsPrimitive.length != 0) {
                collectiveAlarmLevel = collectiveAlarmLevel / (approachingEnemiesLocationsPrimitive.length / 2);
            }
            newLocationBundle.putInt(COLLECTIVE_ALARM_LEVEL, collectiveAlarmLevel);

            // put in the message if someone entered the inner circle for the first time
            newLocationBundle.putBoolean(INNER_CIRCLE_ENTRY, enemyInInnerForFirstTime);

            // lastly, put in a message of if we need to make a phone call or not!
            newLocationBundle.putBoolean(MAKE_PHONE_CALL, phoneCall);

            // put the bundle in the message
            locationUpdateMessage.setData(newLocationBundle);
            // send the message (if you can)!
            if(serverSender != null) {
                serverSender.send(locationUpdateMessage);
            }
        } catch (RemoteException e) {
            // issue appropriate warning!
            Log.w(TAG, "Error in sending the location update message!");
            e.printStackTrace();
        }
    }

    /**
     * Once the user has registered, we start listening for updates to their location!
     */
    private void getLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationUpdates()");
            // if we have the permission to get location information from the user....
            // instantiate a location manager object
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(locationManager != null) {
                // pick the most suitable provider based on some criteria
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                String provider = locationManager.getBestProvider(criteria, true);
                // now start listening for location updates
                locationManager.requestLocationUpdates(provider, 1000, 1,
                        locationListener);
            } else {
                // issue appropriate warning!
                Log.w(TAG, "locationManager wasn't initialized!");
            }
        }
    }

    /**
     * Uploads a location to the FireBase relational database
     * @param location: the location to be uploaded
     */
    private void uploadLocation(Location location) {
        // get the current user
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        // if the user exists and their e-mail is valid...
        if(current_user != null && current_user.getEmail()!=null) {
            Log.d(TAG, "user_email: "+current_user.getEmail());
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference myRef = firebaseDatabase.getReference("users_" +
                    constant.md5(current_user.getEmail()) + "/current_location");
            myRef.child("latitude").setValue(location.getLatitude());
            myRef.child("longitude").setValue(location.getLongitude());
        } else {
            Log.d(TAG, "non-existing user or invalid email in uploadLocation()");
        }
    }

    /**
     * CONVENTION: the approachingEnemiesLocations array below has 2n entries for n enemies
     * detected. In each pair, the first part is the latitude and the second is the longitude.
     * @param l: the location of the user!
     */
    private void getApproachingEnemiesLocation(final Location l){
        // get a pointer to the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        if(current_user != null && current_user.getEmail() != null) {
            // get a reference to the enemies list
            final DatabaseReference myRef = database.getReference("users_" +
                    constant.md5(current_user.getEmail())).child("enemies_list");
            // read the enemies list into an array of Strings
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "looking through enemies list!");
                    // go over all your enemies (push must have been used here!)
                    heardBackFromEnemies = 0; // how many enemies have given us their location so far? ZERO
                    totalEnemyNumbers = dataSnapshot.getChildrenCount();
                    enemyInInnerForFirstTime = false;
                    phoneCall = false; // initialize as false!
                    collectiveAlarmLevel = 0; // initialized as 0
                    Log.d(TAG, "number of enemies = "+totalEnemyNumbers);
                    for (DataSnapshot enemy : dataSnapshot.getChildren()) {
                        // get their id (email -> MD5 hash -> hex representation)
                        final String enemyEmail = enemy.child("email").getValue(String.class);
                        String enemyAlarmLvlStr = enemy
                                .child("color").getValue(String.class);
                        final int enemyAlarmLevel;
                        if(enemyAlarmLvlStr != null) {
                            enemyAlarmLevel = constant.alarmLevelColorToValue(enemyAlarmLvlStr);
                        } else {
                            enemyAlarmLevel = 0;
                        }
                        // get the "inInner" leaf to be used later!
                        final DataSnapshot enemyInInner = enemy.child("inInner");
                        Log.d(TAG, "enemy: "+enemyEmail+", color:"+enemyAlarmLvlStr+", numerically: "+enemyAlarmLevel);
                        if (enemyEmail != null) {
                            // use an MD5 hash on the e-mail because that's how we store the profiles!
                            String enemyID = "users_"+ constant.md5(enemyEmail);
                            // go to that enemy's database and try to see their current location
                            DatabaseReference enemyRef = database.getReference(enemyID).child("current_location");
                            enemyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    heardBackFromEnemies ++; // an enemy gave us their location!
                                    Double enemyLatitude = snap.child("latitude").getValue(Double.class);
                                    Double enemyLongitude = snap.child("longitude").getValue(Double.class);
                                    if (enemyLatitude != null && enemyLongitude != null) {
                                        // now we have the enemy's latitude and longitude.
                                        // See if it is within the radius from user's current location!
                                        float[] results = new float[5];
                                        Location.distanceBetween(l.getLatitude(), l.getLongitude(),
                                                enemyLatitude, enemyLongitude, results);

                                        // is the enemy inside the outer circle
                                        if (results[0] < constant.outerRadiusInMeters(TrackingService.this)) {
                                            // enemy is approaching! Register that!
                                            approachingEnemiesLocations.add(enemyLatitude);
                                            approachingEnemiesLocations.add(enemyLongitude);
                                            // add their alarm level to the collective alarm level sum!
                                            collectiveAlarmLevel += enemyAlarmLevel;
                                        }

                                        // was the enemy in the inner radius before?
                                        Boolean enemyWasInInnerCircle = enemyInInner.getValue(Boolean.class);
                                        if(enemyWasInInnerCircle != null) {
                                            // if this enemy is in the inner circle
                                            if (results[0] < constant.middleRadiusInMeters(TrackingService.this)) {
                                                // if they just entered the inner circle
                                                if(!enemyWasInInnerCircle) {
                                                    // also, we know that the phone should vibrate
                                                    // after we send our message!
                                                    enemyInInnerForFirstTime = true;
                                                }
                                                // now they are in the inner circle
                                                enemyInInner.getRef().setValue(true);
                                            } else {
                                                // enemy is not in inner circle!
                                                enemyInInner.getRef().setValue(false);
                                            }
                                        } else {
                                            Log.d(TAG, "enemyWasInInnerCircle null!");
                                        }

                                        // did someone new breach our most-inner perimeter?
                                        if(results[0] < constant.phoneCallRadiusInMeters(TrackingService.this)){
                                            if(locationUpdatesUntilPhoneCall == 0) {
                                                phoneCall = true; // make a fake phone call!
                                                // don't issue more fake phone calls for a while
                                                locationUpdatesUntilPhoneCall = constant.FAKE_PHONE_CALL_INTERVAL;
                                            }
                                        }
                                    }
                                    if(heardBackFromEnemies == totalEnemyNumbers ) {
                                        // if we now have heard back from every enemy...
                                        // send the message to the client!
                                        sendMessageOfLocationUpdate(l);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    /**
     * Displays a notification which informs the user that their location is being monitored by
     * the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification buildPersistentNotification() {
        // make a notification manager
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // build a notification channel
            String CHANNEL_ID = "notification channel";
            NotificationChannel chan1 = new NotificationChannel(
                    CHANNEL_ID,
                    "default",
                    NotificationManager.IMPORTANCE_NONE);

            // customize the channel!
            chan1.setLightColor(Color.TRANSPARENT);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            notificationManager.createNotificationChannel(chan1);
            // make a notification builder for the notification you will display
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder
                    (this, CHANNEL_ID)
                    .setContentTitle("noChances")
                    .setContentText("This app is tracking your location. You can disable that feature " +
                            "in the settings of log out.");
            // make this a persistent notification!
            // notificationBuilder.setOngoing(true);

            Intent intent = Intents.NotificationToMapsActivity(this);

            notificationBuilder.setContentIntent(PendingIntent.getActivity(
                    this,
                    0, intent, 0));
            // notificationBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
            return notificationBuilder.build();
        }
        return null;
    }

    /**
     * If we are not connected to the client (probably because the app isn't running or because
     * the user might be in some other context) then we still need to issue the phone call!
     */
    private void fakeCall() {
        // play ringtone
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                                                (TrackingService.this);
        Uri notification = Uri.parse(sharedPreferences.getString
                                (settingsPrefFragment.RINGTONE_PREFERENCE, "Life's Good"));
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();
        Intent i = new Intent(this, FakePhoneCall.class);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(i);
    }
}

