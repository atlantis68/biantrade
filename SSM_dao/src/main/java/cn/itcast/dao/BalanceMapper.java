package cn.itcast.dao;

import java.util.List;

import cn.itcast.pojo.Balance;
import cn.itcast.pojo.User;

public interface BalanceMapper {
	
    public int insertBalance(Balance balance);

    public List<User> findUserByStatus();
}
