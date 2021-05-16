package cn.tim.myjavapoet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import cn.tim.annotation.ARouter;

@ARouter(path = "/app/TwoMainActivity")
public class TwoMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_main);
    }
}