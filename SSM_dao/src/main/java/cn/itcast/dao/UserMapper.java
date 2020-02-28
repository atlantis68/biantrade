package cn.itcast.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.itcast.pojo.Plan;
import cn.itcast.pojo.User;

public interface UserMapper {
    public User findUser(@Param("username") String username,
                         @Param("password") String password);
    
    public User findUserById(@Param("id") int id);

    public int updateUserById(User user);
    
    public List<User> findUserByUid(@Param("uid") int uid);
    
    public List<User> findUserByIds(List<String> ids);
    
    public User checkPermission(@Param("id") String id, @Param("relaid") String relaid);
    
    public User findRelaUser(@Param("id") int id);
}
