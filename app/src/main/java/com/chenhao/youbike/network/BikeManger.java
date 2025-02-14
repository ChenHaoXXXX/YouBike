package com.chenhao.youbike.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.chenhao.youbike.model.TaipeiBike;
import com.chenhao.youbike.myInterface.UpdateView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BikeManger {
    private static final String TAG = BikeManger.class.getSimpleName();
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build();
    private final String url = "https://tcgbusfs.blob.core.windows.net/dotapp/youbike/v2/youbike_immediate.json";


    public void getData(UpdateView updateView) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 建立Call
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                List<TaipeiBike> bikeList = parseJson(result);
                updateView.refreshView(bikeList);

            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                updateView.refreshView(null);
            }
        });


    }

    public List<TaipeiBike>  parseJson(String json) {
        List<TaipeiBike> bikeList = new ArrayList<>();

        Gson gson = new Gson();
        bikeList = gson.fromJson(json,
                new TypeToken<ArrayList<TaipeiBike>>(){}.getType());

        Log.d(TAG, "parseJson: " + bikeList.size());
        return bikeList;
    }


}
