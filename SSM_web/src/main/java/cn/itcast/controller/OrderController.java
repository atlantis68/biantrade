package cn.itcast.controller;

import java.text.SimpleDateFormat;
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
import cn.itcast.model.Result;
import cn.itcast.pojo.Config;
import cn.itcast.pojo.Plan;
import cn.itcast.pojo.User;
import cn.itcast.service.ConfigService;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;

@Controller
@RequestMapping("/Order")
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ConfigService configService;
    
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @RequestMapping(value = "/index")
    public String logout(HttpSession session) {
        return "account";
    }
    
    @RequestMapping(value = "/plan")
    @ResponseBody
    public String plan(String symbol, String first, String second, String third, String stop, 
    		String trigger, Integer compare, String trigger1, Integer compare1, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		Float curPrice = ToolsUtils.getCurPriceByKey(symbol);
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		String plan = orderService.plan(symbol, first, second, third, stop, trigger, compare, trigger1, compare1,
    				user.getId(), user.getApiKey(), user.getSecretKey(), curPrice);
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
        							trigger, compare, trigger1, compare1, c.getUid(), c.getType(), c.getLossWorkingType(), curPrice));
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
    public String findAllPlans(HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		List<Plan> plans = orderService.findPlanByUid(user.getId());
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
    
    @RequestMapping(value = "/historyOrders")
    @ResponseBody
    public String historyOrders(Integer startTime, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		List<Plan> plans = orderService.findPlanByTime(startTime);
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
    
    @RequestMapping(value = "/fllowPlans", produces="text/html;charset=UTF-8")
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
    			result.put("msg", "只有跟单人才能操作");
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
    public String cancelPlan(String symbol, String id, String state, String orderIds, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		int number = orderService.cancelPlan(user.getId(), symbol, id, orderIds, Integer.parseInt(state), user.getApiKey(), user.getSecretKey());
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
					ThreadPool.execute(new CancelPlanTask(orderService, plan.getUid(), plan.getId(), plan.getSymbol(), 
							plan.getOrderIds(), Integer.parseInt(state), plan.getCreateTime(), plan.getUpdateTime()));
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
    					plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), 
    					plan.getTrigger1().toString(), plan.getCompare1(), user.getId(), user.getApiKey(), user.getSecretKey(), 
    					ToolsUtils.getCurPriceByKey(plan.getSymbol()));
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
    
    @RequestMapping(value = "/repeat")
    @ResponseBody
    public String repeat(Integer id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		Plan plan = orderService.findPlanById(id);
    		if(plan != null) {
				String temp = plan(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
    					plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), 
    					plan.getTrigger1().toString(), plan.getCompare1(), session);
				result = JSONObject.parseObject(temp); 
    		} else {
    			result.put("status", "error");
    			result.put("msg", "repeat failed");   
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/warn", produces="text/html;charset=UTF-8")
    @ResponseBody
    public String warn(String id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
			if(user.getRole() == 0) {
				//通知
				int number = orderService.warn(id);	
				result.put("status", "ok");
				result.put("msg", "notice " + number + " users");
			} else {
				result.put("status", "error");
				result.put("msg", "只有带单人才能操作");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/predict")
    @ResponseBody
    public String predict(Integer id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute("USER_SESSION");
    		Plan plan = orderService.findPlanById(id);
    		if(plan != null) {
    			Result temp = orderService.generateAndDealOrder(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
    					plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), 
    					plan.getTrigger1().toString(), plan.getCompare1(), user.getId(), user.getApiKey(), user.getSecretKey(), 
    					null, ToolsUtils.getCurPriceByKey(plan.getSymbol()), true);
    			if(temp.getState() == -1) {
        			result.put("status", "ok");
        			result.put("msg", temp.getMsg());  
    			} else {
        			result.put("status", "error");
        			result.put("msg", "show detail failed");  	
    			}
    		} else {
    			result.put("status", "error");
    			result.put("msg", "show detail failed");   
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
