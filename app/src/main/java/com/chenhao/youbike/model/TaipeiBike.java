package com.chenhao.youbike.model;

import static java.lang.System.out;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class TaipeiBike implements Parcelable {

    private String sno; //站點代號
    private String sna; //場站中文名稱
    private String sarea; //場站區域
    private String mday; //資料更新時間
    private String ar; //地點
    private String sareaen; // 場站區域英文
    private String snaen; //場站名稱英文
    private String aren; //地址英文
    private String act; //全站禁用狀態
    private String srcUpdateTime; //系統發布資料更新的時間
    private String updateTime; //大數據平台經過處理後將資料存入DB的時間
    private String infoTime; //各場站來源資料更新時間
    private String infoDate; //各場站來源資料更新時間)
    private int total; //場站總停車格
    @SerializedName("available_rent_bikes")
    private int available_rent_bikes; //場站目前車輛數量
    private double latitude; //緯度
    private double longitude; //經度
    @SerializedName("available_return_bikes")
    private int available_return_bikes; //空位數量

    private float distance = 0.0f; //距離
    private boolean star = false;  //我的最愛
    private TaipeiBike(Parcel in) {
        sno = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        sna = in.readString();
        available_rent_bikes = in.readInt();
        available_return_bikes = in.readInt();
        distance = in.readFloat();


    }
    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getSna() {
        return sna.replace("YouBike2.0_","");
    }

    public void setSna(String sna) {
        this.sna = sna;
    }

    public String getSarea() {
        return sarea;
    }

    public void setSarea(String sarea) {
        this.sarea = sarea;
    }

    public String getMday() {
        return mday;
    }

    public void setMday(String mday) {
        this.mday = mday;
    }

    public String getAr() {
        return ar;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public String getSareaen() {
        return sareaen;
    }

    public void setSareaen(String sareaen) {
        this.sareaen = sareaen;
    }

    public String getSnaen() {
        return snaen;
    }

    public void setSnaen(String snaen) {
        this.snaen = snaen;
    }

    public String getAren() {
        return aren;
    }

    public void setAren(String aren) {
        this.aren = aren;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getSrcUpdateTime() {
        return srcUpdateTime;
    }

    public void setSrcUpdateTime(String srcUpdateTime) {
        this.srcUpdateTime = srcUpdateTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getInfoTime() {
        return infoTime;
    }

    public void setInfoTime(String infoTime) {
        this.infoTime = infoTime;
    }

    public String getInfoDate() {
        return infoDate;
    }

    public void setInfoDate(String infoDate) {
        this.infoDate = infoDate;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getAvailable_rent_bikes() {
        return available_rent_bikes;
    }

    public void setAvailable_rent_bikes(int available_rent_bikes) {
        this.available_rent_bikes = available_rent_bikes;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getAvailable_return_bikes() {
        return available_return_bikes;
    }

    public void setAvailable_return_bikes(int available_return_bikes) {
        this.available_return_bikes = available_return_bikes;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(sno);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(sna);
        parcel.writeInt(available_rent_bikes);
        parcel.writeInt(available_return_bikes);
        parcel.writeFloat(distance);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<TaipeiBike> CREATOR = new Parcelable.Creator<TaipeiBike>() {
        public TaipeiBike createFromParcel(Parcel in) {
            return new TaipeiBike(in);
        }

        public TaipeiBike[] newArray(int size) {
            return new TaipeiBike[size];
        }
    };


}

