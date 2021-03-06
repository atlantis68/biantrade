package cn.itcast.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.itcast.pojo.Config;

public interface ConfigMapper {
	
    public Config findConfigByUid(Config config);
    
    public List<Config> findConfigsByUid(@Param("uid") Integer uid);
    
    public List<Config> findConfigFlag(Config config);
    
    public int updateConfig(Config config);

}
