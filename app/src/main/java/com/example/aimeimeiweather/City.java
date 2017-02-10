package com.example.aimeimeiweather;

import org.litepal.crud.DataSupport;

/**
 * Created by asus on 2017/2/10.
 */

public class City extends DataSupport {
    private int id;
    private String CityName;//市名
    private int CityCode;//市代号
    private int provinceId;//省ID

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public int getCityCode() {
        return CityCode;
    }

    public void setCityCode(int cityCode) {
        CityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
