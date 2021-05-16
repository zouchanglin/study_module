package cn.tim.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import cn.tim.annotation.ARouter;

@ARouter(path = "/personal/PersonalMainActivity", group = "personal")
public class PersonalMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_main_activity);
    }
}