package com.chenhao.youbike.ui;

import static com.chenhao.youbike.Function.distanceBetween;
import static com.chenhao.youbike.Function.getLocalTime;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.Manifest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chenhao.youbike.R;
import com.chenhao.youbike.Function;
import com.chenhao.youbike.database.BikeDatabase;
import com.chenhao.youbike.databinding.ActivityMainBinding;
import com.chenhao.youbike.model.Bike;
import com.chenhao.youbike.model.TaipeiBike;
import com.chenhao.youbike.network.BikeManger;
import com.chenhao.youbike.ui.adapter.BikeAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    // The Fused Location Provider provides access to location APIs.
    private FusedLocationProviderClient fusedLocationClient;

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private BikeManger bikeManger = new BikeManger();
    private List<TaipeiBike> bikeList = new ArrayList<>();
    private List<TaipeiBike> favoriteBikeList = new ArrayList<>();
    private BikeAdapter adapter;
    private CountDownTimer timer;
    private String searchString = "";
    private Boolean isLocation = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //toolbar
        setSupportActionBar(binding.toolbar);

        //recyclerview
        binding.recyclerBike.setHasFixedSize(true);
        binding.recyclerBike.setLayoutManager(new LinearLayoutManager(this));

        startLoading();
        if(checkInternet()){
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                    Log.d(TAG, "onCreate: // Precise location access granted.");
                                    isLocation = true;
                                    bikeManger.getData((newBikeList) -> {
                                        if (newBikeList != null) {
                                            //set adapter
                                            bikeList.addAll(newBikeList);
                                            setAdapter(bikeList);
                                            //get user location
                                            requestCurrentLocation(newBikeList);
                                        } else {
                                            runOnUiThread(() -> {
                                                setFailedDialog();
                                                stopLoading();
                                            });

                                        }

                                    });
                                } else if (coarseLocationGranted != null
                                        && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                    Log.d(TAG, "onCreate: // Only approximate location access granted..");
                                    isLocation = true;
                                    bikeManger.getData((newBikeList) -> {
                                        if (newBikeList != null) {
                                            //set adapter
                                            bikeList.addAll(newBikeList);
                                            setAdapter(bikeList);
                                            //get user location
                                            requestCurrentLocation(newBikeList);
                                        } else {
                                            setFailedDialog();
                                            stopLoading();
                                        }

                                    });
                                } else {
                                    // No location access granted.
                                    setLocationDialog();

                                }


                            }
                    );

            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });

            //set get data timer
            setupDownCounterTimer(60000);
        }

        binding.buttonMap.setOnClickListener(v -> {
            Intent intent = new Intent(this,MapsActivity.class);
            startActivity(intent);
        });

        binding.buttonFavorite.setOnClickListener(v -> {
            SearchView searchView = (SearchView) binding.toolbar.findViewById(R.id.action_search);
            boolean isSelected = binding.buttonFavorite.isSelected();
            binding.buttonFavorite.setSelected(!isSelected);
            if(!isSelected){
                searchView.setVisibility(View.GONE);
                searchView.setQuery("",false);
                binding.buttonFavorite.setText("列表模式");
            }else {
                searchView.setVisibility(View.VISIBLE);
                binding.buttonFavorite.setText("我的最愛");
            }
            startLoading();
            bikeManger.getData((newBikeList) -> {
                if(newBikeList != null) {
                    requestCurrentLocation(newBikeList);
                }else {
                    stopLoading();
                }

            });

        });
    }



    private void setAdapter(List<TaipeiBike> bikeList) {
        Log.d(TAG, "setAdapter: " + bikeList.size());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new BikeAdapter(bikeList,MainActivity.this);
                binding.recyclerBike.setAdapter(adapter);
                binding.recyclerBike.getAdapter().notifyDataSetChanged();
            }
        });
    }





    private void updateAdapterList(List<TaipeiBike> newBikeList)  {
        new Thread(() -> {
            //取得我的最愛列表
            List<Bike> dataBikes = BikeDatabase.getInstance(MainActivity.this).bikeDao().getAll();
            Log.d(TAG, "test: " + dataBikes.size());
            runOnUiThread(() -> {
                favoriteBikeList.clear();
                for(TaipeiBike bike: newBikeList){
                    for (Bike dataBike : dataBikes) {
                        if(bike.getSno().equals(dataBike.sno)){
                            //資料庫裡如果有這筆資料就設為true
                            bike.setStar(true);
                            favoriteBikeList.add(bike);
                        }
                    }
                }

                //更換列表
                bikeList.clear();
                if(binding.buttonFavorite.isSelected()) {
                    if(favoriteBikeList.size() == 0) {
                        binding.textListNoBike.setVisibility(View.VISIBLE);
                    }
                    bikeList.addAll(favoriteBikeList);
                    adapter.notifyDataSetChanged();
                } else {
                    binding.textListNoBike.setVisibility(View.GONE);
                    //取得前20筆資料
                    if(newBikeList.size() > 21) {
                        List<TaipeiBike> taipeiBikes = newBikeList.subList(0, 20);
                        bikeList.addAll(taipeiBikes);
                    } else {
                        bikeList.addAll(newBikeList);
                    }

                    //篩選搜尋欄字串
                    if(!searchString.isEmpty()) {
                        adapter.filter(searchString);
                    }else {
                        adapter.notifyDataSetChanged();
                    }



                }


            });
        }).start();


    }

    private void requestCurrentLocation(List<TaipeiBike> newBikeList) {

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

                    adapter.bikeListCopy.clear();
                    adapter.bikeListCopy.addAll(newBikeList);

                    Location location = task.getResult();
                    String result = "";
                    if (task.isSuccessful() && location!=null) {
                        // Task completed successfully
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        result = "Location (success): " + lat + ", " + lng;

                        binding.textTimeList.setText("最後更新時間: " +  getLocalTime());

                        //添加與使用者距離
                        for (TaipeiBike taipeiBike : newBikeList) {
                            float distance = distanceBetween(lat,lng
                                    ,taipeiBike.getLatitude(),taipeiBike.getLongitude());
                            taipeiBike.setDistance(distance);
                        }
                        //依距離排序
                        Collections.sort(newBikeList, new Function.bikeSort());

                        updateAdapterList(newBikeList);


                    } else {
                        // Task failed with an exception
                        Exception exception = task.getException();
                        result = "Exception thrown: " + exception;
                        Toast.makeText(MainActivity.this,"無法取得當前位置", Toast.LENGTH_SHORT).show();
                    }

                    Log.d(TAG, "getCurrentLocation() result: " + result);
                    stopLoading();
                }
            }));
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"無法取得當前位置", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Request fine location permission.");
                    stopLoading();
                }
            });

        }

    }

    private void setupDownCounterTimer(long timeLengthMilli){
        timer = new CountDownTimer(timeLengthMilli, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.textTimerList.setText("  "+ millisUntilFinished / 1000 +  "秒後自動更新");
            }

            @Override
            public void onFinish() {
                binding.textTimerList.setText("清單更新中......");
                bikeManger.getData((newBikeList) -> {
                    if(newBikeList != null) {
                        requestCurrentLocation(newBikeList);
                    }else {
                        runOnUiThread(() -> {
                            setFailedDialog();
                            stopLoading();
                        });

                    }

                });
                timer.start();
            }
        };

        timer.start();

    }




    private boolean checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.error)
                    .setTitle("無法連結到網路")
                    .setCancelable(false)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }).show();
            return false;
        }
        return true;
    }

    private void setFailedDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.error)
                .setTitle("無法取得資料，請稍後再試")
                .setCancelable(false)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void setLocationDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.error)
                .setTitle("請至設定開啟應用程式位置權限")
                .setCancelable(false)
                .setPositiveButton("設定", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    bikeManger.getData((newBikeList) -> {
                        if (newBikeList != null) {
                            //set adapter
                            bikeList.addAll(newBikeList);
                            setAdapter(bikeList);
                            //get user location
                            requestCurrentLocation(newBikeList);
                        } else {
                            setFailedDialog();
                            stopLoading();
                        }

                    });
                }).show();
    }



    private void startLoading() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        binding.layoutLoading.setVisibility(View.GONE);
    }





    public void timerPause() {
        if(timer != null) {
            timer.cancel();
        }
    }

    private void timerResume() {
        timer.start();;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextChange: " + query);
                searchString = query;
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                searchString = newText;
                adapter.filter(newText);
                return false;
            }
        });


        return true;
    }





    @Override
    protected void onStop() {
        super.onStop();
        timerPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        timerResume();
        if(!isLocation) {
            bikeManger.getData((newBikeList) -> {
                if (newBikeList != null) {
                    //set adapter
                    bikeList.addAll(newBikeList);
                    setAdapter(bikeList);
                    //get user location
                    requestCurrentLocation(newBikeList);
                } else {
                    setFailedDialog();
                    stopLoading();
                }

            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancellationTokenSource.cancel();
    }


}



