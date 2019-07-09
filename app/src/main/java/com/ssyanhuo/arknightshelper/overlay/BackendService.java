package com.ssyanhuo.arknightshelper.overlay;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.google.android.material.tabs.TabLayout;
import com.ssyanhuo.arknightshelper.R;
import com.ssyanhuo.arknightshelper.utiliy.DpUtiliy;
import com.ssyanhuo.arknightshelper.utiliy.JsonUtility;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class BackendService extends Service {
    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    LinearLayout linearLayout;
    LinearLayout linearLayout_hr;
    LinearLayout linearLayout_exp;
    LinearLayout linearLayout_material;
    Button button;
    ArrayList<CheckBox> checkBoxes;
    ArrayList<String> selectedStar;
    ArrayList<String> selectedQualification;
    ArrayList<String> selectedPosition;
    ArrayList<String> selectedSex;
    ArrayList<String> selectedType;
    ArrayList<String> selectedTag;
    String hrJson;
    String expJson;
    String materialJson;
    final int HR = 0;
    final int EXP = 1;
    final int MATERIAL = 2;
    final int CLOSE = 3;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, BroadcastReceiver.class).setAction("com.ssyanhuo.arknightshelper.stopservice");
        notificationIntent.putExtra("action", "stopservice");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("notification", getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
            builder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent)
                    .setChannelId("notification");
        }else {
            builder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent);
        }

        Notification notification = builder.build();
        startForeground(1, notification);

        //启动悬浮窗

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        startFloatingButton();
    }

    public void startFloatingButton(){
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = DpUtiliy.dip2px(getApplicationContext(), 48);
        layoutParams.height = DpUtiliy.dip2px(getApplicationContext(), 48);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.x = 0;
        layoutParams.y = 200;
        button = new Button(this);
        button.setBackground(getResources().getDrawable(R.mipmap.overlay_button));
        button.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) motionEvent.getRawX();
                        y = (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) motionEvent.getRawX();
                        int nowY = (int) motionEvent.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        layoutParams.x = layoutParams.x + movedX;
                        layoutParams.y = layoutParams.y + movedY;

                        // 更新悬浮窗控件布局
                        windowManager.updateViewLayout(view, layoutParams);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFloatingWindow();
            }
        });
        windowManager.addView(button, layoutParams);
    }

    public void showFloatingWindow(){
        checkBoxes = new ArrayList<>();
        linearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.overlay_main, null);
        hrJson = JsonUtility.getJsonString(getApplicationContext(), "data/hr.json");
        windowManager.removeView(button);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        layoutParams.x = 0;
        layoutParams.y = 0;
        if(rotation == 1 || rotation == 3){
            layoutParams.height = displayMetrics.heightPixels;
            layoutParams.width = displayMetrics.widthPixels / 2;
        }else {
            layoutParams.height = displayMetrics.heightPixels / 3;
            layoutParams.width = displayMetrics.widthPixels;
        }
        windowManager.addView(linearLayout, layoutParams);
        linearLayout_hr = linearLayout.findViewById(R.id.hr_content);
        linearLayout_exp = linearLayout.findViewById(R.id.exp_content);
        linearLayout_material = linearLayout.findViewById(R.id.material_content);
        linearLayout_hr.setVisibility(View.VISIBLE);
        linearLayout_exp.setVisibility(View.GONE);
        linearLayout_material.setVisibility(View.GONE);
        TabLayout tabLayout = linearLayout.findViewById(R.id.tab_main);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case HR:
                        changeFloatingWindowContent(HR);
                        break;
                    case EXP:
                        changeFloatingWindowContent(EXP);
                        break;
                    case MATERIAL:
                        changeFloatingWindowContent(MATERIAL);
                        break;
                    case CLOSE:
                        hideFloatingWindow();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        Hr.hideResult((LinearLayout) linearLayout_hr.findViewById(R.id.hr_result_content));
        selectedStar = new ArrayList<>();
        selectedQualification = new ArrayList<>();
        selectedPosition = new ArrayList<>();
        selectedSex = new ArrayList<>();
        selectedType = new ArrayList<>();
        selectedTag = new ArrayList<>();
        Hr.getAllCheckboxes(checkBoxes, linearLayout_hr);
        for(int i = 0; i < checkBoxes.size(); i++){
            checkBoxes.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Hr.getSelectedItems(checkBoxes, selectedStar, selectedQualification, selectedPosition, selectedSex, selectedType, selectedTag);
                    if (!Hr.isFewerThan3(selectedQualification, selectedPosition, selectedSex, selectedType, selectedTag)){
                        compoundButton.setChecked(false);
                    }
                    ArrayList<JSONObject> result = Hr.getResult(hrJson, selectedStar, selectedQualification, selectedPosition, selectedSex, selectedType, selectedTag);
                    Hr.showResult(result, (LinearLayout)linearLayout_hr.findViewById(R.id.hr_result_content), getApplicationContext());
                    if(selectedStar.size() + selectedQualification.size() + selectedPosition.size() + selectedSex.size() + selectedType.size() + selectedTag.size() == 0){
                        Hr.hideResult((LinearLayout) linearLayout_hr.findViewById(R.id.hr_result_content));
                    }
                }
            });
        }
    }
    public void changeFloatingWindowContent(int i){
        switch (i){
            case HR:
                linearLayout_hr.setVisibility(View.VISIBLE);
                linearLayout_exp.setVisibility(View.GONE);
                linearLayout_material.setVisibility(View.GONE);
                break;
            case EXP:
                linearLayout_hr.setVisibility(View.GONE);
                linearLayout_exp.setVisibility(View.VISIBLE);
                linearLayout_material.setVisibility(View.GONE);
                break;
            case MATERIAL:
                linearLayout_hr.setVisibility(View.GONE);
                linearLayout_exp.setVisibility(View.GONE);
                linearLayout_material.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
    public void hideFloatingWindow(){
        windowManager.removeView(linearLayout);
        startFloatingButton();
    }
    //旧的方法，已经移动到单独的类里面
    public void getAllCheckboxes(View view){
        ViewGroup viewGroup = (ViewGroup)view;
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            if(viewGroup.getChildAt(i) instanceof CheckBox){
                checkBoxes.add((CheckBox) viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof HorizontalScrollView){
                getAllCheckboxes(viewGroup.getChildAt(i));
            }else if(viewGroup.getChildAt(i) instanceof LinearLayout){
                getAllCheckboxes(viewGroup.getChildAt(i));
            }
        }
    }
    public void getSelectedItems(){
        for(int i = 0; i < checkBoxes.size(); i++){
            selectedStar.clear();
            selectedQualification.clear();
            selectedPosition.clear();
            selectedSex.clear();
            selectedType.clear();
            selectedTag.clear();
            CheckBox checkBox = checkBoxes.get(i);
            if (!checkBox.isChecked()){continue;}
            View parentView = (View)checkBox.getParent();
            String item = String.valueOf(parentView.getTag());
            switch (item){
                case "star":
                    selectedStar.add(String.valueOf(checkBox.getTag()));
                    break;
                case "qualification":
                    selectedQualification.add(String.valueOf(checkBox.getTag()));
                    break;
                case "position":
                    selectedPosition.add(String.valueOf(checkBox.getTag()));
                    break;
                case "sex":
                    selectedSex.add(String.valueOf(checkBox.getTag()));
                    break;
                case "type":
                    selectedType.add(String.valueOf(checkBox.getTag()));
                    break;
                case "tag":
                    selectedTag.add(String.valueOf(checkBox.getTag()));
                    break;
                default:
                    break;
            }
            if (selectedStar.size() == 0){
                selectedStar.add("1");
                selectedStar.add("2");
                selectedStar.add("3");
                selectedStar.add("4");
                selectedStar.add("5");
                selectedStar.add("6");
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}