package com.example.nochances.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class constant {
    /**
     * FIELDS: Lots of constants to be used around the app for various utility purposes.
     * Their functionality is self-explanatory by their name
     */
    public static final int GEOFENCE_RADIUS_IN_METERS = 1000000;
    public static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS = 60000;
    public static final String GEOFENCE_REQUEST_ID = "NoChances Geofence ID";

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
