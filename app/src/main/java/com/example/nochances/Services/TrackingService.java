package com.example.nochances.Services;



import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.nochances.utils.constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TrackingService extends Service {

    /*
     * TODOs
     * TODO: how to disable tracking, i.e. stop listener from receiving location updates?
     */

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

    /**
     * Basic Service Methods:
     *      1. Constructor: TODO: should it contain something?
     *      2. onCreate(): TODO: Does it do anything?
     *      3. onDestroy(): TODO: Should it do anything?
     *      4. onStartCommand(): Don't forget to START_STICKY! TODO: Does it do anything else?
     *      5. onBind(): return the binder to the serverReceiver
     */
    public TrackingService() {
    }

    /**
     * Sets up location manager and provider
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        // instantiate the locationListener object
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
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
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        // the service is now running!
        isRunning = true;
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
                    sendMessageOfSuccessfullUnregistering();
                    serverSender = null;
                    break;
            }
        }
    }

    /**
     * Sends a message to the client that they unregistered safely. Only then can the client
     * try to bind back into the service!
     */
    private void sendMessageOfSuccessfullUnregistering() {
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

            double [] approachingEnemisLocationsPrimitive = new double[approachingEnemiesLocations.size()];
            for (int i=0; i<approachingEnemisLocationsPrimitive.length; i++) {
                approachingEnemisLocationsPrimitive[i] = approachingEnemiesLocations.get(i);
            }
            newLocationBundle.putDoubleArray(APPROACHING_ENEMIES, approachingEnemisLocationsPrimitive);

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
                locationManager.requestLocationUpdates(provider, 3000, 10,
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
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
      //  DatabaseReference myRef = firebaseDatabase.getReference("user_email/current_location");
       // myRef.child("latitude").setValue(location.getLatitude());
       // myRef.child("longitude").setValue(location.getLongitude());
    }

    /**
     * CONVENTION: the approachingEnemiesLocations array below has 2n entries for n enemies
     * detected. In each pair, the first part is the latitude and the second is the longitude.
     * @param l: the location of the user!
     */
    private void getApproachingEnemiesLocation(final Location l){
        // get a pointer to the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        // get a reference to the enemies list
        DatabaseReference myRef = database.getReference("user_email/enemies_list");
        // read the enemies list into an array of Strings
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // go over all your enemies (push must have been used here!)
                heardBackFromEnemies = 0; // how many enemies have given us their location so far? ZERO
                totalEnemyNumbers = dataSnapshot.getChildrenCount();
                for (DataSnapshot enemy : dataSnapshot.getChildren()) {
                    // get their id (email -> MD5 hash -> hex representation)
                    String enemyEmail = enemy.getValue(String.class);
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
                                    if (results[0] < constant.GEOFENCE_RADIUS_IN_METERS) {
                                        // enemy is approaching! Register that!
                                        approachingEnemiesLocations.add(enemyLatitude);
                                        approachingEnemiesLocations.add(enemyLongitude);
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

    /**
     * Displays a notification which informs the user that their location is being monitored by
     * the app and that if they don't want that anymore they can either:
     *      1) TODO: Disable that from the settings (app becomes useless then)
     *      2) log out
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void showNotification() {
        // code string for the notification channel ID
        final String CHANNEL_ID = "notification channel";
        // make a notification channel with the above channel ID.
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                "channel name", NotificationManager.IMPORTANCE_DEFAULT);

        // make a notification builder for the notification you will display
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("noChances")
                .setContentText("This app is tracking your location. You can disable that feature " +
                        "in the settings of log out.");
        // build and customize your notification!
        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(mNotificationManager != null) {
            // create the notification channel through the notification manager
            mNotificationManager.createNotificationChannel(notificationChannel);

            if(isRunning) {
                // if the service is running, post the notification to the status bar!
                mNotificationManager.notify(0, notification);
            }
        }
    }
}

