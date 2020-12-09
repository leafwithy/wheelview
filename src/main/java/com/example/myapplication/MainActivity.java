package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private WheelView wheelView;
    private TextView textView;
    private List<String> timeList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData(){
        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 0; i < 60; i++){
            arrayList.add("" + i);
        }

        timeList = new ArrayList<>(arrayList);
    }

    private void initView(){
        textView = findViewById(R.id.textview);
        wheelView = findViewById(R.id.wheel_view);

        wheelView.setOffset(1);
        wheelView.setWheels(timeList);
//        wheelView.setSelectedIndex(0);
        wheelView.setSelectedValue("3");
        wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {

            }
        });

        textView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (null != wheelView){
                    wheelView.setSelectedValue("30");
                }
            }
        });

    }
}
