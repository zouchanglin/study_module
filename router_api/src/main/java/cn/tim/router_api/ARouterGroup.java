package cn.tim.router_api;

import java.util.Map;

public interface ARouterGroup {
    /**
     * path -> RouterBean
     * group -> ModuleName
     */
    Map<String, Class<? extends ARouterPath>> getGroupMap();
}
