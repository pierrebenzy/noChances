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
<<<<<<< HEAD
4.$ DAY 4: Themis: Made TrackingService request location change from GPS. Those locations are uploaded to Firebase and also sent back to MapsActivity to update the map. If the activity isn't running but is on the background, the service just uploads location updates to the cloud. Current problems: binding and connecting to service after going to other activities and returning back to MapsActivity. Also, the service is killed when we close the app. Possible fix: a persistent notification! Pierre: commited work done on Saturday and sunday. Sign in and register work (except for the permissions does not work perfectly) Settings mostly done but some bugs are left to fix. I have started the list activity and found a way to add the color picker thing I will start this afternoon and I hope I'll be done with that tonight or tomorrow.Created fragments to hold the complete list of users and the list enemies. User is able to click one item on the list and choose the level threat (color) of the enemy.
