package cn.itcast.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.itcast.pojo.Plan;
import cn.itcast.pojo.User;

public interface UserService {
    public User findUser(String username,
                         String password);

    public User findUser(int id);

    public int updateUserById(User user);
    
    public List<User> findUserByUid(int uid);
    
    public List<User> findUserByIds(List<String> ids);
    
    public User checkPermission(String id, String relaid);
    
    public User findRelaUser(int id);
}
