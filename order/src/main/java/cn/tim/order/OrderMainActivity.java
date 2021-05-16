package cn.tim.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import cn.tim.annotation.ARouter;


@ARouter(path = "/order/OrderMainActivity", group = "order")
public class OrderMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oder_main_activity);
    }
}