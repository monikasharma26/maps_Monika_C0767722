package com.c0767722.maps_monika_c0767722;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int REQUEST_CODE = 1;
    private Marker curr_marker;
    private Marker dest_marker;
    private Polyline polyline;
    private Polygon polygon;
    Marker newMarker;
    String text = "";
    private static final int POLYGON_NUM = 4;
    List<Marker> markersList = new ArrayList<>();
    List<Polyline> polylinesList = new ArrayList<>();
    List<String> res = new ArrayList<>();
    List<Float> resultList = new ArrayList<>();

    int markerCount = 0; //marker counter
    boolean dragMarker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // init location manager
        initLocation();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerCount < 4) {
                    markerCount = markerCount + 1;
                    if (markerCount == 1) {
                        text = "A";
                    } else if (markerCount == 2) {
                        text = "B";
                    } else if (markerCount == 3) {
                        text = "C";
                    } else if (markerCount == 4) {
                        text = "D";
                    } else {
                        text = "";
                    }
                    setDestinationLocation(latLng, text);
                }
                System.out.println("Polygon Count" + markersList.size());
                if (markersList.size() == POLYGON_NUM) {
                    drawShape();

                }
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                clearDestinationMap();
                //  clearMap();
            }
        });

        if (!checkPermission())
            requestPermission();
        else
            initLocationCallback();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
                    setHomeLocation(location);
                    //  mMap.addMarker(new MarkerOptions().position(marker).title("Current use  location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 10));
                }
            }
        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {

            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon pu) {
                Log.d("ssss", "MARKERLIST +" + markersList);
                Float d1234 = ((resultList.get(0)) / 1000 + ((resultList.get(0)) / 1000) + ((resultList.get(0)) / 1000) + ((resultList.get(0)) / 1000));
                Toast.makeText(getApplicationContext(), "Total Distance: " + d1234 + " km", Toast.LENGTH_SHORT).show();

            }
        });

        mMap.setOnMarkerDragListener(this);
       /* // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    // get address
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("location updates>>>", location.getLatitude() + String.valueOf(location.getLongitude()));
                }
            }
        };
    }

    private void clearDestinationMap() {
        if (polylinesList != null) {
            clearMap();
        }
        if (markersList.size() == POLYGON_NUM) {
            clearMap();
        }
    }

    private void setDestinationLocation(LatLng location, String text) {

        MarkerOptions option = getMarkerOption(location);
        Marker marker = mMap.addMarker(option);
        if (dest_marker != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(10)
                    .add(marker.getPosition(), dest_marker.getPosition());
            polyline = mMap.addPolyline(polylineOptions);
            polyline.setClickable(true);
            polylinesList.add(polyline);

        }

        dest_marker = marker;

        markersList.add(marker);

    }

    public static List<LatLng> reDrawPolyLine(List<LatLng> latLngs) {

        List<LatLng> latLngList = getCornerOrder(latLngs);

        if (latLngList.get(0).latitude > latLngList.get(1).latitude) {
            LatLng lng = latLngList.get(0);
            latLngList.set(0, latLngList.get(1));
            latLngList.set(1, lng);
        }

        if (latLngList.get(2).latitude < latLngList.get(3).latitude) {
            LatLng lng = latLngList.get(2);
            latLngList.set(2, latLngList.get(3));
            latLngList.set(3, lng);
        }
        return latLngList;
    }

    private static List<LatLng> getCornerOrder(List<LatLng> points) {
        Collections.sort(points, new Comparator<LatLng>() {
            @Override
            public int compare(LatLng p1, LatLng p2) {
                return Double.compare(p1.longitude, p2.longitude);
            }
        });
        return points;
    }


    private boolean checkPermission() {
        int isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return isGranted == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initLocationCallback();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void initLocation() {
        // init location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // init location request
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void setHomeLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        // MarkerOptions markerOptions = new MarkerOptions().position(userLocation);
        // .title("Your Location")
        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        // .snippet("");
        //  curr_marker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    private void clearMap() {

        if (polylinesList.size() != 0) {
            for (int i = 0; i < polylinesList.size(); i++) {
                polylinesList.get(i).remove();
                polyline.remove();
            }
            polylinesList.removeAll(polylinesList);
        }

        markerCount = 0;
        if (dest_marker != null) {
            dest_marker.remove();
            dest_marker = null;
        }
        if (markersList.size() != 0) {
            for (int i = 0; i < markersList.size(); i++) {
                markersList.get(i).remove();
            }

            markersList.removeAll(markersList);

            polygon.remove();
        }
    }

    private void drawLine(Marker marker) {
        if (dest_marker != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(20)
                    .add(marker.getPosition(), dest_marker.getPosition());
            polyline = mMap.addPolyline(polylineOptions);
            polylinesList.add(polyline);
        }
    }

    private void drawShape() {
        PolygonOptions polygonOptions = new PolygonOptions()
                .fillColor(Color.GREEN)
                .strokeColor(Color.RED)
                .strokeWidth(15)
                .visible(true);

        for (int i = 0; i < POLYGON_NUM; i++) {
            polygonOptions.add(markersList.get(i).getPosition());
        }
        polygon = mMap.addPolygon(polygonOptions);

    }

    /**
     *
     */

    private void getNewPolyLines(List<LatLng> latLngList) {
        for (int i = 0; i < POLYGON_NUM; i++) {
            if (i == 0) {
                drawLine(latLngList.get(3), latLngList.get(i));
            } else {
                drawLine(latLngList.get(i), latLngList.get(i - 1));
            }
        }
    }


    private void clearPolyLines() {
        Log.d("ssss", "datata: " + polylinesList.size());
        for (Polyline polyline : polylinesList) {
            polyline.remove();
        }
        polylinesList.clear();

        //markerCount = 0;
    }

    private void reDrawShape() {
        PolygonOptions polygonOptions = new PolygonOptions();
        List<LatLng> newLaLong = new ArrayList<>();

        for (int i = 0; i < POLYGON_NUM; i++) {
            newMarker = markersList.get(i);
            Log.d("ssss", "reDrawShape: " + newMarker.getTitle());
            LatLng latLng = newMarker.getPosition();
            getNewMarkerOption(latLng);
            newLaLong.add(newMarker.getPosition());
        }
        clearPolyLines();
        newLaLong = reDrawPolyLine(newLaLong);
        getNewPolyLines(newLaLong);
        for (LatLng latLng : newLaLong) {
            polygonOptions.add(latLng);
        }

        polygonOptions.fillColor(Color.GREEN);
        polygon = mMap.addPolygon(polygonOptions);
        polygon.setClickable(true);

    }

    private MarkerOptions getMarkerOption(LatLng latLng) {
        String[] markerAddress = getMarkerAddress(latLng);
        System.out.println(Arrays.toString(markerAddress));
        MarkerOptions option = new MarkerOptions().position(latLng);
        if (markerAddress != null) {
            option.title(markerAddress[0]).snippet(markerAddress[1]);
            option.draggable(true);
        }
        return option;
    }

    private void getNewMarkerOption(LatLng latLng) {
        String[] markerAddress = getMarkerAddress(latLng);
        System.out.println(Arrays.toString(markerAddress));
        MarkerOptions option = new MarkerOptions().position(latLng);
        if (markerAddress != null) {
            option.title(markerAddress[0]).snippet(markerAddress[1]);
            option.draggable(true);
        }

    }

    private String[] getMarkerAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        String[] addressData = null;
        StringBuilder title = new StringBuilder("");
        StringBuilder snippet = new StringBuilder("");
        try {
            Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
            if (address.getSubThoroughfare() != null) {
                title.append(text + ", " + address.getSubThoroughfare());
            }
            if (address.getThoroughfare() != null) {
                title.append(text + "," + address.getThoroughfare());
            }
            if (address.getPostalCode() != null) {
                title.append(text + ", " + address.getPostalCode());
            }
            if (address.getLocality() != null) {
                snippet.append(address.getLocality());
            }
            if (address.getAdminArea() != null) {
                snippet.append(", ");
                snippet.append(", " + address.getAdminArea());
            }
            addressData = new String[]{title.toString(), snippet.toString()};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressData;
    }

    private void drawLine(LatLng position, LatLng position1) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(20)
                .add(position, position1);
        Polyline polyline = mMap.addPolyline(polylineOptions);
        polyline.setClickable(true);
        polylinesList.add(polyline);
    }

    private void clearPolygon() {
        if (polygon != null)
            polygon.remove();

        polygon = null;
    }

    private void drawAllPolylines() {
        Marker marker = null;
        for (Marker markerl : markersList) {
            if (marker != null) {
                drawLine(marker.getPosition(), markerl.getPosition());
            }
            marker = markerl;
        }
    }


    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        dragMarker = true;
        clearPolyLines();
        drawAllPolylines();
        clearPolygon();
        reDrawShape();
    }


}