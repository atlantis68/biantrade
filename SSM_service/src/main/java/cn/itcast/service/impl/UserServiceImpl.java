package cn.itcast.service.impl;

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
}
