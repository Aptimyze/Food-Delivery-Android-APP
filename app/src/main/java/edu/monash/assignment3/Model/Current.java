package edu.monash.assignment3.Model;

public class Current {
    public static User currentUser;

    public static Request currentRequest;

    public static Rider currentRider;

    public static String converCodeToStatus(String status) {
        if(status.equals("0")){
            return "Placed";
        }else if(status.equals("1")){
            return "Shipping";
        }else {
            return "Shipped";
        }


    }

    public static final String DELETE = "Delete";

    public static final String PAYPAL_CLIENT_ID="AW1fyf27EBRC6KBBKrr_TZ1NiG8cwVMpWe0Rhh3ZnzkYP9VZQ7Nrw7mBEO4ym0srMUSFl1yPfB4DGHTH";
}
