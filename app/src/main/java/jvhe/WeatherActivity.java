package jvhe;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.castl.rxandroid_retrofit_demo_jvhe.R;
import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.components.RxActivity;

import java.util.concurrent.TimeUnit;

import RetrofitInterface.NetWork;
import adapter.WeatherAdapter;
import bean.Weather;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class WeatherActivity extends RxActivity {


    private ImageView iv_left;
    private LinearLayout ll_left;
    private ListView lv_weather;
    private Button btn_check;
    private EditText et_city_name;
    private TextView tv_city, tv_date, tv_info, tv_temp, tv_moon, tv_title;

    private static String API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initViews();
        initData();

    }

    private void initViews() {
        setContentView(R.layout.activity_weather);
        lv_weather = (ListView) findViewById(R.id.lv_future_weather);
        et_city_name = (EditText) findViewById(R.id.et_city_name);
        btn_check = (Button) findViewById(R.id.btn_check);
        tv_city = (TextView) findViewById(R.id.tv_weather_city);
        tv_date = (TextView) findViewById(R.id.tv_weather_date);
        tv_info = (TextView) findViewById(R.id.tv_weather_info);
        tv_moon = (TextView) findViewById(R.id.tv_weather_moon);
        tv_temp = (TextView) findViewById(R.id.tv_weather_temp);
        ll_left = (LinearLayout) findViewById(R.id.ll_left);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_left = (ImageView) findViewById(R.id.iv_left);
    }

    private void initData() {
        iv_left.setVisibility(View.VISIBLE);
        tv_title.setText("天气查询");
        ll_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RxView.clicks(btn_check).throttleFirst(3, TimeUnit.SECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        NetWork.getWeatherApi()
                                .getWeatherInfo(et_city_name.getText().toString(), API_KEY)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Weather>() {
                                    @Override
                                    public void call(Weather weather) {
                                        setDispaly(weather);
                                    }
                                });
                    }
                });
    }


    private void setDispaly(Weather weatherinfo) {
        if (weatherinfo.getError_code() == 0) {
            lv_weather.setAdapter(new WeatherAdapter(this, weatherinfo.getResult().getData().getWeather()));
            tv_city.setText(weatherinfo.getResult().getData().getRealtime().getCity_name());
            tv_date.setText(weatherinfo.getResult().getData().getRealtime().getDate());
            tv_info.setText(weatherinfo.getResult().getData().getRealtime().getWeather().getInfo());
            tv_moon.setText(weatherinfo.getResult().getData().getRealtime().getMoon());
            tv_temp.setText(weatherinfo.getResult().getData().getRealtime().getWeather().getTemperature() + "摄氏度");

            setListViewHeightBasedOnChildren(lv_weather);
        } else {
            Toast.makeText(WeatherActivity.this, weatherinfo.getReason(), Toast.LENGTH_SHORT).show();
        }
    }

    //计算listview的高度
    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }
}
