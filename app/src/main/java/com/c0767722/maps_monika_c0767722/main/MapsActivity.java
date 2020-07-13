package com.c0767722.maps_monika_c0767722.main;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.c0767722.maps_monika_c0767722.R;
import com.c0767722.maps_monika_c0767722.helper.DistanceCalculation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationProviderClient;
    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int REQUEST_CODE = 1;
    LatLng destLocation;
    Location destination;
    private Marker dest_marker;
    private Polyline polyline;
    private Polygon polygon;
    Marker newMarker;
    String text = "";
    private static final int POLYGON_NUM = 4;
    List<Marker> markersList = new ArrayList<>();
    List<Polyline> polylinesList = new ArrayList<>();
    List<String> res = new ArrayList<>();

    float resultList[] = new float[10];
    Marker markerDistance;
    int markerCount = 0; //marker counter
    boolean dragMarker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        checkPermissions();

    }

    private void checkPermissions() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
        }
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

        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { setHomeLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            startUpdateLocations();
          //  LatLng canadaCenterLatLong = new LatLng( 43.651070,-79.347015);
           // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(canadaCenterLatLong, 5));
        }
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

            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                clearDestinationMap();
            }
        });



        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                List<LatLng> points = polyline.getPoints();
                LatLng latLng1 = points.remove(0);
                LatLng latLng2 = points.remove(0);
                //   Location.distanceBetween(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, resultList);
                //    Toast.makeText(getApplicationContext(), "Distance: " + resultList[0] / 1000 + " km", Toast.LENGTH_SHORT).show();
                //  double distance = resultList[0] / 1000;
                LatLng midPoint = LatLngBounds.builder().include(latLng1).include(latLng2).build().getCenter();
                double distance = DistanceCalculation.totalDistance(latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude);
                showDistance(midPoint, distance, null);

            }

        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon pu) {
                double totalDistance = 0;
                LatLng latLng = null;
                LatLng midPoint = null;
                LatLngBounds.Builder builder = LatLngBounds.builder();
                for(LatLng point: polygon.getPoints()){
                    builder.include(point);
                }
                 midPoint = builder.build().getCenter();

                for (LatLng allpoints : polygon.getPoints()) {
                    if (latLng != null) {
                        totalDistance += DistanceCalculation.totalDistance(latLng.latitude, latLng.longitude, allpoints.latitude, allpoints.longitude);
                    }
                    latLng = allpoints;
                }
                showDistance(midPoint, totalDistance, "");
            }
        });

        mMap.setOnMarkerDragListener(this);
    }

    private void showDistance(LatLng latLng, double distance, String txt) {
        if (markerDistance != null) {
            markerDistance.remove();
        }
        //https://stackoverflow.com/questions/40394823/polyline-with-infowindow-in-android-app
       BitmapDescriptor invisibleMarker = BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Distance:" + String.format(Locale.CANADA, "%.3f", distance) + "km")
                .icon(invisibleMarker)
                .anchor(0f,  0f);

        markerDistance = mMap.addMarker(options);
        markerDistance.showInfoWindow();

    }
    private void startUpdateLocations() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        /*Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setHomeMarker(lastKnownLocation);*/
    }

    private void enablecurrentlocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
        }
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

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
        if (markersList.size() == POLYGON_NUM) {
            reDrawShape();
        }

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
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }
    private void initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void setHomeLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
        if (markerDistance != null) {
            markerDistance.remove();
            markerDistance = null;
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
                    .width(10)
                    .add(marker.getPosition(), dest_marker.getPosition());
            polyline = mMap.addPolyline(polylineOptions);
            polylinesList.add(polyline);
        }
    }

    private void textDisplay(Marker marker, int i) {
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(180, 150, config);
        Canvas canvas = new Canvas(bitmap);

        Paint color = new Paint();
        color.setTextSize(80);
        color.setColor(Color.BLUE);

        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.mipmap.pin), 0, 0, color);
        canvas.drawText(String.valueOf((char) (i + 65)), 90, 57, color);

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        marker.setAnchor(0.32f, 0.88f);
    }


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
            textDisplay(newMarker, i);
            Log.d("ssss", "reDrawShape: " + newMarker.getTitle());
            LatLng latLng = newMarker.getPosition();
            getMarkerOption(latLng);
            newLaLong.add(newMarker.getPosition());
        }
        clearPolyLines();
        newLaLong = reDrawPolyLine(newLaLong);
        getNewPolyLines(newLaLong);
        for (LatLng latLng : newLaLong) {
            polygonOptions.add(latLng);
        }
        polygonOptions.strokeWidth(10);
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
            option.icon(BitmapDescriptorFactory.fromResource(R.mipmap.pin));
            option.draggable(true);
        }
        return option;
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
                .width(10)
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