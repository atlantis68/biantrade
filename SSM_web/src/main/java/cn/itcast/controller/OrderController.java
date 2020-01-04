package cn.itcast.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.itcast.back.CancelPlanTask;
import cn.itcast.back.FollowPlanTask;
import cn.itcast.back.ThreadPool;
import cn.itcast.pojo.Config;
import cn.itcast.pojo.Plan;
import cn.itcast.pojo.User;
import cn.itcast.service.ConfigService;
import cn.itcast.service.OrderService;

@Controller
@RequestMapping("/Order")
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ConfigService configService;
    
    @RequestMapping(value = "/index")
    public String logout(HttpSession session) {
        return "account";
    }
    
    @RequestMapping(value = "/plan")
    @ResponseBody
    public String plan(String symbol, String first, String second, String third, String stop, 
    		String trigger, Integer compare, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		String plan = orderService.plan(symbol, first, second, third, stop, trigger, compare,
    				user.getId(), user.getApiKey(), user.getSecretKey());
        	result.put("status", "ok");
        	result.put("msg", plan);
        	if(user.getRole() == 0) {
        		JSONObject jSONObject = JSON.parseObject(plan);
        		if(jSONObject.containsKey("id")) {
        			int id = jSONObject.getIntValue("id");
        			//进入关联跟单
        			if(id > 0) {
        				Config config = new Config();
        				config.setType(symbol);
        				List<Config> allConfig = configService.findConfigFlag12(config);
        				for(Config c : allConfig) {
        					ThreadPool.execute(new FollowPlanTask(orderService, id, symbol, first, second, third, stop, 
        							trigger, compare, c.getUid(), c.getType(), c.getLossWorkingType()));
        				}
        			}
        		}
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
    }


    @RequestMapping(value = "/findAllPlans")
    @ResponseBody
    public String findAllPlans(String symbol, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		List<Plan> plans = orderService.findPlanByUid(user.getId(), symbol);
        	result.put("status", "ok");
        	result.put("msg", JSON.toJSONString(plans));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/fllowPlans")
    @ResponseBody
    public String fllowPlans(String symbol, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute("USER_SESSION");
    		if(user.getRole() > 0) {
        		List<Plan> plans = orderService.findFllowPlans(symbol);
        		result.put("status", "ok");
        		result.put("msg", JSON.toJSONString(plans));
    		} else {
    			result.put("status", "error");
    			result.put("msg", "you are dashen!!!");
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/cancelPlan")
    @ResponseBody
    public String cancelPlan(String symbol, String id, String orderIds, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		int number = orderService.cancelPlan(user.getId(), symbol, id, orderIds, user.getApiKey(), user.getSecretKey());
			if(number > 0) {
				result.put("status", "ok");
				result.put("msg", "canceled successful");
			} else {
				result.put("status", "error");
				result.put("msg", "canceled failed");
			}
			if(user.getRole() == 0) {
				//进入关联撤单
				List<Plan> plans = orderService.findPlansById(Integer.parseInt(id));
				for(Plan plan : plans) {
					ThreadPool.execute(new CancelPlanTask(orderService, plan.getUid(), plan.getId(), plan.getSymbol(), plan.getOrderIds(), plan.getCreateTime(), plan.getUpdateTime()));
				}				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/follow")
    @ResponseBody
    public String follow(Integer id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		Plan plan = orderService.findPlanById(id);
    		if(plan != null && plan.getState() < 4) {
    			orderService.follow(id, plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
    					plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), 
    					plan.getCompare(), user.getId(), user.getApiKey(), user.getSecretKey());
    			result.put("status", "ok");
    			result.put("msg", "follow successful");    			
    		} else {
    			result.put("status", "error");
    			result.put("msg", "follow failed");   
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/finish")
    @ResponseBody
    public String finish(String id) {
    	JSONObject result = new JSONObject();
    	try {
    		int numer = orderService.updatePlanById(Integer.parseInt(id), 5);
			if(numer > 0) {
				result.put("status", "ok");
				result.put("msg", "finish successful");
			} else {
				result.put("status", "error");
				result.put("msg", "finish failed");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
		
    }
}
