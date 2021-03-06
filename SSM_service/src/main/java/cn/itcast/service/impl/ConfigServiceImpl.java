package cn.itcast.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.itcast.dao.ConfigMapper;
import cn.itcast.pojo.Config;
import cn.itcast.service.ConfigService;

@Service
@Transactional
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private ConfigMapper configMapper;

	@Override
	public Config findConfigByUid(Config config) {
		// TODO Auto-generated method stub
		return configMapper.findConfigByUid(config);
	}
	
	@Override
	public List<Config> findConfigsByUid(Integer uid) {
		// TODO Auto-generated method stub
		return configMapper.findConfigsByUid(uid);
	}

	@Override
	public int updateConfig(Config config) {
		// TODO Auto-generated method stub
		return configMapper.updateConfig(config);
	}

	@Override
	public List<Config> findConfigFlag(Config config) {
		// TODO Auto-generated method stub
		return configMapper.findConfigFlag(config);
	}
}
