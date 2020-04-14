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

import cn.itcast.back.ClearMarketTask;
import cn.itcast.back.ThreadPool;
import cn.itcast.back.TradeMarketTask;
import cn.itcast.client.HttpClient;
import cn.itcast.client.SHA256;
import cn.itcast.constant.TransactionConstants;
import cn.itcast.pojo.Balance;
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

    	User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    	model.addAttribute(TransactionConstants.USER_ROLE, user.getRole());
    	model.addAttribute(TransactionConstants.USER_ID, user.getId());
    	model.addAttribute(TransactionConstants.USER_USERNAME, user.getUsername());
    	model.addAttribute(TransactionConstants.USER_NICKNAME, user.getNickname());
    	if(StringUtils.isNotEmpty(user.getRelaids())) {
    		List<String> idList = Arrays.asList(user.getRelaids().split(","));
    		List<User> users = userService.findUserByIds(idList);
    		if(users != null && users.size() > 0) {
    			JSONArray userInfos = new JSONArray();
    			for(User u : users) {
    				Map<String, String> temp = new HashMap<String, String>();
    				temp.put(TransactionConstants.USER_ID, "" + u.getId());
    				temp.put(TransactionConstants.USER_USERNAME, u.getUsername());
    				temp.put(TransactionConstants.USER_NICKNAME, u.getNickname());
    				userInfos.add(temp);
    			}
    			model.addAttribute("ids", userInfos.toString());            		
    		}
    	}
    	return "account";            	
    }
    
    @RequestMapping("/tradeMarket")
    @ResponseBody
    public String tradeMarket(String symbol, String type, String side, String quantity, String price, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		String temp = null;
    		String timeInForce = null;
    		String workingType = null;
    		if(type.equals(TransactionConstants.TYPE_LIMIT)) {
    			quantity = ToolsUtils.formatQuantity(symbol, Float.parseFloat(quantity));
    			price = ToolsUtils.formatPrice(symbol, Float.parseFloat(price));
    			timeInForce = TransactionConstants.TIMEINFORCE_GTC;
    			workingType = TransactionConstants.WORKINGTYPE_CONTRACT_PRICE;
    		} else {
    			price = null;
    		}
    		boolean firstsd = orderService.positionSide(user.getApiKey(), user.getSecretKey());
    		temp = orderService.trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, false, side), 
    				quantity, price, null, type, timeInForce, workingType, null, user.getApiKey(), user.getSecretKey());
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
				Mail mail = new Mail();
				mail.setUid(user.getId());
				mail.setSymbol(symbol);
				if(type.equals(TransactionConstants.TYPE_LIMIT)) {
					mail.setSubject(symbol + "即时限价单创建成功，成交价格" + price + "，已提交到币安");
				} else {
					mail.setSubject(symbol + "即时市价单创建成功，成交价格" + ToolsUtils.getCurPriceByKey(symbol) + "，已提交到币安");					
				}
				mail.setContent("提交数量：" + quantity);
				mail.setState(0);
				mail.setCreateTime(format.format(new Date()));
				mail.setUpdateTime(format.format(new Date()));
				orderService.insertMail(mail);
			} else {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			}
			result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(temp));
			Config config = new Config();
			config.setType(symbol);
			config.setLossWorkingType(user.getId().toString() + "8");;
			List<Config> allConfig = configService.findConfigFlag(config);
			for(Config c : allConfig) {
				ThreadPool.execute(new TradeMarketTask(orderService, c.getUid(), symbol, side, quantity, price, 
						type, timeInForce, workingType, c.getType(), c.getLossWorkingType()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
    }
	
    @RequestMapping("/clearMarket")
    @ResponseBody
    public String clearMarket(String symbol, String side, String quantity, HttpSession session) {
    	JSONObject result = new JSONObject();
    	String realQuantity;
    	try {
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		boolean firstsd = orderService.positionSide(user.getApiKey(), user.getSecretKey());
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
        			if(risk != null && StringUtils.isNotEmpty(risk.get(TransactionConstants.BIAN_POSITIONAMT)) 
        					&& StringUtils.isNotEmpty(risk.get(TransactionConstants.BIAN_SYMBOL)) && risk.get(TransactionConstants.BIAN_SYMBOL).equals(symbol)) {
        				if(firstsd) {
        					if(side.equals(TransactionConstants.SIDE_BUY) && risk.get(TransactionConstants.BIAN_POSITIONSIDE).equals(TransactionConstants.POSITIONSIDE_SHORT)) {
        						positionAmt = Math.abs(Float.parseFloat(risk.get(TransactionConstants.BIAN_POSITIONAMT))); 
        						break;
        					} else if(side.equals(TransactionConstants.SIDE_SELL) && risk.get(TransactionConstants.BIAN_POSITIONSIDE).equals(TransactionConstants.POSITIONSIDE_LONG)) {
        						positionAmt = Math.abs(Float.parseFloat(risk.get(TransactionConstants.BIAN_POSITIONAMT))); 
        						break;
        					}
        				} else {
        					if(risk.get(TransactionConstants.BIAN_POSITIONSIDE).equals(TransactionConstants.POSITIONSIDE_BOTH)) {
        						positionAmt = Math.abs(Float.parseFloat(risk.get(TransactionConstants.BIAN_POSITIONAMT)));     
        						break;
        					}
        				}
        			}       			
        		}
    			realQuantity = "" + (positionAmt * (Float.parseFloat(quantity) / 100));
    		}
    		String temp = orderService.trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, true, side),
    				ToolsUtils.formatQuantity(symbol, Float.parseFloat(realQuantity)), 
    				null, null, TransactionConstants.TYPE_MARKET, null, null, firstsd ? null : "true", user.getApiKey(), user.getSecretKey());
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
				Mail mail = new Mail();
				mail.setUid(user.getId());
				mail.setSymbol(symbol);
				mail.setSubject(symbol + "平仓单创建成功，成交价格" + ToolsUtils.getCurPriceByKey(symbol) + "，已提交到币安");
				mail.setContent("提交数量：" + realQuantity);
				mail.setState(0);
				mail.setCreateTime(format.format(new Date()));
				mail.setUpdateTime(format.format(new Date()));
				orderService.insertMail(mail);
			} else {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			}
			result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(temp));
			Config config = new Config();
			config.setType(symbol);
			config.setLossWorkingType(user.getId().toString() + "6");;
			List<Config> allConfig = configService.findConfigFlag(config);
			for(Config c : allConfig) {
				ThreadPool.execute(new ClearMarketTask(orderService, c.getUid(), symbol, side, quantity, null, 
						null, TransactionConstants.TYPE_MARKET, null, null, c.getType(), c.getLossWorkingType()));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
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
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		boolean firstsd = orderService.positionSide(user.getApiKey(), user.getSecretKey());
    		if(stopPrice != null) {
    			float positionAmt = 0;
        		String risks = orderService.positionRisk(user.getApiKey(), user.getSecretKey());
        		List<String> lists = JSON.parseArray(risks, String.class);
        		for(String list : lists) {
        			Map<String, String> risk = JSON.parseObject(list, new TypeReference<Map<String, String>>(){} );
        			if(risk != null && StringUtils.isNotEmpty(risk.get(TransactionConstants.BIAN_POSITIONAMT)) 
        					&& StringUtils.isNotEmpty(risk.get(TransactionConstants.BIAN_SYMBOL)) && risk.get(TransactionConstants.BIAN_SYMBOL).equals(symbol)) {
        				if(firstsd) {
        					if(side.equals(TransactionConstants.SIDE_BUY) && risk.get(TransactionConstants.BIAN_POSITIONSIDE).equals(TransactionConstants.POSITIONSIDE_SHORT)) {
        						positionAmt = Math.abs(Float.parseFloat(risk.get(TransactionConstants.BIAN_POSITIONAMT))); 
        						break;
        					} else if(side.equals(TransactionConstants.SIDE_SELL) && risk.get(TransactionConstants.BIAN_POSITIONSIDE).equals(TransactionConstants.POSITIONSIDE_LONG)) {
        						positionAmt = Math.abs(Float.parseFloat(risk.get(TransactionConstants.BIAN_POSITIONAMT))); 
        						break;
        					}
        				} else {
        					if(risk.get(TransactionConstants.BIAN_POSITIONSIDE).equals(TransactionConstants.POSITIONSIDE_BOTH)) {
        						positionAmt = Math.abs(Float.parseFloat(risk.get(TransactionConstants.BIAN_POSITIONAMT)));     
        						break;
        					}
        				}
        			}       			
        		}
    			realQuantity = "" + (positionAmt * (Float.parseFloat(quantity) / 100));
    			String temp = orderService.trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, true, side), 
    					ToolsUtils.formatQuantity(symbol, Float.parseFloat(realQuantity)), 
    					null, ToolsUtils.formatPrice(symbol, stopPrice), type, null, TransactionConstants.WORKINGTYPE_CONTRACT_PRICE, firstsd ? null : "true", 
    					user.getApiKey(), user.getSecretKey());
    			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
    			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
    				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
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
    				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    			}
    			result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(temp));
				Config config = new Config();
				config.setType(symbol);
				config.setLossWorkingType(user.getId().toString() + "7");
				List<Config> allConfig = configService.findConfigFlag(config);
				for(Config c : allConfig) {
					ThreadPool.execute(new ClearMarketTask(orderService, c.getUid(), symbol, side, quantity, null, 
							ToolsUtils.formatPrice(symbol, stopPrice), type, null, TransactionConstants.WORKINGTYPE_CONTRACT_PRICE, c.getType(), c.getLossWorkingType()));
				}
    		} else {
        		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
        		result.put(TransactionConstants.SYSTEM_MSG, "parameter error");
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
    }
    
