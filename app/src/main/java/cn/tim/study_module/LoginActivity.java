package cn.tim.study_module;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import cn.tim.annotation.ARouter;

@ARouter(path = "/app/LoginActivity", group = "app")
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}