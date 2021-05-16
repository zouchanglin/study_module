package cn.tim.study_module;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import cn.tim.annotation.ARouter;


@ARouter(path = "/app/MainActivity", group = "app")
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(BuildConfig.isRelease){
            Log.i(TAG, "onCreate: 集成化环境");
        }else{
            Log.i(TAG, "onCreate: 组件化环境");
        }
    }
}