package cn.itcast.service;

import java.util.List;

import cn.itcast.pojo.Config;

public interface ConfigService {
    public Config findConfigByUid(Config config);
    
    public List<Config> findConfigsByUid(Integer uid);
    
    public List<Config> findConfigFlag(Config config);

    public int updateConfig(Config config);

}
