package cn.itcast.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import cn.itcast.back.ThreadPool;
import cn.itcast.back.TradeMarketTask;
import cn.itcast.client.HttpClient;
import cn.itcast.client.SHA256;
import cn.itcast.pojo.Config;
import cn.itcast.pojo.Mail;
import cn.itcast.pojo.User;
import cn.itcast.service.ConfigService;
import cn.itcast.service.OrderService;
import cn.itcast.service.UserService;
import cn.itcast.utils.ToolsUtils;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

@Controller
@RequestMapping("/Account")
public class AccountController {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
	
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    @RequestMapping(value = "/index")
    public String index(Model model, HttpSession session) {

    	User user = (User) session.getAttribute("USER_SESSION");
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
    }
	
    @RequestMapping("/tradeMarket")
    @ResponseBody
    public String tradeMarket(String symbol, String side, String quantity, HttpSession session) {
    	JSONObject result = new JSONObject();
    	String realQuantity;
    	try {
    		User user = (User) session.getAttribute("USER_SESSION");
    		if(StringUtils.isEmpty(quantity)) {
    			Config config = new Config();
    			config.setUid(user.getId());
    			config.setType(symbol);
    			Config allConfig = configService.findConfigByUid(config);
//    			realQuantity = ToolsUtils.formatQuantity(symbol, allConfig.getMarketAmount() * leverage / price);
    			realQuantity = allConfig.getMarketAmount().toString();
    		} else {
    			float positionAmt = 0;
        		String risks = orderService.positionRisk(user.getApiKey(), user.getSecretKey());
        		List<String> lists = JSON.parseArray(risks, String.class);
        		for(String list : lists) {
        			Map<String, String> risk = JSON.parseObject(list, new TypeReference<Map<String, String>>(){} );
        			if(risk != null && StringUtils.isNotEmpty(risk.get("positionAmt")) 
        					&& StringUtils.isNotEmpty(risk.get("symbol")) && risk.get("symbol").equals(symbol)) {
        				positionAmt = Math.abs(Float.parseFloat(risk.get("positionAmt")));
        				break;
        			}       			
        		}
    			realQuantity = "" + (positionAmt * (Float.parseFloat(quantity) / 100));
    		}
    		String temp = orderService.trade(symbol, side, ToolsUtils.formatQuantity(symbol, Float.parseFloat(realQuantity)), 
    				null, null, "MARKET", null, null, "true", user.getApiKey(), user.getSecretKey());
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
				result.put("status", "ok");
				Mail mail = new Mail();
				mail.setUid(user.getId());
				mail.setSymbol(symbol);
				mail.setSubject(symbol + "即时单创建成功，成交价格" + ToolsUtils.getCurPriceByKey(symbol) + "，已提交到币安");
				mail.setContent("提交数量：" + realQuantity);
				mail.setState(0);
				mail.setCreateTime(format.format(new Date()));
				mail.setUpdateTime(format.format(new Date()));
				orderService.insertMail(mail);
			} else {
				result.put("status", "error");
			}
			result.put("msg", JSON.toJSONString(temp));
        	if(user.getRole().indexOf("0") > -1) {
				Config config = new Config();
				config.setType(symbol);
				config.setLossWorkingType("6");;
				List<Config> allConfig = configService.findConfigFlag(config);
				for(Config c : allConfig) {
					ThreadPool.execute(new TradeMarketTask(orderService, c.getUid(), symbol, side, quantity, null, 
							null, "MARKET", null, null, "true", c.getType(), c.getLossWorkingType()));
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
    
    @RequestMapping("/profitOrLoss")
    @ResponseBody
    public String profitOrLoss(String symbol, String side, String quantity, String rate, 
    		String type, Float stopPrice, HttpSession session) {
    	JSONObject result = new JSONObject();
    	String realQuantity;
    	try {
    		User user = (User) session.getAttribute("USER_SESSION");
    		if(stopPrice != null) {
    			float positionAmt = 0;
        		String risks = orderService.positionRisk(user.getApiKey(), user.getSecretKey());
        		List<String> lists = JSON.parseArray(risks, String.class);
        		for(String list : lists) {
        			Map<String, String> risk = JSON.parseObject(list, new TypeReference<Map<String, String>>(){} );
        			if(risk != null && StringUtils.isNotEmpty(risk.get("positionAmt")) 
        					&& StringUtils.isNotEmpty(risk.get("symbol")) && risk.get("symbol").equals(symbol)) {
        				positionAmt = Math.abs(Float.parseFloat(risk.get("positionAmt")));
        				break;
        			}       			
        		}
    			realQuantity = "" + (positionAmt * (Float.parseFloat(quantity) / 100));
    			String temp = orderService.trade(symbol, side, ToolsUtils.formatQuantity(symbol, Float.parseFloat(realQuantity)), 
    					null, ToolsUtils.formatPrice(symbol, stopPrice), type, null, "CONTRACT_PRICE", "true", user.getApiKey(), user.getSecretKey());
    			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
    			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
    				result.put("status", "ok");
    				Mail mail = new Mail();
    				mail.setUid(user.getId());
    				mail.setSymbol(symbol);
    				mail.setSubject(symbol + "止盈/止损单创建成功，挂单价格" + stopPrice + "，已提交到币安");
    				mail.setContent("提交数量：" + realQuantity);
    				mail.setState(0);
    				mail.setCreateTime(format.format(new Date()));
    				mail.setUpdateTime(format.format(new Date()));
    				orderService.insertMail(mail);
    			} else {
    				result.put("status", "error");
    			}
    			result.put("msg", JSON.toJSONString(temp));
            	if(user.getRole().indexOf("0") > -1) {
    				Config config = new Config();
    				config.setType(symbol);
    				config.setLossWorkingType("7");
    				List<Config> allConfig = configService.findConfigFlag(config);
    				for(Config c : allConfig) {
    					ThreadPool.execute(new TradeMarketTask(orderService, c.getUid(), symbol, side, quantity, null, 
    							ToolsUtils.formatPrice(symbol, stopPrice), type, null, "CONTRACT_PRICE", "true", c.getType(), c.getLossWorkingType()));
    				}
            	}
    		} else {
        		result.put("status", "error");
        		result.put("msg", "parameter error");
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
    }
    
    @RequestMapping("/trade")
    @ResponseBody
    public String trade(String symbol, String side, String quantity, String price, String stopPrice, String type, 
    		String timeInForce, String workingType, String reduceOnly, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute("USER_SESSION");
    		String temp = orderService.trade(symbol, side, quantity, price, stopPrice, type, timeInForce, workingType, reduceOnly, user.getApiKey(), user.getSecretKey());
        	result.put("status", "ok");
        	result.put("msg", JSON.toJSONString(temp));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
    }
    
    @RequestMapping("/cancel")
    @ResponseBody
    public String cancel(String symbol, String orderId, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
            User user = (User) session.getAttribute("USER_SESSION");
            String temp = orderService.cancel(symbol, orderId, user.getApiKey(), user.getSecretKey());
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
				result.put("status", "ok");
				Mail mail = new Mail();
				mail.setUid(user.getId());
				mail.setSymbol(symbol);
				mail.setSubject(symbol + "即时单撤销成功，已提交到币安");
				mail.setContent("撤销订单号：" + orderId);
				mail.setState(0);
				mail.setCreateTime(format.format(new Date()));
				mail.setUpdateTime(format.format(new Date()));
				orderService.insertMail(mail);
			} else {
				result.put("status", "error");
			}
			result.put("msg", JSON.toJSONString(temp));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
    }  
    
    @RequestMapping("/cancelAll")
    @ResponseBody
    public String cancelAll(String symbol, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		StringBuffer uri = new StringBuffer();
    		uri.append("timestamp=").append(System.currentTimeMillis());
    		if(StringUtils.isNotEmpty(symbol)) {
    			uri.append("&symbol=").append(symbol);
    		}
            User user = (User) session.getAttribute("USER_SESSION");
            String signature = SHA256.HMACSHA256(uri.toString().getBytes(), user.getSecretKey().getBytes());
    		uri.append("&signature=").append(signature);
    		Request request = new Request.Builder()
    			.url(HttpClient.baseUrl + "/fapi/v1/allOpenOrders?" + uri.toString())
    			.header("X-MBX-APIKEY", user.getApiKey())
    			.delete().build();
    		logger.info(request.url().toString());
    		Call call = HttpClient.okHttpClient.newCall(request);
			Response response = call.execute();
			String temp = response.body().string();
			logger.info("cancelAll = " + temp);
        	result.put("status", "ok");
        	result.put("msg", temp);
    		Mail mail = new Mail();
    		mail.setUid(user.getId());
    		mail.setSymbol(symbol);
    		mail.setSubject(symbol + "的全部即时单撤销成功，已提交到币安");
    		mail.setContent(symbol + "的全部即时单撤销成功，已提交到币安");
    		mail.setState(0);
    		mail.setCreateTime(format.format(new Date()));
    		mail.setUpdateTime(format.format(new Date()));
    		orderService.insertMail(mail);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put("status", "error");
    		result.put("msg", e.getMessage());
		}
    	return result.toJSONString();
    }     
    

    @RequestMapping("/findAllOrders")
    @ResponseBody
    public String findAllOrders(String symbol, String startTime, String orderId, HttpSession session) {
    	String temp = null;
    	JSONObject result = new JSONObject();
    	try {
    		StringBuffer uri = new StringBuffer();
    		uri.append("timestamp=").append(System.currentTimeMillis());
    		if(StringUtils.isNotEmpty(symbol)) {
    			uri.append("&symbol=").append(symbol);
    		}
    		long start = System.currentTimeMillis();
    		if(StringUtils.isNotEmpty(startTime)) {
    			start = start - Integer.parseInt(startTime) * 24 * 60 * 60 * 1000;
    		}
    		uri.append("&startTime=").append(start);
            User user = (User) session.getAttribute("USER_SESSION");
            String signature = SHA256.HMACSHA256(uri.toString().getBytes(), user.getSecretKey().getBytes());
    		uri.append("&signature=").append(signature);
    		Request request = new Request.Builder()
    			.url(HttpClient.baseUrl + "/fapi/v1/allOrders?" + uri.toString())
    			.header("X-MBX-APIKEY", user.getApiKey())
    			.get().build();
    		logger.info(request.url().toString());
    		Call call = HttpClient.okHttpClient.newCall(request);
			Response response = call.execute();
			temp = response.body().string();
			List<String> lists = JSON.parseArray(temp, String.class);
			result.put("status", "ok");
			result.put("msg", temp);
			logger.info("findAllOrders = " + lists.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put("status", "error");
			if(StringUtils.isEmpty(temp)) {
				result.put("msg", e.getMessage());
			} else {
				result.put("msg", temp);
			}
		}
    	return result.toJSONString();
    }

    @RequestMapping("/balance")
    @ResponseBody
    public String balance(HttpSession session) {
    	String temp = null;
    	JSONObject result = new JSONObject();
    	try {
    		StringBuffer uri = new StringBuffer();
    		uri.append("timestamp=").append(System.currentTimeMillis());  		
            User user = (User) session.getAttribute("USER_SESSION");
            String signature = SHA256.HMACSHA256(uri.toString().getBytes(), user.getSecretKey().getBytes());
    		uri.append("&signature=").append(signature);
    		Request request = new Request.Builder()
    			.url(HttpClient.baseUrl + "/fapi/v1/balance?" + uri.toString())
    			.header("X-MBX-APIKEY", user.getApiKey())
    			.get().build();
    		logger.info(request.url().toString());
    		Call call = HttpClient.okHttpClient.newCall(request);
			Response response = call.execute();
			temp = response.body().string();
			List<String> lists = JSON.parseArray(temp, String.class);
			result.put("status", "ok");
			result.put("msg", temp);
			logger.info("balance = " + result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put("status", "error");
			if(StringUtils.isEmpty(temp)) {
				result.put("msg", e.getMessage());
			} else {
				result.put("msg", temp);
			}
		}
    	return result.toJSONString();
    }
    
    @RequestMapping("/positionRisk")
    @ResponseBody
    public String positionRisk(HttpSession session) {
    	String temp = null;
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute("USER_SESSION");
    		temp = orderService.positionRisk(user.getApiKey(), user.getSecretKey());
			List<String> lists = JSON.parseArray(temp, String.class);
			result.put("status", "ok");
			result.put("msg", temp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put("status", "error");
			if(StringUtils.isEmpty(temp)) {
				result.put("msg", e.getMessage());
			} else {
				result.put("msg", temp);
			}
		}
    	return result.toJSONString();
    }
    
    @RequestMapping("/getPrice")
    @ResponseBody
    public String getPrice() {
    	JSONObject result = new JSONObject();
		result.put("status", "ok");
		result.put("msg", JSON.toJSONString(ToolsUtils.getCurPrice()));
		return result.toJSONString();
    }
}