//    @RequestMapping("/trade")
//    @ResponseBody
//    public String trade(String symbol, String side, String quantity, String price, String stopPrice, String type, 
//    		String timeInForce, String workingType, String reduceOnly, HttpSession session) {
//    	JSONObject result = new JSONObject();
//    	try {
//    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
//    		String temp = orderService.trade(symbol, side, quantity, price, stopPrice, type, timeInForce, workingType, reduceOnly, user.getApiKey(), user.getSecretKey());
//        	result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
//        	result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(temp));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
//    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
//		}
//    	return result.toJSONString();
//    }
    
    @RequestMapping("/cancel")
    @ResponseBody
    public String cancel(String symbol, String orderId, HttpSession session) {
    	JSONObject result = new JSONObject();
    	try {
            User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
            String temp = orderService.cancel(symbol, orderId, user.getApiKey(), user.getSecretKey());
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
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
				result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			}
			result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(temp));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
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
            User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
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
        	result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
        	result.put(TransactionConstants.SYSTEM_MSG, temp);
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
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
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
            User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
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
			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
			result.put(TransactionConstants.SYSTEM_MSG, temp);
			logger.info("findAllOrders = " + lists.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			if(StringUtils.isEmpty(temp)) {
				result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
			} else {
				result.put(TransactionConstants.SYSTEM_MSG, temp);
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
            User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
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
			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
			result.put(TransactionConstants.SYSTEM_MSG, temp);
			logger.info("balance = " + result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			if(StringUtils.isEmpty(temp)) {
				result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
			} else {
				result.put(TransactionConstants.SYSTEM_MSG, temp);
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
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		temp = orderService.positionRisk(user.getApiKey(), user.getSecretKey());
			List<String> lists = JSON.parseArray(temp, String.class);
			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
			result.put(TransactionConstants.SYSTEM_MSG, temp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			if(StringUtils.isEmpty(temp)) {
				result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
			} else {
				result.put(TransactionConstants.SYSTEM_MSG, temp);
			}
		}
    	return result.toJSONString();
    }
    
    @RequestMapping("/getPrice")
    @ResponseBody
    public String getPrice() {
    	JSONObject result = new JSONObject();
		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
		result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(ToolsUtils.getCurPrice()));
		return result.toJSONString();
    }
    
    @RequestMapping("/myBalance")
    @ResponseBody
    public String myBalance(HttpSession session) {
    	User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    	List<Balance> balances = orderService.findBalanceByUid(user.getId());
    	JSONObject result = new JSONObject();
		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
		result.put(TransactionConstants.SYSTEM_MSG, JSON.toJSONString(balances));
		return result.toJSONString();
    }
    
    @RequestMapping("/positionSide")
    @ResponseBody
    public String positionSide(HttpSession session) {
    	String temp = null;
    	JSONObject result = new JSONObject();
    	try {
    		User user = (User) session.getAttribute(TransactionConstants.USER_SESSION);
    		Boolean side = orderService.positionSide(user.getApiKey(), user.getSecretKey());
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
    		result.put(TransactionConstants.SYSTEM_MSG, side);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			if(StringUtils.isEmpty(temp)) {
				result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
			} else {
				result.put(TransactionConstants.SYSTEM_MSG, temp);
			}
		}
    	return result.toJSONString();
    }
}
