package cn.tim.router_api;

import java.util.Map;

import cn.tim.annotation.bean.RouterBean;

public interface ARouterPath {

    /**
     * key：path value RouterBean
     */
    Map<String, RouterBean> getPathMap();
}