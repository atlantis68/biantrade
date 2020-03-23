package cn.itcast.dao;

import java.util.List;

import cn.itcast.pojo.Monitor;

public interface MonitorMapper {
	
    public List<Monitor> findMoniteInfo();
}
