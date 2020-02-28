package cn.itcast.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.itcast.dao.UserMapper;
import cn.itcast.pojo.User;
import cn.itcast.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUser(String username, String password) {
        return this.userMapper.findUser(username, password);
    }
    
    public User findUser(int id) {
        return this.userMapper.findUserById(id);
    }
    
    public int updateUserById(User user) {
    	return userMapper.updateUserById(user);
    }
    
    public List<User> findUserByUid(int uid) {
    	return userMapper.findUserByUid(uid);
    }
    
    public List<User> findUserByIds(List<String> ids) {
    	return userMapper.findUserByIds(ids);
    }

	@Override
	public User checkPermission(String id, String relaid) {
		// TODO Auto-generated method stub
		return userMapper.checkPermission(id, relaid);
	}

	@Override
	public User findRelaUser(int id) {
		// TODO Auto-generated method stub
		return userMapper.findRelaUser(id);
	}
}
