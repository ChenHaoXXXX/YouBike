package com.chenhao.youbike.ui;

import static com.chenhao.youbike.Function.distanceBetween;
import static com.chenhao.youbike.Function.getLocalTime;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chenhao.youbike.R;
import com.chenhao.youbike.Function;
import com.chenhao.youbike.model.TaipeiBike;
import com.chenhao.youbike.network.BikeManger;
import com.chenhao.youbike.ui.adapter.InfoWindowAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.chenhao.youbike.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener{
    private static final String TAG = MapsActivity.class.getSimpleName();

    // The Fused Location Provider provides access to location APIs.
    private FusedLocationProviderClient fusedLocationClient;

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private BikeManger bikeManger = new BikeManger();

    private TaipeiBike selectBike;
    private CountDownTimer timer;
    double userLatitude = 25.02351;
    double userLongitude = 121.54282;

    private List<TaipeiBike> bikeList = new ArrayList<>();
    private List<Marker> markers =  new ArrayList<>();;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        selectBike = getIntent().getParcelableExtra("ubike");
        //check location Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.error)
                    .setTitle("請至設定開啟應用程式位置權限")
                    .setCancelable(false)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
           return;

        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding.toolbarMap.setNavigationIcon(R.drawable.ic_back);
        binding.toolbarMap.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if(selectBike != null) {
            showMapInfo(selectBike.getSna(),selectBike.getLatitude(),selectBike.getLongitude(),selectBike.getDistance());
            Log.d(TAG, "onCreate: " + selectBike.getSno());
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setInfoWindowAdapter(new InfoWindowAdapter(MapsActivity.this));

        // Add a marker in Sydney and move the camera
        if(selectBike != null) {
            //點擊列表選項
            LatLng sydney = new LatLng(selectBike.getLatitude(), selectBike.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(sydney)
                    .snippet( "可借車輛:"+ selectBike.getAvailable_rent_bikes() + "\n" + "可停空位:" + selectBike.getAvailable_return_bikes())
                    .title(selectBike.getSna()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18f));
        }else {
            //點擊地圖模式
            LatLng sydney = new LatLng(25.02351, 121.54282);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18f));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                         PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // thread wait for another thread's output
        CountDownLatch latch = new CountDownLatch(1);
        requestCurrentLocation(latch);
        startLoading();
        bikeManger.getData((newBikeList) -> {
            if(newBikeList != null) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                updateBilkList(newBikeList);
            }else {
                runOnUiThread(() -> {
                    stopLoading();
                });

            }
        });
        setupDownCounterTimer(60000);

    }

    private void updateBilkList(List<TaipeiBike> newBikeList) {
        runOnUiThread(() -> {
            if(newBikeList != null) {
                if(selectBike == null) {
                    LatLng sydney = new LatLng(userLatitude, userLongitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18f));
                }

                //添加與使用者距離
                for(int i = 0; i < newBikeList.size() ; i++) {
                    TaipeiBike taipeiBike = newBikeList.get(i);
                    float distance = distanceBetween(userLatitude,userLongitude
                            ,taipeiBike.getLatitude(),taipeiBike.getLongitude());
                    taipeiBike.setDistance(distance);
                    taipeiBike.getSna().replace("YouBike2.0_","");

                }

                //依距離排序
                Collections.sort(newBikeList, new Function.bikeSort());
                bikeList.clear();
                bikeList.addAll(newBikeList);

                binding.textTimeMap.setText("最後更新時間: " +  getLocalTime());
                setupMap();
                stopLoading();

            }else {

                stopLoading();


            }
        });




    }

    private void setupMap() {
        mMap.clear();
        markers.clear();
        mMap.setInfoWindowAdapter(new InfoWindowAdapter(MapsActivity.this));

        //取得前50筆資料
        //List<TaipeiBike> bikes = bikeList.subList(pos, pos + 50);
        for (TaipeiBike uBike : bikeList) {
            LatLng latLng = new LatLng(Double.valueOf(uBike.getLatitude()), Double.valueOf(uBike.getLongitude()));
            Marker marker =  mMap.addMarker(new MarkerOptions().position(latLng).title(uBike.getSna())
                    .snippet( "可借車輛:"+ uBike.getAvailable_rent_bikes() + "\n" + "可停空位:" + uBike.getAvailable_return_bikes()));
            markers.add(marker);

            if(selectBike != null) {
                if(marker.getPosition().latitude == selectBike.getLatitude()
                        && marker.getPosition().longitude  == selectBike.getLongitude()){
                    marker.showInfoWindow();

                }
            }


        }
        mMap.setOnMarkerClickListener(this);
    }

    private void showMapInfo(String name, Double lat, Double lng, float distance) {
        binding.layoutInfo.setVisibility(View.VISIBLE);
        binding.textTitleMap.setText(name);
        if (distance >= 1000) {
            BigDecimal f = new BigDecimal(distance / 1000);
            String result =f.setScale(2, BigDecimal.ROUND_HALF_UP).toString(); //取小數後兩位
            binding.textMapDistance.setText("約" + result + "公里");
        }else{
            binding.textMapDistance.setText("約" + distance + "公尺");
        }
        binding.buttonGoogleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps?&daddr=" +
                        String.valueOf(lat)+ ","+ String.valueOf(lng) +
                        "&dirflg=w"; //googleMap設定值
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }

    private void requestCurrentLocation(CountDownLatch latch) {

        // Request permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            //getUserLocation
            Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            );

            currentLocationTask.addOnCompleteListener((new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    String result = "";
                    if (task.isSuccessful() && location != null) {
                        // Task completed successfully
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        latch.countDown();
                        Log.d(TAG, "onComplete: " );
                    } else {
                        // Task failed with an exception
                        Exception exception = task.getException();
                        result = "Exception thrown: " + exception;
                        Log.d(TAG, "onComplete: result");
                        Toast.makeText(MapsActivity.this,"無法取得當前位置", Toast.LENGTH_SHORT).show();
                        latch.countDown();
                    }


                }
            }));
        } else {
            Log.d(TAG, "Request fine location permission.");
        }
    }



    public void setupDownCounterTimer(long timeLengthMilli){
        timer = new CountDownTimer(timeLengthMilli, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.textTimerMap.setText("  "+ millisUntilFinished / 1000 +  "秒後自動更新");
            }

            @Override
            public void onFinish() {
                binding.textTimerMap.setText("更新中......");
                bikeManger.getData((newBikeList) -> {
                    if(newBikeList != null) {
                        updateBilkList(newBikeList);
                    }else {
                        runOnUiThread(() -> {
                            stopLoading();
                        });

                    }

                });
                timer.start();
            }
        };

        timer.start();

    }

    private void startLoading() {
        binding.layoutLoadingMap.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        binding.layoutLoadingMap.setVisibility(View.GONE);
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        String title = marker.getTitle();

        double marketLatitude = marker.getPosition().latitude;
        double marketLongitude = marker.getPosition().longitude;
        float distance = distanceBetween(userLatitude, userLongitude, //取得距離
                marketLatitude,  marketLongitude);
        showMapInfo(title,marketLatitude,marketLongitude,distance);

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "已顯示目前位置", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}