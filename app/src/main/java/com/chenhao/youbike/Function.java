package com.chenhao.youbike;

import android.location.Location;

import com.chenhao.youbike.model.TaipeiBike;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Function {

    //依距離做排序
    public static class bikeSort implements Comparator<TaipeiBike> {

        @Override
        public int compare(TaipeiBike a, TaipeiBike b) {
            return Float.compare(a.getDistance(), b.getDistance());
        }
    }

    //計算直線距離
    public static float distanceBetween(double startLatitude,double startLongitude,double endLatitude, double endLongitude)
    {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude,startLongitude,
                endLatitude, endLongitude, results);

        BigDecimal f = new BigDecimal(results[0]);
        float result = f.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();
        //Log.d(TAG, "distanceBetween: " + result);
        return result;
    }

    public static String getLocalTime()  {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("HH:mm:ss");
        Date date = new Date();
        System.out.println("现在时间：" + sdf.format(date));
        return sdf.format(date);
    }
}
