package com.example.nochances.utils;

import android.graphics.Color;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class constant {
    /**
     * FIELDS: Lots of constants to be used around the app for various utility purposes.
     * Their functionality is self-explanatory by their name
     */
    public static final int GEOFENCE_RADIUS_IN_METERS = 100;
    public static final int INNER_RADIUS_IN_METERS = 30;
    public static final int PHONE_CALL_RADIUS_IN_METERS = 5;
    // there need to be at least 30 location updates before we can call again!
    public static final int FAKE_PHONE_CALL_INTERVAL = 30;

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Decoding colors into integers for general alarm level.
     * This preference acts as a reverse threshold, so if it is "orange"(3), the user
     * will only be notified when the average threat is at least (3), so pretty often.
     * Note that regularly, these colors are translated to integers in the opposite way,
     * like green to 1, blue to 2 ect.
     *
     * IMPORTANT: Returning -1 means the string was somehow incorrectly fed to this function.
     */
    public static int generalAlarmLevelToValue(String color){
        switch (color) {
            case "red":
                return 1;
            case "blue":
                return 4;
            case "green":
                return 5;
            case "yellow":
                return 3;
            case "orange":
                return 2;
            default:
                return -1;
        }
    }

    /**
     * Turns a value (1,2,3,4,5) to a color (int) which we will paint the inside of the circle with.
     * Any other possible input to the function is turned to a gray color
     */
    public static int valueToAlarmLevelColor(int value) {
        if(value == 1) { // green
            return Color.argb(100, 0, 255, 0);
        } else if(value == 2) { // blue
            return Color.argb(100, 0, 0, 255);
        } else if(value == 3) { // yellow
            return Color.argb(100, 255, 255, 0);
        } else if (value == 4) { // orange
            return Color.argb(100, 255, 128, 0);
        } else if (value == 5) { // red
            return Color.argb(100, 255, 0, 0);
        } else { // gray
            return Color.argb(100, 150,150,150);
        }
    }

    /**
     * Turns a color of an enemy to a value (green : 1, red: 5).
     *
     * IMPORTANT: 6 is returned in case of any irregularity!
     * We can see the notion of reversing which we had mentioned earlier.
     */
    public static int alarmLevelColorToValue(String color) {
        switch (color) {
            case "red":
                return 5;
            case "blue":
                return 2;
            case "green":
                return 1;
            case "yellow":
                return 3;
            case "orange":
                return 4;
            default:
                return 6;
        }
    }
}
