package cn.tim.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import cn.tim.annotation.ARouter;
import cn.tim.annotation.Parameter;
import cn.tim.router_api.ParameterManager;


@ARouter(path = "/order/OrderMainActivity", group = "order")
public class OrderMainActivity extends AppCompatActivity {
    private static final String TAG = "OrderMainActivity";
    @Parameter
    String name;

    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oder_main_activity);

        ParameterManager.getInstance().loadParameter(this);
        Log.i(TAG, "onCreate: name = " + name);
        Log.i(TAG, "onCreate: age = " + age);
    }
}