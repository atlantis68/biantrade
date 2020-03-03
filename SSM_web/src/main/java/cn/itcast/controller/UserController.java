package cn.itcast.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.itcast.pojo.User;
import cn.itcast.service.UserService;

@Controller
@RequestMapping("/User")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/index")
    public String index(Model model, HttpSession session) {
    	User user = (User) session.getAttribute("USER_SESSION");
    	model.addAttribute("role", user.getRole());
    	model.addAttribute("username", user.getUsername());
    	model.addAttribute("nickname", user.getNickname());
        return "user";
    }

    //用户登录
    @RequestMapping(value = "/login")
    public String login(String username, String password, Model model, HttpSession session) {
        User user = userService.findUser(username, password);
        if (user != null) {
        	session.setMaxInactiveInterval(-1);
            session.setAttribute("USER_SESSION", user);
            if(StringUtils.isNotEmpty(user.getApiKey()) && StringUtils.isNotEmpty(user.getSecretKey())) {
            	model.addAttribute("role", user.getRole());
            	model.addAttribute("id", user.getId());
            	model.addAttribute("username", user.getUsername());
            	model.addAttribute("nickname", user.getNickname());
            	if(StringUtils.isNotEmpty(user.getRelaids())) {
            		List<String> idList = Arrays.asList(user.getRelaids().split(","));
            		List<User> users = userService.findUserByIds(idList);
            		if(users != null && users.size() > 0) {
            			JSONArray userInfos = new JSONArray();
            			for(User u : users) {
            				Map<String, String> temp = new HashMap<String, String>();
            				temp.put("id", "" + u.getId());
            				temp.put("username", u.getUsername());
            				temp.put("nickname", u.getNickname());
            				userInfos.add(temp);
            			}
            			model.addAttribute("ids", userInfos.toString());            		
            		}
            	}
            	return "account";            	
            } else {
            	return "user";
            }
        }
        model.addAttribute("msg", "用户或密码错误,请重新输入");
        return "index";
    }
    
    @RequestMapping(value = "/findUser")
    @ResponseBody
    public String findUser(HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute("USER_SESSION");
    		User userInfo = userService.findUser(user.getId());    	
        	result.put("status", "ok");
        	result.put("msg", JSON.toJSONString(userInfo));
    	} catch(Exception e) {
    		e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
    	}
    	return result.toJSONString();
    }

    //退出登录
    @RequestMapping(value = "/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "index";
    }
    
    @RequestMapping(value = "/update")
    @ResponseBody
    public String update(Integer id, String password, String apiKey, String secretKey, String mail, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
        	User user = new User();
        	user.setId(id);
        	user.setPassword(password);
        	user.setApiKey(apiKey);
        	user.setSecretKey(secretKey);
        	user.setMail(mail);
        	int number = userService.updateUserById(user);
			if(number > 0) {
				result.put("status", "ok");
				result.put("msg", "save successful");
			} else {
				result.put("status", "error");
				result.put("msg", "save failed");
			}
        	session.invalidate();
    	} catch(Exception e) {
    		e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
    	}
        return result.toJSONString();
    }
    
    @RequestMapping(value = "/findUserByUid", produces="text/html;charset=UTF-8")
    @ResponseBody
    public String findUserByUid(Integer uid, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		List<User> userInfos = userService.findUserByUid(uid);    	
        	result.put("status", "ok");
        	result.put("msg", JSON.toJSONString(userInfos));
    	} catch(Exception e) {
    		e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
    	}
    	return result.toJSONString();
    }
    
    @RequestMapping(value = "/changeUser", produces="text/html;charset=UTF-8")
    @ResponseBody
    public String changeUser(String id, String relaid, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		User my = userService.checkPermission(id, "," + relaid + ",");
    		if(my != null) {
    			User relaUser = userService.findRelaUser(Integer.parseInt(relaid));
    			if(relaUser != null) {
    				session.setMaxInactiveInterval(-1);
    	            session.setAttribute("USER_SESSION", relaUser);
    				Map<String, String> temp = new HashMap<String, String>();
    				temp.put("id", "" + relaUser.getId());
    				temp.put("role", relaUser.getRole());
    				temp.put("username", relaUser.getUsername());
    				temp.put("nickname", relaUser.getNickname());
    	        	result.put("status", "ok");
    	        	result.put("msg", JSON.toJSONString(temp));
    	        	return result.toJSONString();
    			}
    		}
        	result.put("status", "error");
        	result.put("msg", "切换用户失败");
    	} catch(Exception e) {
    		e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
    	}
    	return result.toJSONString();
    }
}
