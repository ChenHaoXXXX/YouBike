package com.chenhao.youbike.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chenhao.youbike.R;
import com.chenhao.youbike.Function;
import com.chenhao.youbike.database.BikeDatabase;
import com.chenhao.youbike.databinding.RowBikeBinding;
import com.chenhao.youbike.model.Bike;
import com.chenhao.youbike.model.TaipeiBike;
import com.chenhao.youbike.ui.MapsActivity;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BikeAdapter extends RecyclerView.Adapter<BikeAdapter.BikeHolder> {

    private static final String TAG = BikeAdapter.class.getSimpleName();
    List<TaipeiBike> bikeList;
    public List<TaipeiBike> bikeListCopy = new ArrayList<>();
    private Context context;

    public BikeAdapter(List<TaipeiBike> bikeList, Context context) {
        this.bikeList = bikeList;
        this.context = context;
        bikeListCopy.addAll(bikeList);

    }



    @NonNull
    @Override
    public BikeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowBikeBinding binding =
                RowBikeBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new BikeHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BikeHolder holder, int position) {
        TaipeiBike uBike = bikeList.get(position);

        holder.lendText.setText("可借車輛:" + uBike.getAvailable_rent_bikes());
        holder.parkingText.setText("可停空位:"+uBike.getAvailable_return_bikes());
        holder.titleText.setText(uBike.getSna());

        if(uBike.getDistance()>=1000){
            BigDecimal f = new BigDecimal(uBike.getDistance()/1000);
            String result = f.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
            holder.distanceText.setText("約"+ result + "公里");
        }else {
            holder.distanceText.setText("約"+ uBike.getDistance() + "公尺");
        }

        if(uBike.isStar()){
            holder.loveImage.setImageResource(R.drawable.ic_love);
        }else {
            holder.loveImage.setImageResource(R.drawable.ic_love_empty);
        }

        holder.loveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uBike.setStar(!uBike.isStar());
                if(uBike.isStar()) {
                    holder.loveImage.setImageResource(R.drawable.ic_love);
                    Bike bike = new Bike(uBike.getSno(),true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BikeDatabase.getInstance(context).bikeDao().insert(bike);
                        }
                    }).start();
                }else{
                    holder.loveImage.setImageResource(R.drawable.ic_love_empty);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bike result = BikeDatabase.getInstance(context).bikeDao().findByBike(uBike.getSno());
                            BikeDatabase.getInstance(context).bikeDao().delete(result);
                        }
                    }).start();
                }

            }
        });


        holder.itemView.setOnClickListener(v -> {
            String bikeListStr = (new Gson()).toJson(bikeList);
            Log.d(TAG, "onBindViewHolder: " + bikeListStr);
            Intent intent = new Intent(v.getContext(),MapsActivity.class);
            intent.putExtra("ubike", uBike);
            v.getContext().startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return bikeList.size();
    }

    public class  BikeHolder extends RecyclerView.ViewHolder {
        ImageView loveImage;
        TextView titleText;
        TextView distanceText;
        TextView lendText;
        TextView parkingText;
        public BikeHolder(@NonNull RowBikeBinding binding) {
            super(binding.getRoot());

            loveImage = binding.imageLove;
            titleText = binding.textTitle;
            distanceText = binding.textDistance;
            lendText = binding.textLend;
            parkingText  = binding.textParking;

        }
    }



    public void filter(String text) {
        bikeList.clear();
        if(text.isEmpty()){
            bikeList.addAll(bikeListCopy);
        } else{
            text = text.toLowerCase();
            for(TaipeiBike bike: bikeListCopy){
                String localName = bike.getSna().replace("YouBike2.0_","");
                if(localName.toLowerCase().contains(text)){
                    bikeList.add(bike);

                }
            }
        }

        Collections.sort(bikeList, new Function.bikeSort());
        Log.d(TAG, "filter: " + bikeList.size());
        notifyDataSetChanged();
    }
}
