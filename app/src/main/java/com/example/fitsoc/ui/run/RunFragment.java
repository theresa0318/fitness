//Referenced from https://gist.github.com/joshdholtz/4522551
package com.example.fitsoc.ui.run;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitsoc.R;
import com.example.fitsoc.data.FitEvent;
import com.example.fitsoc.data.RunningData;
import com.example.fitsoc.databinding.FragmentRunBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

public class RunFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private final String TAG = "Running";

    private GoogleMap map;
    private MapView mapView;
    private TextView timer;
    private ImageButton startBtn;
    private ImageButton pauseBtn;
    private ImageButton stopBtn;
    private FragmentRunBinding binding;
    private Runnable timerRunnable;
    private RunViewModel model;
    private ImageButton share;
    private TextView textLength;
    private TextView startPos;
    private TextView endPos;

    private MarkerOptions startOptions;
    private MarkerOptions endOptions;
    private Marker startMarker;
    private Marker endMarker;
    private PolylineOptions routeOptions;
    private Polyline route;

    private final LatLng melbourne = new LatLng(-37.8136, 144.9631);
    private static final int DEFAULT_ZOOM = 18;

    private long firstStartTime;
    private long lastStopTime;
    private long startTime;
    private long totalTime;
    private ArrayList<Long> distances = new ArrayList<>();
    private ArrayList<Long> speeds = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Polyline> routes = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationSettingsRequest.Builder builder;
    private LocationRequest locationRequest;
    private List<Location> locationList = new ArrayList<>();
    private Location lastKnownLocation;
    private Location startLocation;
    private Location stopLocation;

    private FitnessOptions fitnessOptions;
    private GoogleSignInAccount account;
    private Session session;

    private boolean isStartRunning = false;
    private boolean firstRun = true;//this is for run before stop
    private boolean requestingLocationUpdates = false;
    private boolean locationPermissionGranted = false;
    private boolean recognitionPermissionGranted = false;
    private boolean isRunningEnd = false;
    private boolean isRunningCont = false;
    private boolean secondRun = false;//this is for another round run after stop

    private final Handler timerHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (!locationPermissionGranted) getLocationPermission();
        if (!recognitionPermissionGranted) getRecognitionPermission();
        model = new ViewModelProvider(this).get(RunViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_run, container, false);
        binding = FragmentRunBinding.inflate(inflater, container, false);
        View run = binding.getRoot();
        setBtnListeners();
        setTimer();
        initiateFitness();

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

    private void initiateFitness() {
        fitnessOptions = FitnessOptions
                .builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                .build();

        account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions);

        account.requestExtraScopes(Fitness.SCOPE_ACTIVITY_READ_WRITE);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Toast.makeText(requireActivity(), "Set Permissions!", Toast.LENGTH_SHORT).show();
            GoogleSignIn.requestPermissions(
                    requireActivity(), // your activity
                    1, // e.g. 1
                    account,
                    fitnessOptions);
        } else {
            Log.i(TAG, "Already has permissions");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setFitness() {
        // TODO TYPE_CALORIES_EXPENDED is not working
        // TODO TYPE_STEP_COUNT_CADENCE is not working
        Fitness.getSensorsClient(requireActivity(), account)
                .findDataSources(
                        new DataSourcesRequest.Builder()
                                .setDataTypes(
                                        DataType.TYPE_CALORIES_EXPENDED,
                                        DataType.TYPE_STEP_COUNT_CADENCE,
                                        DataType.TYPE_SPEED,
                                        DataType.TYPE_DISTANCE_DELTA)
                                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                                .build())
                .addOnSuccessListener(dataSources -> {
                    Log.d(TAG, String.valueOf(dataSources.size()));
                    dataSources.forEach(dataSource -> {
                        setFitListener(dataSource, dataSource.getDataType());
                        //  TODO do something here?
                        Log.i(TAG, "Data source found: " + dataSource.getDataType());
                        Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());
                    });})
                .addOnFailureListener(e ->
                        Log.e(TAG, "Find data sources request failed", e));

//        Fitness.getRecordingClient(requireContext(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
//                // This example shows subscribing to a DataType, across all possible
//                // data sources. Alternatively, a specific DataSource can be used.
//                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
//                .addOnSuccessListener(unused ->
//                        Log.i(TAG, "TYPE_STEP_COUNT_DELTA successfully subscribed!"))
//                .addOnFailureListener( e ->
//                        Log.w(TAG, "There was a problem TYPE_STEP_COUNT_DELTA subscribing.", e));

//        Fitness.getRecordingClient(requireContext(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
//                // This example shows subscribing to a DataType, across all possible
//                // data sources. Alternatively, a specific DataSource can be used.
//                .subscribe(DataType.AGGREGATE_STEP_COUNT_DELTA)
//                .addOnSuccessListener(unused ->
//                        Log.i(TAG, "AGGREGATE_STEP_COUNT_DELTA successfully subscribed!"))
//                .addOnFailureListener( e ->
//                        Log.w(TAG, "There was a problem AGGREGATE_STEP_COUNT_DELTA subscribing.", e));

//        session = new Session.Builder()
//                .setName("Running Data")
//                .setIdentifier("session 1")
//                .setDescription("Morning run")
//                .setActivity(FitnessActivities.RUNNING)
//                .setStartTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .build();


//        Fitness.getRecordingClient(requireContext(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
//                .listSubscriptions()
//                .addOnSuccessListener(subscriptions -> {
//                    for (Subscription sc : subscriptions) {
//                        DataType dt = sc.getDataType();
//                        Log.i(TAG, "Active subscription for data type: " + dt.toString());
//                    }
//                });
    }

    private void setFitListener(DataSource dataSource, DataType dataType) {
        Fitness.getSensorsClient(requireContext(),
                GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
                .add(new SensorRequest.Builder()
                    .setDataSource(dataSource) // Optional but recommended
                    // for custom data sets.
                    .setDataType(dataType) // Can't be omitted.
                    .setSamplingRate(5, TimeUnit.SECONDS)
                    .build(),
                fitListener)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Listener registered!"))
                .addOnFailureListener(task ->
                        Log.e(TAG, "Listener not registered.", task.getCause()));
    }

    private void removeFitListener() {
        Fitness.getSensorsClient(requireContext(),
                GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
                .remove(fitListener)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Listener was removed!"))
                .addOnFailureListener(e ->
                        Log.i(TAG, "Listener was not removed."));
    }


    private final OnDataPointListener fitListener = dataPoint -> {
        for (Field field : dataPoint.getDataType().getFields()) {
            Value value = dataPoint.getValue(field);
            String runningInfo = field.getName();
            if (runningInfo.equals("distance")) {
                distances.add((long) value.asFloat());
            } else if (runningInfo.equals("speed")) {
                speeds.add((long) value.asFloat());
            } else {
                // TODO get other data here
                Log.i(TAG, "Other Data");
            }
            Log.i(TAG, "Detected DataPoint field: " + field.getName());
            Log.i(TAG, "Detected DataPoint value: " + value);
        }
    };

    private void startSession() {
        Fitness.getSessionsClient(requireContext(), account)
                .startSession(session)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Session started successfully!"))
                .addOnFailureListener(e ->
                        Log.w(TAG, "There was an error starting the session", e));
    }

    private void stopSession(boolean isfinished) {
        Fitness.getRecordingClient(requireContext(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(unused -> {
                            Log.i(TAG, "Successfully unsubscribed.");
                            if (isfinished) {
                                getSessionResult();
                            }
                        }
                )
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to unsubscribe.");
                    // Retry the unsubscribe request.
                });
//        Fitness.getRecordingClient(requireContext(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
//                .unsubscribe(DataType.AGGREGATE_STEP_COUNT_DELTA)
//                .addOnSuccessListener(unused ->
//                        Log.i(TAG,"Successfully unsubscribed."))
//                .addOnFailureListener(e -> {
//                    Log.w(TAG, "Failed to unsubscribe.");
//                    // Retry the unsubscribe request.
//                });
    }

    private void getSessionResult() {
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(firstStartTime, lastStopTime, TimeUnit.SECONDS)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setSessionName("Running Session")
                .build();

        Fitness.getSessionsClient(requireContext(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
                .readSession(readRequest)
                .addOnSuccessListener(response -> {
                    // Get a list of the sessions that match the criteria to check the
                    // result.
                    List<Session> sessions = response.getSessions();
                    Log.i(TAG, "Number of returned sessions is:" + sessions.size());
                    for (Session session : sessions) {
                        // Process the session
//                        dumpSession(session);

                        // Process the data sets for this session
                        List<DataSet> dataSets = response.getDataSet(session);
                        for (DataSet dataSet : dataSets) {
                            Log.d(TAG, dataSet.toString());
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.w(TAG, "Failed to read session", e));
    }

    //  Start Running
    private void runningStart() {
        //reset timer
        if(secondRun) {
            //remove all previous run markers
            for(Marker marker:markers){
                marker.remove();
            }
            //remove all previous run routes
            for(Polyline route:routes){
                route.remove();
            }
            totalTime = 0;
            startTime = System.currentTimeMillis();
            setTimer();
        }

        createLocationRequest();
        getLocation(true);
        firstRun = false;
        totalTime = 0;
        firstStartTime = System.currentTimeMillis();
        setFitness();
        runningContinue();
        isRunningCont = true;
        // TODO initiate user data here?
    }

    //  Continue Running
    @SuppressLint({"MissingPermission"})
    private void runningContinue() {
        startTime = System.currentTimeMillis();
        requestingLocationUpdates = true;
        isStartRunning = true;
        timerHandler.postDelayed(timerRunnable, 0);
        Float color;
        if(!isRunningCont) {
            color = BitmapDescriptorFactory.HUE_VIOLET;
        }
        else{
            color = BitmapDescriptorFactory.HUE_RED;
        }
        fusedLocationProviderClient
                .getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        LatLng startLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        startOptions = new MarkerOptions().position(startLatLng)
                                // TODO Some icon changes here?
                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                                // TODO Maybe some "Check Points" here?
                                .title("Starting Point");
                        Marker marker = map.addMarker(startOptions);
                        markers.add(marker);
                        routeOptions = new PolylineOptions().width(15).color(Color.parseColor("#61BF99"));
                        startLocationUpdates();
//                        startSession();
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
        Float color;
        if(!isRunningEnd) {
            color = BitmapDescriptorFactory.HUE_RED;
        }
        else{
             color = BitmapDescriptorFactory.HUE_BLUE;
        }
        fusedLocationProviderClient
                .getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        LatLng startLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        endOptions = new MarkerOptions().position(startLatLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                                .title("End Point");
                        Marker marker = map.addMarker(endOptions);
                        markers.add(marker);
                        stopLocationUpdates();
//                        stopSession(false);
                    }
                });
    }

    //  Stop Running
    private void runningStop() throws IOException {
        getLocation(false);
        lastStopTime = System.currentTimeMillis();
        isRunningEnd = true;
        runningPause();
        removeFitListener();
        writeToDatabase();

        //update activity result content
        updateActResult();

        //reset all attributes
        isRunningCont = false;
        isRunningEnd = false;
        firstRun = true;
        secondRun = true;
        distances.removeAll(distances);
        speeds.removeAll(speeds);
    }

    private void writeToDatabase() {
        long totalDistance = distances.stream().mapToLong(distance -> distance).sum();
        Toast.makeText(requireContext(), "Distance: " + totalDistance, Toast.LENGTH_SHORT).show();
        OptionalDouble avgSpeed = speeds.stream().mapToLong(speed -> speed).average();
        Toast.makeText(requireContext(), "Speed: " + avgSpeed.getAsDouble(), Toast.LENGTH_SHORT).show();
        RunningData data = new RunningData();
        data.setDistance(totalDistance);
        data.setSpeedAVG((long) avgSpeed.getAsDouble());
        data.setStartTime(new Timestamp(startTime));
        data.setEndTime(new Timestamp(lastStopTime));
        data.setTotalTime(totalTime);
        data.setStartLocation(locationList.get(0));
        data.setEndLocation(locationList.get(locationList.size() - 1));
        FitEvent event = new FitEvent(data);
        data.writeToDatabase();
        event.writeToDatabase();
    }

    private void updateActResult() throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        addresses = geocoder.getFromLocation(locationList.get(0).getLatitude(),locationList.get(0).getLongitude(),1);
        startPos = getView().findViewById(R.id.startPosition);
        startPos.setText(addresses.get(0).getAddressLine(0));

        addresses.removeAll(addresses);
        addresses = geocoder.getFromLocation(locationList.get(locationList.size()-1).getLatitude(),locationList.get(locationList.size()-1).getLongitude(),1);
        endPos = getView().findViewById(R.id.destination);
        endPos.setText(addresses.get(0).getAddressLine(0));

        // TODO distance 可能要判断一下 小于1km用m代替类似history
        int totalDistance = (int) distances.stream().mapToLong(distance -> distance).sum();
        textLength = getView().findViewById(R.id.length);
        textLength.setText(totalDistance);

        // TODO Ranks left
    }

    private void setBtnListeners() {
        // TODO simplify codes here
        startBtn = binding.timeStart;
        pauseBtn = binding.timePause;
        stopBtn = binding.timeStop;
        share = binding.share;
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
            if (isStartRunning) {
                try {
                    runningStop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        share.setOnClickListener(view -> {
            ShotShareUtil.shotShare(requireContext());
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void getRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requireRecognitionPermission();
        } else {
            recognitionPermissionGranted = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requireRecognitionPermission() {
        requestPermissionLauncher2.launch(android.Manifest.permission.ACTIVITY_RECOGNITION);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher2 =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // TODO Can add some operations here?
                    recognitionPermissionGranted = true;
                    Log.d(TAG, "Permission Get Daze!");
                } else {
                    // TODO Can add some operations here?
                    recognitionPermissionGranted = false;
                    Log.d(TAG, "Permission Get Daze???");
                }
            });

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
                routes.add(route);
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
        initiateLocation();
        updateLocationUI();
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