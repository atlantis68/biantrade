package cn.itcast.dao;

import java.util.List;

import cn.itcast.pojo.Config;

public interface ConfigMapper {
	
    public Config findConfigByUid(Config config);
    
    public List<Config> findConfigFlag2(Config config);
    
    public List<Config> findConfigFlag12(Config config);
    
    public int updateConfig(Config config);

}
