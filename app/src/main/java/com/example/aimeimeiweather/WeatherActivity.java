package com.example.aimeimeiweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.aimeimeiweather.gson.Forecast;
import com.example.aimeimeiweather.gson.Weather;
import com.example.aimeimeiweather.service.AutoUpdateService;
import com.example.aimeimeiweather.util.HttpUtil;
import com.example.aimeimeiweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ScrollView weatherLayout;
    private ImageView bingPicImg;
    private String mWeatherId;
    public SwipeRefreshLayout swipeRefresh;//修改为public，在ChooseAreaFragment中使用到
    public DrawerLayout drawerLayout;//修改为public，在ChooseAreaFragment中使用到
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {//Android5.0以上才会执行,状态栏与背景图融合一体
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(//活动的布局会显示在状态栏上面
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置成透明色
        }
        setContentView(R.layout.activity_weather);
        //初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);               //城市名
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);  //更新时间
        degreeText = (TextView) findViewById(R.id.degree_text);             //当前气温
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);  //天气概括
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout); //预报：ListView
        aqiText = (TextView) findViewById(R.id.aqi_text);                   //空气质量：AQI指数
        pm25Text = (TextView) findViewById(R.id.pm25_text);                 //空气质量：PM2.5指数
        comfortText = (TextView) findViewById(R.id.comfort_text);           //舒适度
        carWashText = (TextView) findViewById(R.id.car_wash_text);          //洗车指数
        sportText = (TextView) findViewById(R.id.sport_text);               //运动建议
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);           //背景图片
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);     //侧滑效果的主屏幕
        //侧滑左菜单
        navButton = (Button) findViewById(R.id.nav_button);
        //下拉刷新
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//设置下拉进度条颜色
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);//打开滑动菜单
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);//内存中读取图片
        } else {
            loadBingPic();//请求今日的“必应”背景图
        }
    }


    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=b37146b0d7e74879a932ae85a04e6652";//自己的
//        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";//郭霖的
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;//保存更新后的Id
                            showWeatherInfo(weather);//保存数据后再调用该方法读取数据
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);//刷新事件结束
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);//刷新事件结束
                    }
                });
            }
        });
        loadBingPic();//加载“必应”每日一图
    }

    /**
     * 加载“必应”每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);       //天气预报日期
            TextView infoText = (TextView) view.findViewById(R.id.info_text);       //天气概括
            TextView minMaxText = (TextView) view.findViewById(R.id.min_max_text);  //温度范围
            TextView windText = (TextView) view.findViewById(R.id.wind_text);       //风向
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            minMaxText.setText(forecast.temperature.min + "~" + forecast.temperature.max + "℃");
            windText.setText(forecast.wind.dir);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
