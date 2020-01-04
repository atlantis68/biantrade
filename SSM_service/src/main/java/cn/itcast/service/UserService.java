package cn.itcast.service;

import cn.itcast.pojo.User;

public interface UserService {
    public User findUser(String username,
                         String password);

    public User findUser(int id);

    public int updateUserById(User user);
}
