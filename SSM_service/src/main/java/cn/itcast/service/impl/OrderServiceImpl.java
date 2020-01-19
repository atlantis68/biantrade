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
import cn.itcast.model.Result;
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
	
	public List<Plan> findPlanByUid(Integer uid) {
		return planMapper.findPlanByUid(uid.toString());
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
	
	public List<Plan> findPlanByTime(Integer time) {
		return planMapper.findPlanByTime(time);
	}

	//需要保证事务
	public String plan(String symbol, String first, String second, String third, String stop, String trigger, 
			Integer compare, String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, Float curPrice) {
		String result = "";
		try {
			List<String> orderIds = new ArrayList<String>();
			Result res = generateAndDealOrder(symbol, first, second, third, stop, trigger, compare, 
					trigger1, compare1, uid, apiKey, secretKey, orderIds, curPrice);
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
			plan.setTrigger1(StringUtils.isNotEmpty(trigger1) ? Float.parseFloat(trigger1) : 0f);
			plan.setCompare1(compare1);
			plan.setState(res.getState() < 2 ? res.getState() : 3);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(0);
			String orders = "";
			if(res.getState() == 1) {
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

			Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), null, null, 
					0, format.format(new Date()), format.format(new Date()));
			if(res.getState() == 0) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "创建成功，不满足触发条件，未提交到币安：" + res.getMsg());
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    
			} else if(res.getState() == 1) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "创建成功，已提交到币安");
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    	    					
			} else {
				mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
						+ "，提交到币安失败，异常编码：" + res.getState());
				mail.setContent("异常详情：" + res.getMsg());
			}
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
			Integer compare, String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, Float curPrice) {
		String result = "";
		try {
			List<String> orderIds = new ArrayList<String>();
			Result res = generateAndDealOrder(symbol, first, second, third, stop, trigger, compare, 
					trigger1, compare1, uid, apiKey, secretKey, orderIds, curPrice);
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
			plan.setTrigger1(StringUtils.isNotEmpty(trigger1) ? Float.parseFloat(trigger1) : 0f);
			plan.setCompare1(compare1);
			plan.setState(res.getState() < 2 ? res.getState() : 3);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(1);
			String orders = "";
			if(res.getState() == 1) {
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

    		Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), null, null, 
    				0, format.format(new Date()), format.format(new Date()));
			if(res.getState() == 0) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "的跟单创建成功，不满足触发条件，未提交到币安：" + res.getMsg());
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    
			} else if(res.getState() == 1) {
				mail.setSubject(symbol + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") 
						+ first + "的跟单创建成功，已提交到币安");
				mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
						+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    	    					
			} else {
				mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
						+ "的跟单，提交到币安失败，异常编码：" + res.getState());
				mail.setContent("异常详情：" + res.getMsg());
			}
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
	public Result generateAndDealOrder(String symbol, String first, String second, String third, String stop, String trigger, 
			Integer compare, String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, List<String> orderIds, Float curPrice) {
		int state = 1;
		String msg = "成功";
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
				return new Result(2, "价格设置不合理");
			}
			//计算下单数量
			//三档平均值
			Float avg = (firstPrice + secondPrice + thirdPrice) / 3;
			//止损差价
			Float diff = Math.abs(avg - stopPrice);
			Float threshold = diff / avg;
			int lever = 0;
			if(symbol.toUpperCase().equals("BTCUSDT")) {
				if(threshold <= 0.01) {
					lever = 100;
				} else {
					lever = (int)(avg / diff);
				}
			} else {
				if(threshold <= 0.02) {
					lever = 50;
				} else {
					lever = (int)(avg / diff);
				}
			}
			if(lever == 0) {
				lever = 1;
			}
			int range = 0;
			if(symbol.toUpperCase().equals("BTCUSDT")) {
				if(lever <= 10) {
					range = 10000000;
				} else if(lever <= 20) {
					range = 5000000;
				} else if(lever <= 50) {
					range = 1000000;
				} else if(lever <= 100) {
					range = 250000;
				} else if(lever <= 125) {
					range = 50000;
				}
			} else {
				if(lever <= 10) {
					range = 1000000;
				} else if(lever <= 25) {
					range = 250000;
				} else if(lever <= 50) {
					range = 50000;
				} 
			}
			float par1 = allConfig.getLimitAmount() * allConfig.getMaxLoss() / 100 / diff;
			float par2 = range / ToolsUtils.getCurPriceByKey(symbol);
			String quantity = ToolsUtils.formatQuantity(symbol, (par1 < par2 ? par1 : par2) / 3);
			//是否立即提交
			Float triggerPrice = null;
			if(StringUtils.isNotEmpty(trigger)) {
				triggerPrice = Float.parseFloat(trigger);
				if(compare == 0) {
					if(triggerPrice > curPrice) {
						return new Result(0, "现价低于触发价");
					}	
				} else {
					if(triggerPrice < curPrice) {
						return new Result(0, "现价高于触发价");
					}	
				}
			}
			//开始执行
			//调整杠杆
			leverage(symbol, lever, apiKey, secretKey);
			//操作第一档
			String temp = trade(symbol, side, quantity, ToolsUtils.formatPrice(symbol, firstPrice), 
					null, "LIMIT", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
				orderIds.add(tempInfo.get("orderId"));
			} else {
				msg = temp;
			}
			seq++;
			if(orderIds.size() == seq) {
				//操作第二档
				temp = trade(symbol, side, quantity, ToolsUtils.formatPrice(symbol, secondPrice), 
						null, "LIMIT", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
				tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
				if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
					orderIds.add(tempInfo.get("orderId"));
				} else {
					msg = temp;
				}			
			}
			seq++;
			if(orderIds.size() == seq) {
				//操作第三档
				temp = trade(symbol, side, quantity, ToolsUtils.formatPrice(symbol, thirdPrice), 
						null, "LIMIT", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
				tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
				if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
					orderIds.add(tempInfo.get("orderId"));
				} else {
					msg = temp;
				}	
			}
			seq++;
			
			if(allConfig.getLossType() == 0) {
				if(orderIds.size() == seq) {
					//操作止损单1
					temp = trade(symbol, stopSide, quantity, ToolsUtils.formatPrice(symbol, entrustPrice), 
							ToolsUtils.formatPrice(symbol, stopPrice), "STOP", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单2
					temp = trade(symbol, stopSide, quantity, ToolsUtils.formatPrice(symbol, entrustPrice), 
							ToolsUtils.formatPrice(symbol, stopPrice), "STOP", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单3
					temp = trade(symbol, stopSide, quantity, ToolsUtils.formatPrice(symbol, entrustPrice), 
							ToolsUtils.formatPrice(symbol, stopPrice), "STOP", "GTC", allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} else {
						msg = temp;
					}
				}
				seq++;
			} else {
				if(orderIds.size() == seq) {
					//操作止损单1
					temp = trade(symbol, stopSide, quantity, null, ToolsUtils.formatPrice(symbol, stopPrice), 
							"STOP_MARKET", null, allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单2
					temp = trade(symbol, stopSide, quantity, null, ToolsUtils.formatPrice(symbol, stopPrice), 
							"STOP_MARKET", null, allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单3
					temp = trade(symbol, stopSide, quantity, null, ToolsUtils.formatPrice(symbol, stopPrice), 
							"STOP_MARKET", null, allConfig.getLossWorkingType(), null, apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get("orderId"))) {
						orderIds.add(tempInfo.get("orderId"));
					} else {
						msg = temp;
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
					state = 4;
				}
			}
			state = 3;
		}
    	return new Result(state, msg);
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
    
    public int cancelPlan(Integer uid, String symbol, String id, String orderIds, int state, String apiKey, String secretKey) throws Exception {
    	if(StringUtils.isNotEmpty(orderIds)) {
    		String[] orders = orderIds.split(",");
    		for(String orderId : orders) {
    			try {
    				cancel(symbol, orderId, apiKey, secretKey);	
    	    	} catch(Exception e) {
    	    		e.printStackTrace();
    	    	}
    		}
    		Mail mail = ToolsUtils.generateMail(uid, symbol, symbol + "计划单" + id + "和相关挂单撤销成功", 
    				"关联订单：" + orderIds, 0, format.format(new Date()), format.format(new Date()));
    		mailMapper.insertMail(mail);
    	}
    	Plan plan = new Plan();
    	plan.setId(Integer.parseInt(id));
    	plan.setState(state);
    	plan.setOrderIds(orderIds);
    	plan.setUpdateTime(format.format(new Date()));
    	return planMapper.updatePlanById(plan);
    }
    
    public String leverage(String symbol, int leverage, String apiKey, String secretKey) throws Exception {
    	String result = "";
		StringBuffer uri = new StringBuffer();
		uri.append("timestamp=").append(System.currentTimeMillis()).append("&leverage=").append(leverage);
		if(StringUtils.isNotEmpty(symbol)) {
			uri.append("&symbol=").append(symbol);
		}
        String signature = SHA256.HMACSHA256(uri.toString().getBytes(), secretKey.getBytes());
		uri.append("&signature=").append(signature);
		Request request = new Request.Builder()
			.url(HttpClient.baseUrl + "/fapi/v1/leverage?" + uri.toString())
			.header("X-MBX-APIKEY", apiKey)
			.post((new FormBody.Builder()).build()).build();
		logger.info(request.url().toString());
		Call call = HttpClient.okHttpClient.newCall(request);
		Response response = call.execute();
		result = response.body().string();
		logger.info("leverage = " + result);

    	return result;
    }
    
    public String positionRisk(String apiKey, String secretKey) throws Exception {
    	String result = "";
		StringBuffer uri = new StringBuffer();
		uri.append("timestamp=").append(System.currentTimeMillis());
        String signature = SHA256.HMACSHA256(uri.toString().getBytes(), secretKey.getBytes());
		uri.append("&signature=").append(signature);
		Request request = new Request.Builder()
			.url(HttpClient.baseUrl + "/fapi/v1/positionRisk?" + uri.toString())
			.header("X-MBX-APIKEY", apiKey)
			.get().build();
		logger.info(request.url().toString());
		Call call = HttpClient.okHttpClient.newCall(request);
		Response response = call.execute();
		result = response.body().string();
		logger.info("positionRisk = " + result);

    	return result;
    }
    
    public int warn(String id) throws Exception {
		List<Plan> plans = findPlansById(Integer.parseInt(id));
		for(Plan plan : plans) {
    		Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), plan.getSymbol() + "计划单" 
    				+ (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") + plan.getFirst() + "建议止盈", 
    				"", 0, format.format(new Date()), format.format(new Date()));
    		mailMapper.insertMail(mail);
		}	
    	return plans.size();
    }
}
