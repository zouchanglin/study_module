package cn.tim.router;

import android.app.Activity;

import java.util.Map;

public interface IRouterLoad {
    void loadInfo(Map<String, Class<? extends Activity>> routers);
}
