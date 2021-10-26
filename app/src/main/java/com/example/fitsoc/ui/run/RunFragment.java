//Referenced from https://gist.github.com/joshdholtz/4522551
package com.example.fitsoc.ui.run;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitsoc.R;
import com.example.fitsoc.databinding.FragmentRunBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class RunFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
    private GoogleMap map;
    private MapView mapView;
    private TextView timer;
    private ImageButton startBtn;
    private ImageButton pauseBtn;
    private ImageButton stopBtn;
    private FragmentRunBinding binding;
    private Runnable timerRunnable;
    private RunViewModel model;

    private MarkerOptions startOptions;
    private MarkerOptions endOptions;
    private Marker startMarker;
    private Marker endMarker;
    private PolylineOptions routeOptions;
    private Polyline route;

    private final LatLng melbourne = new LatLng(-37.8136, 144.9631);
    private static final int DEFAULT_ZOOM = 15;

    private long startTime;
    private long totalTime;
    private boolean isStartRunning;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationSettingsRequest.Builder builder;
    private LocationRequest locationRequest;
    private List<Location> locationList;
    private Location lastKnownLocation;
    private Location startLocation;
    private Location stopLocation;
    private boolean firstRun;
    private boolean requestingLocationUpdates;
    private boolean locationPermissionGranted;

    private final Handler timerHandler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationList = new ArrayList<>();
        firstRun = true;
        isStartRunning = false;
        if (!locationPermissionGranted) getLocationPermission();
        model = new ViewModelProvider(this).get(RunViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_run, container, false);
        binding = FragmentRunBinding.inflate(inflater, container, false);
        View run = binding.getRoot();
        setBtnListeners();
        setTimer();

        mapView = (MapView) run.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return run;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        // TODO other tasks onResume here
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        // TODO other tasks onPause here
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //  Start Running
    private void runningStart() {
        createLocationRequest();
        getLocation(true);
        firstRun = false;
        totalTime = 0;
        runningContinue();
        // TODO initiate user data here?
    }

    //  Continue Running
    @SuppressLint({"MissingPermission"})
    private void runningContinue() {
        startTime = System.currentTimeMillis();
        requestingLocationUpdates = true;
        isStartRunning = true;
        timerHandler.postDelayed(timerRunnable, 0);
        fusedLocationProviderClient
                .getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        LatLng startLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        startOptions = new MarkerOptions().position(startLatLng)
                                // TODO Some icon changes here?
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                // TODO Maybe some "Check Points" here?
                                .title("Starting Point");
                        map.addMarker(startOptions);
                        routeOptions = new PolylineOptions().width(15).color(Color.parseColor("#61BF99"));
                        startLocationUpdates();
                    }
                });
    }

    //  Pause Running
    @SuppressLint({"MissingPermission"})
    private void runningPause() {
        totalTime = totalTime + System.currentTimeMillis() - startTime;
        requestingLocationUpdates = false;
        isStartRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        fusedLocationProviderClient
                .getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        LatLng startLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        endOptions = new MarkerOptions().position(startLatLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                .title("End Point");
                        map.addMarker(endOptions);
                        stopLocationUpdates();
                    }
                });
    }

    //  Stop Running
    private void runningStop() {
        getLocation(false);
        runningPause();
        // TODO store data here...
    }

    private void setBtnListeners() {
        // TODO simplify codes here
        startBtn = binding.timeStart;
        pauseBtn = binding.timePause;
        stopBtn = binding.timeStop;
        startBtn.setOnClickListener(view -> {
            if (!isStartRunning) {
                if (firstRun) runningStart();
                else runningContinue();
            }
        });
        pauseBtn.setOnClickListener(view -> {
            if (isStartRunning) runningPause();
        });
        stopBtn.setOnClickListener(view -> {
            if (isStartRunning) runningStop();
        });
    }

    private void getLocationPermission() {
        //  Request location permission
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //  TODO some operations here?
            //  if already permitted, set flag -> true
            locationPermissionGranted = true;
        } else {
            //  TODO some operations here?
            //  if not permitted, require the permission
            requireLocationPermission();
        }
    }

    private void requireLocationPermission() {
        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // TODO Can add some operations here?
                    locationPermissionGranted = true;
                } else {
                    // TODO Can add some operations here?
                    locationPermissionGranted = false;
                }
            });

    private void createLocationRequest() {
        // initiate location request
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        // TODO some listeners can be set for task?
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                Log.d("Location Update: ", location.toString());
                locationList.add(location);
                LatLng nowLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                routeOptions.add(nowLatLng);
                route = map.addPolyline(routeOptions);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        nowLatLng, DEFAULT_ZOOM));
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void getLocation(boolean isStartLocation) {
        fusedLocationProviderClient
            .getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
            .addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    if (isStartLocation) startLocation = task.getResult();
                    else stopLocation = task.getResult();
                } else {
                    if (isStartLocation) startLocation = null;
                    else stopLocation = null;
                }
            });
    }

    @SuppressLint("MissingPermission")
    private void initiateLocation() {
        fusedLocationProviderClient
            .getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
            .addOnCompleteListener(requireActivity(), task -> {
                if (task.isSuccessful()) {
                    lastKnownLocation = task.getResult();
                    LatLng nowLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            nowLatLng, DEFAULT_ZOOM));
                } else {
                    lastKnownLocation = null;
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            melbourne, DEFAULT_ZOOM));
                }
            });
    }

    private CancellationToken cancellationToken = new CancellationToken() {
        //  TODO ??? here
        @Override
        public boolean isCancellationRequested() {
            return false;
        }

        @NonNull
        @Override
        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
            return null;
        }
    };

    private void setTimer() {
        timer = binding.timeCalculator;
        timerRunnable = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                long millis = totalTime + System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;

                timer.setText(String.format("Time: %02d:%02d:%02d", hours, minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }
        };
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap mMap) {
        map = mMap;
        updateLocationUI();
        initiateLocation();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
    }

    private void updateLocationUI() {
        // show user location UI
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //  TODO some toast content:
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT)
                .show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        //  TODO some toast content:
        Toast.makeText(getContext(), "Current location:\n" + location, Toast.LENGTH_LONG)
                .show();
    }
}