package cn.itcast.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import cn.itcast.client.HttpClient;
import cn.itcast.client.SHA256;
import cn.itcast.dao.MailMapper;
import cn.itcast.dao.PlanMapper;
import cn.itcast.pojo.Config;
import cn.itcast.pojo.Mail;
import cn.itcast.pojo.Plan;
import cn.itcast.service.ConfigService;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private PlanMapper planMapper;
    
    @Autowired
    private MailMapper mailMapper;
    
    @Autowired
    private ConfigService configService;
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public List<Plan> findPlanByUid(Integer uid, String symbol) {
		return planMapper.findPlanByUid(uid.toString(), symbol);
	}
	
	public List<Plan> findFllowPlans(String symbol) {
		return planMapper.findFllowPlans(symbol);
	}
	
	public Plan findPlanById(Integer id) {
		return planMapper.findPlanById(id);
	}
	
	public List<Plan> findPlansById(Integer id) {
		return planMapper.findPlansById(id);
	}
	
	public int updatePlanById(Plan plan) {
		return planMapper.updatePlanById(plan);
	}
	
	public int insertMail(Mail mail) {
		return mailMapper.insertMail(mail);
	}

	//需要保证事务
	public String plan(String symbol, String first, String second, String third, String stop, String trigger, 
			Integer compare, Integer uid, String apiKey, String secretKey, Float curPrice) {
		String result = "";
		try {
			List<String> orderIds = new ArrayList<String>();
			int status = generateAndDealOrder(symbol, first, second, third, stop, trigger, compare, uid, apiKey, secretKey, orderIds, curPrice);
			Plan plan = new Plan();
			plan.setUid(uid);
			plan.setPid(0);
			plan.setSymbol(symbol);
			plan.setFirst(Float.parseFloat(first));
			plan.setSecond(Float.parseFloat(second));
			plan.setThird(Float.parseFloat(third));
			plan.setStop(Float.parseFloat(stop));
			plan.setTrigger(StringUtils.isNotEmpty(trigger) ? Float.parseFloat(trigger) : 0f);
			plan.setCompare(compare);
			plan.setState(status < 2 ? status : 3);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(0);
			String orders = "";
			if(status == 1) {
				for(String orderId : orderIds) {
					orders += orderId + ",";
				}
				orders = orders.substring(0, orders.length() - 1);
			}
			plan.setOrderIds(orders);
			planMapper.insertPlan(plan);
			JSONObject resultJson = new JSONObject();
			resultJson.put("status", "ok");
			resultJson.put("msg", "create plan successful");
			resultJson.put("id", plan.getId());
			result = resultJson.toString();

			Mail mail = new Mail();
			mail.setUid(plan.getUid());
			mail.setSymbol(plan.getSymbol());
			if(status == 0) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "创建成功，不满足触发条件，未提交到币安");
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    
			} else if(status == 1) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "创建成功，已提交到币安");
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    	    					
			} else {
				mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
						+ "，提交到币安失败");
				mail.setContent("异常编码：" + status);
			}
			mail.setState(0);
			mail.setCreateTime(format.format(new Date()));
			mail.setUpdateTime(format.format(new Date()));
			mailMapper.insertMail(mail);
		} catch(Exception e) {
			e.printStackTrace();
			JSONObject resultJson = new JSONObject();
			resultJson.put("status", "error");
			resultJson.put("msg", e.getMessage());
			result = resultJson.toString();
		}
		return result;
	}
	
	public String follow(Integer id, String symbol, String first, String second, String third, String stop, String trigger, 
			Integer compare, Integer uid, String apiKey, String secretKey, Float curPrice) {
		String result = "";
		try {
			List<String> orderIds = new ArrayList<String>();
			int status = generateAndDealOrder(symbol, first, second, third, stop, trigger, compare, uid, apiKey, secretKey, orderIds, curPrice);
			Plan plan = new Plan();
			plan.setUid(uid);
			plan.setPid(id);
			plan.setSymbol(symbol);
			plan.setFirst(Float.parseFloat(first));
			plan.setSecond(Float.parseFloat(second));
			plan.setThird(Float.parseFloat(third));
			plan.setStop(Float.parseFloat(stop));
			plan.setTrigger(StringUtils.isNotEmpty(trigger) ? Float.parseFloat(trigger) : 0f);
			plan.setCompare(compare);
			plan.setState(status < 2 ? status : 3);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(1);
			String orders = "";
			if(status == 1) {
				for(String orderId : orderIds) {
					orders += orderId + ",";
				}
				orders = orders.substring(0, orders.length() - 1);
			}
			plan.setOrderIds(orders);
			planMapper.insertPlan(plan);
			JSONObject resultJson = new JSONObject();
			resultJson.put("status", "ok");
			resultJson.put("msg", "create plan successful");
			result = resultJson.toString();
			
			Mail mail = new Mail();
			mail.setUid(plan.getUid());
			mail.setSymbol(plan.getSymbol());
			if(status == 0) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "的跟单创建成功，不满足触发条件，未提交到币安");
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    
			} else if(status == 1) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "的跟单创建成功，已提交到币安");
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    	    					
			} else {
				mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
						+ "的跟单，提交到币安失败");
				mail.setContent("异常编码：" + status);
			}
			mail.setState(0);
			mail.setCreateTime(format.format(new Date()));
			mail.setUpdateTime(format.format(new Date()));
			mailMapper.insertMail(mail);
		} catch(Exception e) {
			e.printStackTrace();
			JSONObject resultJson = new JSONObject();
			resultJson.put("status", "error");
			resultJson.put("msg", e.getMessage());
			result = resultJson.toString();
		}
		return result;
	}
	
	//0：不满足触发条件
	//1：满足触发条件，且成功提交币安
	//2：价格设置不正确
	//3：满足触发条件，提交币安未全部成功但是回滚成功
	//4：满足触发条件，提交币安未全部成功，回滚也未全部成功，需要人工操作
	public int generateAndDealOrder(String symbol, String first, String second, String third, String stop, String trigger, 
			Integer compare, Integer uid, String apiKey, String secretKey, List<String> orderIds, Float curPrice) {
		int seq = 0;
    	try {
    		//获取配置项
			Config config = new Config();
			config.setUid(uid);
			config.setType(symbol);
			Config allConfig = configService.findConfigByUid(config);
			//获取页面设置，校验规则，判断多还是空
			Float firstPrice = Float.parseFloat(first);
			Float secondPrice = Float.parseFloat(second);
			Float thirdPrice = Float.parseFloat(third);
			Float stopPrice = Float.parseFloat(stop);
			String side = null;
			String stopSide = null;
			Float entrustPrice = null;
			if(firstPrice >= secondPrice && secondPrice >= thirdPrice && thirdPrice > stopPrice) {
				side = "BUY";
				firstPrice = firstPrice * (1 + allConfig.getTradeOffset() / 10000);
				secondPrice = secondPrice * (1 + allConfig.getTradeOffset() / 10000);
				thirdPrice = thirdPrice * (1 + allConfig.getTradeOffset() / 10000);
				stopSide = "SELL";
				stopPrice = stopPrice * (1 - allConfig.getLossTriggerOffset() / 10000);
				entrustPrice = stopPrice * (1 - allConfig.getLossEntrustOffset() / 10000);
			} else if(firstPrice <= secondPrice && secondPrice <= thirdPrice && thirdPrice < stopPrice) {
				side = "SELL";
				firstPrice = firstPrice * (1 - allConfig.getTradeOffset() / 10000);
				secondPrice = secondPrice * (1 - allConfig.getTradeOffset() / 10000);
				thirdPrice = thirdPrice * (1 - allConfig.getTradeOffset() / 10000);
				stopSide = "BUY";
				stopPrice = stopPrice * (1 + allConfig.getLossTriggerOffset() / 10000);
				entrustPrice = stopPrice * (1 + allConfig.getLossEntrustOffset() / 10000);
			}
			if(StringUtils.isEmpty(side)) {
				return 2;
			}
			//计算下单数量
			Float diff = Math.abs(((firstPrice + secondPrice + thirdPrice) / 3) - stopPrice);
			String quantity = ToolsUtils.formatQuantity(symbol, allConfig.getLimitAmount() * allConfig.getMaxLoss() / 100 / diff / 3);
			//是否立即提交
			Float triggerPrice = null;
			if(StringUtils.isNotEmpty(trigger)) {
				triggerPrice = Float.parseFloat(trigger);
				if(compare == 0) {
					if(triggerPrice > curPrice) {
						return 0;
					}	
				} else {
					if(triggerPrice < curPrice) {
						return 0;
					}	
				}
			}
			//开始执行
			//操作第一档
			String temp = trade(symbol, side, quantity, ToolsUtils.formatPrice(symbol, firstPrice), null, "LIMIT", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
				orderIds.add(tempInfo.get("orderId"));
			} 
			seq++;
			if(orderIds.size() == seq) {
				//操作第二档
				temp = trade(symbol, side, quantity, ToolsUtils.formatPrice(symbol, secondPrice), null, "LIMIT", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
				tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
				if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
					orderIds.add(tempInfo.get("orderId"));
				} 				
			}
			seq++;
			if(orderIds.size() == seq) {
				//操作第三档
				temp = trade(symbol, side, quantity, ToolsUtils.formatPrice(symbol, thirdPrice), null, "LIMIT", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
				tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
				if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
					orderIds.add(tempInfo.get("orderId"));
				} 				
			}
			seq++;
			
			if(allConfig.getLossType() == 0) {
				if(orderIds.size() == seq) {
					//操作止损单1
					temp = trade(symbol, stopSide, quantity, ToolsUtils.formatPrice(symbol, entrustPrice), ToolsUtils.formatPrice(symbol, stopPrice), "STOP", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} 
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单2
					temp = trade(symbol, stopSide, quantity, ToolsUtils.formatPrice(symbol, entrustPrice), ToolsUtils.formatPrice(symbol, stopPrice), "STOP", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} 
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单3
					temp = trade(symbol, stopSide, quantity, ToolsUtils.formatPrice(symbol, entrustPrice), ToolsUtils.formatPrice(symbol, stopPrice), "STOP", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} 
				}
				seq++;
			} else {
				if(orderIds.size() == seq) {
					//操作止损单1
					temp = trade(symbol, stopSide, quantity, null, ToolsUtils.formatPrice(symbol, stopPrice), "STOP_MARKET", null, allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单2
					temp = trade(symbol, stopSide, quantity, null, ToolsUtils.formatPrice(symbol, stopPrice), "STOP_MARKET", null, allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单3
					temp = trade(symbol, stopSide, quantity, null, ToolsUtils.formatPrice(symbol, stopPrice), "STOP_MARKET", null, allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					}
				}
				seq++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//是否全部成功
    	if(orderIds.size() < 6) {
			for(String orderId : orderIds) {
				try {
					cancel(symbol, orderId, apiKey, secretKey);						
				} catch(Exception e) {
					return 4;
				}
			}
			return 3;
		}
    	return 1;
    }
	  
    public String trade(String symbol, String side, String quantity, String price, String stopPrice, String type, 
    		String timeInForce, String workingType, String reduceOnly, String apiKey, String secretKey) throws Exception {
    	String result = "";
		StringBuffer uri = new StringBuffer();
		uri.append("timestamp=").append(System.currentTimeMillis());
		if(StringUtils.isNotEmpty(symbol)) {
			uri.append("&symbol=").append(symbol);
		}
		if(StringUtils.isNotEmpty(side)) {
			uri.append("&side=").append(side);
		}
		if(StringUtils.isNotEmpty(quantity)) {
			uri.append("&quantity=").append(quantity);
		}
		if(StringUtils.isNotEmpty(price)) {
			uri.append("&price=").append(price);
		}
		if(StringUtils.isNotEmpty(stopPrice)) {
			uri.append("&stopPrice=").append(stopPrice);
		}
		if(StringUtils.isNotEmpty(type)) {
			uri.append("&type=").append(type);
		}
		if(StringUtils.isNotEmpty(timeInForce)) {
			uri.append("&timeInForce=").append(timeInForce);
		}
		if(StringUtils.isNotEmpty(workingType)) {
			uri.append("&workingType=").append(workingType);
		}
		if(StringUtils.isNotEmpty(reduceOnly)) {
			uri.append("&reduceOnly=").append(reduceOnly);
		}
        String signature = SHA256.HMACSHA256(uri.toString().getBytes(), secretKey.getBytes());
		uri.append("&signature=").append(signature);
		Request request = new Request.Builder()
			.url(HttpClient.baseUrl + "/fapi/v1/order?" + uri.toString())
			.header("X-MBX-APIKEY", apiKey)
			.post((new FormBody.Builder()).build()).build();
		logger.info(request.url().toString());
		Call call = HttpClient.okHttpClient.newCall(request);
		Response response = call.execute();
		result = response.body().string();
		logger.info("trade = " + result);

    	return result;
    }

    public String cancel(String symbol, String orderId, String apiKey, String secretKey) throws Exception {
    	String result = "";
		StringBuffer uri = new StringBuffer();
		uri.append("timestamp=").append(System.currentTimeMillis());
		if(StringUtils.isNotEmpty(symbol)) {
			uri.append("&symbol=").append(symbol);
		}
		if(StringUtils.isNotEmpty(orderId)) {
			uri.append("&orderId=").append(orderId);
		}    		
        String signature = SHA256.HMACSHA256(uri.toString().getBytes(), secretKey.getBytes());
		uri.append("&signature=").append(signature);
		Request request = new Request.Builder()
			.url(HttpClient.baseUrl + "/fapi/v1/order?" + uri.toString())
			.header("X-MBX-APIKEY", apiKey)
			.delete().build();
		logger.info(request.url().toString());
		Call call = HttpClient.okHttpClient.newCall(request);
		Response response = call.execute();
		result = response.body().string();
		logger.info("cancel = " + result);

    	return result;
    }
    
    public int cancelPlan(Integer uid, String symbol, String id, String orderIds, String apiKey, String secretKey) throws Exception {
    	if(StringUtils.isNotEmpty(orderIds)) {
    		String[] orders = orderIds.split(",");
    		for(String orderId : orders) {
    			try {
    				cancel(symbol, orderId, apiKey, secretKey);	
    	    	} catch(Exception e) {
    	    		e.printStackTrace();
    	    	}
    		}
    		Mail mail = new Mail();
    		mail.setUid(uid);
    		mail.setSymbol(symbol);
    		mail.setSubject(symbol + "计划单" + id + "和相关挂单撤销成功");
    		mail.setContent("关联订单：" + orderIds);
    		mail.setState(0);
    		mail.setCreateTime(format.format(new Date()));
    		mail.setUpdateTime(format.format(new Date()));
    		mailMapper.insertMail(mail);
    	}
    	Plan plan = new Plan();
    	plan.setId(Integer.parseInt(id));
    	plan.setState(4);
    	plan.setOrderIds(orderIds);
    	plan.setUpdateTime(format.format(new Date()));
    	return planMapper.updatePlanById(plan);
    }
}
