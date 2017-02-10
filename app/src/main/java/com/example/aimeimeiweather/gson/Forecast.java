package com.example.aimeimeiweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    @SerializedName("wind")
    public Wind wind;

    public class Temperature {

        public String max;

        public String min;



    }

    public class More {

        @SerializedName("txt_d")
        public String info;

    }

    public class Wind {

        public String dir;

    }

}
