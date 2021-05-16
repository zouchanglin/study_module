package cn.tim.study_module;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import cn.tim.annotation.ARouter;
import cn.tim.annotation.Parameter;
import cn.tim.router_api.ParameterManager;
import cn.tim.router_api.RouterManager;


@ARouter(path = "/app/MainActivity", group = "app")
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Parameter
    String name;

    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(BuildConfig.isRelease){
            Log.i(TAG, "onCreate: 集成化环境");
        }else{
            Log.i(TAG, "onCreate: 组件化环境");
        }

        ParameterManager.getInstance().loadParameter(this);
    }

    public void toOrderModule(View view) {
        RouterManager.getInstance()
                .build("/order/OrderMainActivity")
                .withString("name", "张三")
                .withInt("age", 20)
                .navigation(this);
    }

    public void toPersonalModule(View view) {
    }
}