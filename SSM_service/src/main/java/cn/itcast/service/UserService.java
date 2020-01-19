package cn.itcast.service;

import java.util.List;

import cn.itcast.pojo.User;

public interface UserService {
    public User findUser(String username,
                         String password);

    public User findUser(int id);

    public int updateUserById(User user);
    
    public List<User> findUserByUid(int uid);
}
