package com.ubx.rfid_demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ubx.rfid_demo.databinding.ActivityMainBinding;
import com.ubx.rfid_demo.databinding.ActivityQtBinding;
import com.ubx.rfid_demo.ui.main.Activity6BTag;
import com.ubx.rfid_demo.ui.main.SectionsPagerAdapter;
import com.ubx.rfid_demo.ui.main.SettingFragment;
import com.ubx.rfid_demo.ui.main.TagManageFragment;
import com.ubx.rfid_demo.ui.main.TagScanFragment;
import com.ubx.rfid_demo.utils.SoundTool;
import com.ubx.rfid_demo.utils.UHFUtils;
import com.ubx.usdk.USDKManager;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.rfid.aidl.RfidDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QTActivity extends AppCompatActivity {
    public static final String TAG = "usdk";
    private ActivityQtBinding binding;

    public  boolean RFID_INIT_STATUS = false;
    public List<String> mDataParents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityQtBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mDataParents = new ArrayList<>();

        SoundTool.getInstance(BaseApplication.getContext());
        UHFUtils.C72_leer_todo(this);


        binding.stopInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UHFUtils.stopInventory();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundTool.getInstance(BaseApplication.getContext()).release();
        RFID_INIT_STATUS = false;
    }




    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == 523 &&  event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0){
            //TODO prensa



            return true;
        }else if (event.getKeyCode() == 523 &&  event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0){
            //TODO elevar



            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}