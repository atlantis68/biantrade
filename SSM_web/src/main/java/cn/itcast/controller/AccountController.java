package cn.itcast.controller;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import cn.itcast.client.HttpClient;
import cn.itcast.client.SHA256;
import cn.itcast.pojo.Config;
import cn.itcast.pojo.User;
import cn.itcast.service.ConfigService;
import cn.itcast.service.OrderService;
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
    
	private static DecimalFormat decimalFormat = new DecimalFormat("0.000");
	
    @RequestMapping(value = "/index")
    public String logout(HttpSession session) {
        return "account";
    }
	
    @RequestMapping("/tradeMarket")
    @ResponseBody
    public String tradeMarket(String symbol, String side, String quantity, HttpSession session) {
    	JSONObject result = new JSONObject();
    	String realQuantity;
    	try {
    		StringBuffer uri = new StringBuffer();
    		uri.append("timestamp=").append(System.currentTimeMillis());
    		if(StringUtils.isNotEmpty(symbol)) {
    			uri.append("&symbol=").append(symbol);
    		}
    		User user = (User) session.getAttribute("USER_SESSION");
    		if(StringUtils.isEmpty(quantity)) {
    			Config config = new Config();
    			config.setUid(user.getId());
    			config.setType(symbol);
    			Config allConfig = configService.findConfigByUid(config);
    			int number = allConfig.getMarketAmount();
    			float price = ToolsUtils.getCurPriceByKey(symbol);
    			realQuantity = decimalFormat.format(number * allConfig.getRate() / price);
    		} else {
    			realQuantity = quantity;
    		}
    		String temp = orderService.trade(symbol, side, realQuantity, null, null, "MARKET", null, null, null, user.getApiKey(), user.getSecretKey());
        	result.put("status", "ok");
        	result.put("msg", JSON.toJSONString(temp));
        	if(user.getRole() == 0) {
				Config config = new Config();
				config.setType(symbol);
				List<Config> allConfig = configService.findConfigFlag2(config);
				for(Config c : allConfig) {
	        		if(StringUtils.isEmpty(quantity)) {
		    			int number = c.getMarketAmount();
		    			float price = ToolsUtils.getCurPriceByKey(symbol);
	        			realQuantity = decimalFormat.format(number * c.getRate() / price);
	        		} else {
	        			realQuantity = quantity;
	        		}
					orderService.trade(symbol, side, realQuantity, null, null, "MARKET", null, null, null, c.getType(), c.getLossWorkingType());
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
    		StringBuffer uri = new StringBuffer();
    		uri.append("timestamp=").append(System.currentTimeMillis());  		
            User user = (User) session.getAttribute("USER_SESSION");
            String signature = SHA256.HMACSHA256(uri.toString().getBytes(), user.getSecretKey().getBytes());
    		uri.append("&signature=").append(signature);
    		Request request = new Request.Builder()
    			.url(HttpClient.baseUrl + "/fapi/v1/positionRisk?" + uri.toString())
    			.header("X-MBX-APIKEY", user.getApiKey())
    			.get().build();
    		logger.info(request.url().toString());
    		Call call = HttpClient.okHttpClient.newCall(request);
			Response response = call.execute();
			temp = response.body().string();
			List<String> lists = JSON.parseArray(temp, String.class);
			result.put("status", "ok");
			result.put("msg", temp);
			logger.info("positionRisk = " + result);
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
}
