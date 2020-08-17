package cn.itcast.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.itcast.back.CancelPlanTask;
import cn.itcast.back.FollowPlanTask;
import cn.itcast.back.FollowStrategyStopTask;
import cn.itcast.back.FollowStrategyTask;
import cn.itcast.back.ThreadPool;
import cn.itcast.constant.TransactionConstants;
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
    public String index(Model model, HttpSession session) {
    	User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    	model.addAttribute(TransactionConstants.USER_ROLE, user.getRole());
    	model.addAttribute(TransactionConstants.USER_USERNAME, user.getUsername());
    	model.addAttribute(TransactionConstants.USER_NICKNAME, user.getNickname());
        return "account";
    }
    
    @RequestMapping(value = "/plan")
    @ResponseBody
    public String plan(String symbol, String first, String second, String third, String stop, Integer level,
    		String trigger, Integer compare, String trigger1, Integer compare1, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		Float curPrice = ToolsUtils.getCurPriceByKey(symbol);
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		String plan = orderService.plan(symbol, first, second, third, stop, trigger, compare, trigger1, compare1,
    				user.getId(), user.getApiKey(), user.getSecretKey(), curPrice, level);
        	result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
        	result.put(TransactionConstants.SYSTEM_MSG, plan);
        	if(user.getRole().indexOf("0") > -1) {
        		JSONObject jSONObject = JSON.parseObject(plan);
        		if(jSONObject.containsKey(TransactionConstants.USER_ID)) {
        			int id = jSONObject.getIntValue(TransactionConstants.USER_ID);
        			//进入关联跟单
        			if(id > 0) {
        				Config config = new Config();
        				config.setType(symbol);
        				config.setLossWorkingType("" + level);;
        				List<Config> allConfig = configService.findConfigFlag(config);
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
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
    }
    
    @RequestMapping(value = "/save")
    @ResponseBody
    public String save(String symbol, String first, String second, String third, String stop, Integer level,
    		String trigger, Integer compare, String trigger1, Integer compare1, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
			Plan plan = new Plan();
			plan.setUid(user.getId());
			plan.setPid(0);
			plan.setSymbol(symbol);
			plan.setFirst(Float.parseFloat(first));
			plan.setSecond(Float.parseFloat(second));
			plan.setThird(Float.parseFloat(third));
			plan.setStop(Float.parseFloat(stop));
			plan.setTrigger(StringUtils.isNotEmpty(trigger) ? Float.parseFloat(trigger) : 0f);
			plan.setCompare(StringUtils.isNotEmpty(trigger) ? compare : 0);
			plan.setTrigger1(StringUtils.isNotEmpty(trigger1) ? Float.parseFloat(trigger1) : 0f);
			plan.setCompare1(StringUtils.isNotEmpty(trigger1) ? compare1 : 1);
			plan.setState(5);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(0);
			if(level != null) {
				plan.setLevel(level);
			} else {
				plan.setLevel(1);
			}
			plan.setOrderIds("");
			if(orderService.insertPlan(plan) > 0) {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);				
			} else {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);		
				result.put(TransactionConstants.SYSTEM_MSG, "保存失败");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
    }


    @RequestMapping(value = "/findAllPlans")
    @ResponseBody
    public String findAllPlans(HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		List<Plan> plans = orderService.findPlanByUid(user.getId());
        	result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
        	result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(plans));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }

    @RequestMapping(value = "/findCachePlans")
    @ResponseBody
    public String findCachePlans(HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		List<Plan> plans = orderService.findCachePlanByUid(user.getId());
        	result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
        	result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(plans));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/historyOrders")
    @ResponseBody
    public String historyOrders(Integer startTime, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
			int level = 1;
			String[] roles = user.getRole().split(",");
			for(String role : roles) {
    			if(StringUtils.isNotEmpty(role)) {
    				int atomic = Integer.parseInt(role);
    				if(atomic < 6 && atomic > level) {
    					level = atomic;
    				}
    				
    			}
			}
			List<Plan> plans = orderService.findPlanByTime(startTime, level);  
        	result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
        	result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(plans));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/fllowPlans", produces="text/html;charset=UTF-8")
    @ResponseBody
    public String fllowPlans(String symbol, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		int level = 1;
    		String[] roles = user.getRole().split(",");
    		for(String role : roles) {
    			if(StringUtils.isNotEmpty(role)) {
    				int atomic = Integer.parseInt(role);
    				if(atomic < 6 && atomic > level) {
    					level = atomic;
    				}
    				
    			}
    		}
    		List<Plan> plans = orderService.findFllowPlans(level);
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
    		result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(plans));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/cancelPlan")
    @ResponseBody
    public String cancelPlan(String symbol, String id, String state, String orderIds, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		int number = orderService.cancelPlan(user.getId(), symbol, id, orderIds, Integer.parseInt(state), user.getApiKey(), user.getSecretKey());
			if(number > 0) {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
				result.put(TransactionConstants.SYSTEM_MSG, "canceled successful");
			} else {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
				result.put(TransactionConstants.SYSTEM_MSG, "canceled failed");
			}
			if(user.getRole().indexOf("0") > -1) {
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
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/follow")
    @ResponseBody
    public String follow(Integer id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		Plan plan = orderService.findPlanById(id);
    		if(plan != null && plan.getState() < 4) {
    			orderService.follow(id, plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
    					plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), 
    					plan.getTrigger1().toString(), plan.getCompare1(), user.getId(), user.getApiKey(), user.getSecretKey(), 
    					ToolsUtils.getCurPriceByKey(plan.getSymbol()));
    			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
    			result.put(TransactionConstants.SYSTEM_MSG, "follow successful");    			
    		} else {
    			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    			result.put(TransactionConstants.SYSTEM_MSG, "follow failed");   
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/repeat")
    @ResponseBody
    public String repeat(Integer id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		Plan plan = orderService.findPlanById(id);
    		if(plan != null) {
    			Integer level = null;
    			if(user.getRole().indexOf("0") > -1) {
    				level = plan.getLevel();
    			}
				String temp = plan(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
						plan.getThird().toString(), plan.getStop().toString(), level, plan.getTrigger().toString(), 
						plan.getCompare(), plan.getTrigger1().toString(), plan.getCompare1(), session);
				result = JSONObject.parseObject(temp); 
    		} else {
    			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    			result.put(TransactionConstants.SYSTEM_MSG, "repeat failed");   
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/warn", produces="text/html;charset=UTF-8")
    @ResponseBody
    public String warn(String id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
			if(user.getRole().indexOf("0") > -1) {
				//通知
				int number = orderService.warn(id);	
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
				result.put(TransactionConstants.SYSTEM_MSG, "notice " + number + " users");
			} else {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
				result.put(TransactionConstants.SYSTEM_MSG, "只有带单人才能操作");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/predict")
    @ResponseBody
    public String predict(Integer id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		Plan plan = orderService.findPlanById(id);
    		if(plan != null) {
    			Result temp = orderService.generateAndDealOrder(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
    					plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), 
    					plan.getTrigger1().toString(), plan.getCompare1(), user.getId(), user.getApiKey(), user.getSecretKey(), 
    					null, ToolsUtils.getCurPriceByKey(plan.getSymbol()), true);
    			if(temp.getState() == -1) {
        			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
        			result.put(TransactionConstants.SYSTEM_MSG, temp.getMsg());  
    			} else {
        			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
        			result.put(TransactionConstants.SYSTEM_MSG, "show detail failed");  	
    			}
    		} else {
    			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    			result.put(TransactionConstants.SYSTEM_MSG, "show detail failed");   
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/submit")
    @ResponseBody
    public String submit(Integer id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		//获取配置项
    		Plan plan = orderService.findPlanById(id);
    		if(plan != null) {
    			return plan(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), plan.getThird().toString(), plan.getStop().toString(), 
    					plan.getLevel(), plan.getTrigger().toString(), plan.getCompare(), plan.getTrigger1().toString(), plan.getCompare1(), session);
    		} else {
    			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    			result.put(TransactionConstants.SYSTEM_MSG, "submit plan failed");   
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
		
    }
    
    @RequestMapping(value = "/strategyMarket")
    @ResponseBody
    public String strategyMarket(String symbol, String side, Integer level, HttpSession session) {
    	JSONObject result = new JSONObject();
    	String strategy = null;
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		strategy = orderService.strategyMarket(symbol, side, user.getId(), user.getApiKey(), user.getSecretKey(), level, null);
        	if(user.getRole().indexOf("0") > -1) {
        		JSONObject jSONObject = JSON.parseObject(strategy);
        		if(jSONObject.containsKey(TransactionConstants.USER_ID)) {
        			int id = jSONObject.getIntValue(TransactionConstants.USER_ID);
        			//进入关联跟单
        			if(id > 0) {
        				Config config = new Config();
        				config.setType(symbol);
        				config.setLossWorkingType("" + level);;
        				List<Config> allConfig = configService.findConfigFlag(config);
        				for(Config c : allConfig) {
        					ThreadPool.execute(new FollowStrategyTask(orderService, symbol, side, c.getUid(), c.getType(), c.getLossWorkingType(), id));
        				}
        			}
        		}
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return StringUtils.isNotEmpty(strategy) ? strategy : result.toJSONString();
    }
    
    @RequestMapping(value = "/strategyStop")
    @ResponseBody
    public String strategyStop(String symbol, String side, String stopPrice, String id, HttpSession session) {
    	JSONObject result = new JSONObject();
    	String strategy = null;
    	try {
    		//获取配置项
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		strategy = orderService.strategyStop(symbol, side, stopPrice, user.getId(), id, user.getApiKey(), user.getSecretKey());
			if(user.getRole().indexOf("0") > -1) {
				//进入关联操作
				List<Plan> plans = orderService.findPlansById(Integer.parseInt(id));
				for(Plan plan : plans) {
					ThreadPool.execute(new FollowStrategyStopTask(orderService, plan.getSymbol(), side, stopPrice, 
							plan.getUid(), plan.getCreateTime(), plan.getUpdateTime(), "" + plan.getId()));
				}				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return StringUtils.isNotEmpty(strategy) ? strategy : result.toJSONString();
    }
}
