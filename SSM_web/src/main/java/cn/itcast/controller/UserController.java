package cn.itcast.controller;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.itcast.pojo.User;
import cn.itcast.service.UserService;

@Controller
@RequestMapping("/User")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/index")
    public String index() {
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
}
