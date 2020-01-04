package cn.itcast.dao;

import org.apache.ibatis.annotations.Param;

import cn.itcast.pojo.User;

public interface UserMapper {
    public User findUser(@Param("username") String username,
                         @Param("password") String password);
    
    public User findUserById(@Param("id") int id);

    public int updateUserById(User user);
}
