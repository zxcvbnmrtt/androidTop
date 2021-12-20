package com.example.myapplication;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    //注册发现的广播
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //处理通知
            String strClsName = intent.getStringExtra("topActivity");
            Log.i(MyService.TAG,"Receiver One Broadcast! :" +strClsName);
        }
    };


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 注册Receiver
        IntentFilter intentFilter = new IntentFilter(MyService.RECEIVER_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver,intentFilter);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动服务
                //Intent intent = new Intent(getActivity(), MyService.class);
                // 启动服务
                //getActivity().startService(intent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getActivity().startForegroundService(new Intent(getActivity(), MyService.class));
                } else {
                    getActivity().startService(new Intent(getActivity(), MyService.class));
                }

            }
        });

        binding.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动服务
                Intent intent = new Intent(getActivity(), MyService.class);
                // 启动服务
                getActivity().stopService(intent);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    //判断当前界面显示的是哪个Activity
    public static String getTopActivity(Context context){
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        String pname = rti.get(0).topActivity.getPackageName();
        return pname;
    }
}