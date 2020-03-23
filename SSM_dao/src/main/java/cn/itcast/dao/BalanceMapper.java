package cn.itcast.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.itcast.pojo.Balance;
import cn.itcast.pojo.User;

public interface BalanceMapper {
	
    public int insertBalance(Balance balance);

    public List<Balance> findBalanceByUid(@Param("uid") Integer uid);
    
    public List<User> findUserByStatus();
}
