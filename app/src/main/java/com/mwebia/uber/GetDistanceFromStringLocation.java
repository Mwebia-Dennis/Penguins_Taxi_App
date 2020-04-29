package com.mwebia.uber;

import android.location.Location;
import android.util.Log;

class GetDistanceFromStringLocation {

    double distance;


    private String str_lat_requester;
    private String str_lon_requester;
    private Double str_lat_user;
    private Double str_lon_user;

    double lat_Double_requester = 0;
    double lon_Doublelon_requester = 0;
    double lat_Double_user= 0;
    double lon_Double_user = 0;

    GetDistanceFromStringLocation(String str_lat_requester, String str_lon_requester, Double str_lat_user, Double str_lon_user){
        this.str_lat_requester = str_lat_requester;
        this.str_lon_requester = str_lon_requester;
        this.str_lat_user = str_lat_user;
        this.str_lon_user = str_lon_user;
    }

    private Double getDistance(){
        try {

            lat_Double_requester = Double.parseDouble(str_lat_requester);
            lon_Doublelon_requester = Double.parseDouble(str_lon_requester);
        } catch (NumberFormatException e) {
            Log.v("Main", "Convert to Double Failed : ");
        }

        Location locationA = new Location("point A");
        locationA.setLatitude(lat_Double_requester);
        locationA.setLongitude(lon_Doublelon_requester);

        Location locationB = new Location("point B");
        locationB.setLatitude(lat_Double_user);
        locationB.setLongitude(lon_Double_user);

        distance = locationA.distanceTo(locationB);

        return  distance;
    }

}
