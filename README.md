# NoChances README

### TEAM
Pierre Desvallons, Haris Themistoklis

### Details
This is a README for our final project in the course CS65 - Smartphone Programming.

This document will contain information about high-level code management, known issues and instructions about how to download, install and use the app.

### Progress
1. DAY 1: Set-up gitlab. Official start of coding. Setting up git.Testing
2. DAY 2: Themis: Set up MapActivity. Experimenting with permissions. Hard to make everything run smoothly.
3. DAY 3: Themis: Made permissions work. **Now, when the user selects "Don't show again" when prompted by the location permissions, they are presented with a dialog which points them to the Settings so that they can set the permissions manually**.
4. DAY 4: Themis: Made TrackingService request location change from GPS. Those locations are uploaded to Firebase and also sent back to MapsActivity to update the map. If the activity isn't running but is on the background, the service just uploads location updates to the cloud. Current problems: binding and connecting to service after going to other activities and returning back to MapsActivity. Also, the service is killed when we close the app. Possible fix: a persistent notification! Pierre: commited work done on Saturday and sunday. Sign in and register work (except for the permissions does not work perfectly) Settings mostly done but some bugs are left to fix. I have started the list activity and found a way to add the color picker thing I will start this afternoon and I hope I'll be done with that tonight or tomorrow.Created fragments to hold the complete list of users and the list enemies. User is able to click one item on the list and choose the level threat (color) of the enemy.
5. DAY 5: Themis: Persistent notification implemented. The service now runs on the background, even when the app is killed! One problem I am facing is that when the user clicks on the notification, they aren't redirected back to the app as the PendingIntent specifies but are directed to a settings page. I spent a lot of time trying to fix this issue and I suspect it happens because I haven't yet implemented a way to kill the foreground service. I will come back to that.
6. DAy 6: Themis: Color of map, vibration, 3 radii idea. Now there are 3 circles around the user. The outer one changes color depending on the user's general alarm level and the alarm levels of the users inside that circle. The middle circle is made so that when it is breached by an enemy for the first time, the phone vibrates. The inner circle is such that when it is breached, a fake phone call is triggered. I haven't done the fake phone call part or the vibration yet.
7. DAY 7: Themis: Working on color of map, interacting with Firebase and vibration. Figuring out foreground service mechanics.
8. DAY 8: Themis: Color changes on map successfully implemented. Notifications working. Merging the two branches.
9. DAY 9: Themis: Working on making the app work after merging. Unsuspected, low-level problems arising (map is not working, authentication and Firebase malfunctioning). Running adb from command line in hope of finding the bug. Uninstalling and re-installing the API-key of the map helped. Re-structuring database after discussion with Pierre.
10. DAY 10: Themis: Working on Fake phone call UI. Surprisingly hard to make a sliding answering method. The UI looks poorly made but it took a long time to do it. I'm wondering if there are faster and easier methods. I tried looking at Google's source code of android of the similar feature but the code was unpractically vast. I also thought about making a PHP server which would call phones with SIM cards directly. That had 2 limitations: first, we would have to have real devices working for the sole purpose of calling phones (also how many of those devices, which device calls who ect) and second, the UI approach works for phones even without SIM cards. I really hope we can make the UI more appealing though...
11. DAY 11: Themis: Finished Fake call UI (looks decent and slider - after a lot of testing) somewhat works. Integrated app with updated Firebase design by Pierre. Lots of features work and thanks to coding defensively, the app doesn't crash! VVibration and ringing hasn't been tested yet


### Known Problems
1. When some enemy gets out of the outer circle, it might take a few iterations of location requests to make it so that the color of the circle changes successfully. During those iterations, maybe the color will be wrong even if the enemy is out of the circle! The reason why this happens is because it takes some time to receive a new location update so the color of the circle remains the same during that time!
2. Right now, logging out kills the persistent service but leaves some other instance of it open. That instance doesn't do anything other than update locations, so it really is useless. We just cannot find a decent way to kill it yet.
3. Unknown server issue with Firebase. Seems to be caused by phone or it might be an admitted bug. Prevents us from getting the pictures of the profiles.
