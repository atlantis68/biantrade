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
import cn.itcast.constant.TransactionConstants;
import cn.itcast.dao.BalanceMapper;
import cn.itcast.dao.MailMapper;
import cn.itcast.dao.PlanMapper;
import cn.itcast.model.Result;
import cn.itcast.pojo.Balance;
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
    
    @Autowired
    private BalanceMapper balanceMapper;
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public List<Plan> findPlanByUid(Integer uid) {
		return planMapper.findPlanByUid(uid.toString());
	}
	
	public List<Plan> findCachePlanByUid(Integer uid) {
		return planMapper.findCachePlanByUid(uid.toString());
	}
	
	public List<Plan> findFllowPlans(Integer level) {
		return planMapper.findFllowPlans(level);
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
	
	public List<Plan> findPlanByTime(Integer time, Integer level) {
		return planMapper.findPlanByTime(time, level);
	}
	
	public List<Balance> findBalanceByUid(Integer uid) {
		return balanceMapper.findBalanceByUid(uid);
	}

	//需要保证事务
	public String plan(String symbol, String first, String second, String third, String stop, String trigger, Integer compare, 
    		String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, Float curPrice, Integer level) {
		String result = "";
		try {
			List<String> orderIds = new ArrayList<String>();
			Result res = generateAndDealOrder(symbol, first, second, third, stop, trigger, compare, 
					trigger1, compare1, uid, apiKey, secretKey, orderIds, curPrice, false);
			Plan plan = new Plan();
			plan.setUid(uid);
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
			plan.setState(res.getState() < 2 ? res.getState() : 3);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(0);
			if(level != null) {
				plan.setLevel(level);
			} else {
				plan.setLevel(1);
			}
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
			resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
			resultJson.put(TransactionConstants.SYSTEM_MSG, "create plan successful");
			resultJson.put(TransactionConstants.USER_ID, plan.getId());
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
			resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			resultJson.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
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
					trigger1, compare1, uid, apiKey, secretKey, orderIds, curPrice, false);
			Plan plan = new Plan();
			plan.setUid(uid);
			plan.setPid(id);
			plan.setSymbol(symbol);
			plan.setFirst(Float.parseFloat(first));
			plan.setSecond(Float.parseFloat(second));
			plan.setThird(Float.parseFloat(third));
			plan.setStop(Float.parseFloat(stop));
			plan.setTrigger(StringUtils.isNotEmpty(trigger) ? Float.parseFloat(trigger) : 0f);
			plan.setCompare(StringUtils.isNotEmpty(trigger) ? compare : 0);
			plan.setTrigger1(StringUtils.isNotEmpty(trigger1) ? Float.parseFloat(trigger1) : 0f);
			plan.setCompare1(StringUtils.isNotEmpty(trigger1) ? compare1 : 1);
			plan.setState(res.getState() < 2 ? res.getState() : 3);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(1);
			plan.setLevel(1);
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
			resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
			resultJson.put(TransactionConstants.SYSTEM_MSG, "create plan successful");
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
			resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			resultJson.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
			result = resultJson.toString();
		}
		return result;
	}
	
	//0：不满足触发条件
	//1：满足触发条件，且成功提交币安
	//2：价格设置不正确
	//3：满足触发条件，提交币安未全部成功但是回滚成功
	//4：满足触发条件，提交币安未全部成功，回滚也未全部成功，需要人工操作
	public Result generateAndDealOrder(String symbol, String first, String second, String third, String stop, 
			String trigger, Integer compare, String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, 
			List<String> orderIds, Float curPrice, boolean mock) {
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
			if(firstPrice > stopPrice && secondPrice > stopPrice && thirdPrice > stopPrice) {
				side = TransactionConstants.SIDE_BUY;
				firstPrice = firstPrice * (1 + allConfig.getTradeOffset() / 10000);
				secondPrice = secondPrice * (1 + allConfig.getTradeOffset() / 10000);
				thirdPrice = thirdPrice * (1 + allConfig.getTradeOffset() / 10000);
				stopSide = TransactionConstants.SIDE_SELL;
				stopPrice = stopPrice * (1 - allConfig.getLossTriggerOffset() / 10000);
				entrustPrice = stopPrice * (1 - allConfig.getLossEntrustOffset() / 10000);
			} else if(firstPrice < stopPrice && secondPrice < stopPrice && thirdPrice < stopPrice) {
				side = TransactionConstants.SIDE_SELL;
				firstPrice = firstPrice * (1 - allConfig.getTradeOffset() / 10000);
				secondPrice = secondPrice * (1 - allConfig.getTradeOffset() / 10000);
				thirdPrice = thirdPrice * (1 - allConfig.getTradeOffset() / 10000);
				stopSide = TransactionConstants.SIDE_BUY;
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
			float par2 = range / avg;
			String quantity = ToolsUtils.formatQuantity(symbol, (par1 < par2 ? par1 : par2) / 3);
			if(mock) {
				JSONObject json = new JSONObject();
				json.put("fisrt", ToolsUtils.formatPrice(symbol, firstPrice));
				json.put("second", ToolsUtils.formatPrice(symbol, secondPrice));
				json.put("third", ToolsUtils.formatPrice(symbol, thirdPrice));
				json.put("stop", ToolsUtils.formatPrice(symbol, stopPrice));
				json.put("avg", ToolsUtils.formatPrice(symbol, avg));
				json.put("quantity", Float.parseFloat(quantity));
				json.put("lever", lever);
				json.put("margin", ToolsUtils.formatPrice(symbol, avg * Float.parseFloat(quantity) * 3 / lever));
				json.put("lossrate", ToolsUtils.formatPrice("BTCUSDT", threshold * lever * 100) + "%");
				json.put("losspredict", ToolsUtils.formatPrice("BTCUSDT", diff * Float.parseFloat(quantity) * 3));
				return new Result(-1, json.toJSONString());
			}
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
			boolean firstsd = positionSide(apiKey, secretKey);
			//操作第一档
			String temp = trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, false, side), quantity, 
					ToolsUtils.formatPrice(symbol, firstPrice), null, TransactionConstants.TYPE_LIMIT, 
					TransactionConstants.TIMEINFORCE_GTC, allConfig.getLossWorkingType(), null, apiKey, secretKey);
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
				orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
			} else {
				msg = temp;
			}
			seq++;
			if(orderIds.size() == seq) {
				//操作第二档
				temp = trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, false, side), quantity, 
						ToolsUtils.formatPrice(symbol, secondPrice), null, TransactionConstants.TYPE_LIMIT, 
						TransactionConstants.TIMEINFORCE_GTC, allConfig.getLossWorkingType(), null, apiKey, secretKey);
				tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
				if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
					orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
				} else {
					msg = temp;
				}			
			}
			seq++;
			if(orderIds.size() == seq) {
				//操作第三档
				temp = trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, false, side), quantity, 
						ToolsUtils.formatPrice(symbol, thirdPrice), null, TransactionConstants.TYPE_LIMIT, 
						TransactionConstants.TIMEINFORCE_GTC, allConfig.getLossWorkingType(), null, apiKey, secretKey);
				tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
				if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
					orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
				} else {
					msg = temp;
				}	
			}
			seq++;
			
			if(allConfig.getLossType() == 0) {
				if(orderIds.size() == seq) {
					//操作止损单1
					temp = trade(symbol, stopSide, ToolsUtils.generatePositionSide(firstsd, true, stopSide), quantity, 
							ToolsUtils.formatPrice(symbol, entrustPrice), ToolsUtils.formatPrice(symbol, stopPrice), 
							TransactionConstants.TYPE_STOP, TransactionConstants.TIMEINFORCE_GTC, 
							allConfig.getLossWorkingType(), firstsd ? null : "true", apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
						orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单2
					temp = trade(symbol, stopSide, ToolsUtils.generatePositionSide(firstsd, true, stopSide), quantity, 
							ToolsUtils.formatPrice(symbol, entrustPrice), ToolsUtils.formatPrice(symbol, stopPrice), 
							TransactionConstants.TYPE_STOP, TransactionConstants.TIMEINFORCE_GTC, 
							allConfig.getLossWorkingType(), firstsd ? null : "true", apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
						orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单3
					temp = trade(symbol, stopSide, ToolsUtils.generatePositionSide(firstsd, true, stopSide), quantity, 
							ToolsUtils.formatPrice(symbol, entrustPrice), ToolsUtils.formatPrice(symbol, stopPrice), 
							TransactionConstants.TYPE_STOP, TransactionConstants.TIMEINFORCE_GTC, 
							allConfig.getLossWorkingType(), firstsd ? null : "true", apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
						orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
					} else {
						msg = temp;
					}
				}
				seq++;
			} else {
				if(orderIds.size() == seq) {
					//操作止损单1
					temp = trade(symbol, stopSide, ToolsUtils.generatePositionSide(firstsd, true, stopSide),  quantity, null, 
							ToolsUtils.formatPrice(symbol, stopPrice),  TransactionConstants.TYPE_STOP_MARKET, null, allConfig.getLossWorkingType(), 
							firstsd ? null : "true", apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
						orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单2
					temp = trade(symbol, stopSide, ToolsUtils.generatePositionSide(firstsd, true, stopSide), quantity, null, 
							ToolsUtils.formatPrice(symbol, stopPrice),  TransactionConstants.TYPE_STOP_MARKET, null, allConfig.getLossWorkingType(), 
							firstsd ? null : "true", apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
						orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
					} else {
						msg = temp;
					}
				}
				seq++;
				if(orderIds.size() == seq) {
					//操作止损单3
					temp = trade(symbol, stopSide, ToolsUtils.generatePositionSide(firstsd, true, stopSide), quantity, null, 
							ToolsUtils.formatPrice(symbol, stopPrice), TransactionConstants.TYPE_STOP_MARKET, null, allConfig.getLossWorkingType(), 
							firstsd ? null : "true", apiKey, secretKey);
					tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
						orderIds.add(tempInfo.get(TransactionConstants.BIAN_ORDERID));
					} else {
						msg = temp;
					}
				}
				seq++;
			}
		} catch (Exception e) {
			msg = e.getMessage();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//是否全部成功
    	if(orderIds.size() < 6) {
    		boolean allCanceled = true;
			for(String orderId : orderIds) {
				try {
					String temp = cancel(symbol, orderId, apiKey, secretKey);	
					Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
					if(!(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID)))) {
						allCanceled = false;
					} 
				} catch(Exception e) {
					allCanceled = false;
				}
			}
			if(allCanceled) {
				state = 3;				
			} else {
				state = 4;
			}
		}
    	return new Result(state, msg);
    }
	  
    public String trade(String symbol, String side, String positionSide, String quantity, String price, String stopPrice, String type, 
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
		if(StringUtils.isNotEmpty(positionSide)) {
			uri.append("&positionSide=").append(positionSide);
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
    		Mail mail = ToolsUtils.generateMail(uid, symbol, symbol + "订单" + id + "和相关挂单撤销成功", 
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
			String subject = "";
			if(plan.getType() < 2) {
				subject = plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）") + plan.getFirst() + "建议止盈";
			} else {
				subject = plan.getSymbol() + "策略单" + (plan.getType() == 2 ? "（多单）" : "（空单）") + "建议止盈";
			}
    		Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), subject, 
    				"", 0, format.format(new Date()), format.format(new Date()));
    		mailMapper.insertMail(mail);
		}	
    	return plans.size();
    }
    
    public int insertPlan(Plan plan) {
    	return planMapper.insertPlan(plan);
    }
    
    public Boolean positionSide(String apiKey, String secretKey) throws Exception {
    	String result = "";
		StringBuffer uri = new StringBuffer();
		uri.append("timestamp=").append(System.currentTimeMillis());
        String signature = SHA256.HMACSHA256(uri.toString().getBytes(), secretKey.getBytes());
		uri.append("&signature=").append(signature);
		Request request = new Request.Builder()
			.url(HttpClient.baseUrl + "/fapi/v1/positionSide/dual?" + uri.toString())
			.header("X-MBX-APIKEY", apiKey)
			.get().build();
		logger.info(request.url().toString());
		Call call = HttpClient.okHttpClient.newCall(request);
		Response response = call.execute();
		result = response.body().string();
		logger.info("positionSide = " + result);
		JSONObject json = JSON.parseObject(result);
		if(json.get("dualSidePosition") != null) {
			return Boolean.parseBoolean(json.getString("dualSidePosition"));			
		}
		return null;
    }
    
	//需要保证事务
	public String strategyMarket(String symbol, String side, Integer uid, String apiKey, String secretKey, Integer level, Integer id) {
		JSONObject resultJson = new JSONObject();
		try {
			String temp = null;
			Config config = new Config();
			config.setUid(uid);
			config.setType(symbol);
			Config allConfig = configService.findConfigByUid(config);
			boolean firstsd = positionSide(apiKey, secretKey);
			temp = trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, false, side), "" + allConfig.getMarketAmount(), 
					null, null, TransactionConstants.TYPE_MARKET, null, null, null, apiKey, secretKey);  
			Float curPrice = ToolsUtils.getCurPriceByKey(symbol);
			Plan plan = new Plan();
			plan.setUid(uid);
			plan.setPid(id != null ? id : 0);
			plan.setSymbol(symbol);
			plan.setFirst(curPrice);
			plan.setSecond(curPrice);
			plan.setThird(curPrice);
			plan.setStop(-1f);
			plan.setTrigger(0f);
			plan.setCompare(0);
			plan.setTrigger1(0f);
			plan.setCompare1(1);
			plan.setCreateTime(format.format(new Date()));
			plan.setUpdateTime(format.format(new Date()));
			plan.setType(side.equals(TransactionConstants.SIDE_BUY) ? 2 : 3);
			plan.setLevel(level != null ? level : 1);
			
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
				plan.setState(2);
				plan.setOrderIds(tempInfo.get(TransactionConstants.BIAN_ORDERID));
				resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
				resultJson.put(TransactionConstants.SYSTEM_MSG, "create strategy market successful");
			} else {
				plan.setState(3);
				plan.setOrderIds("");
				resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
				resultJson.put(TransactionConstants.SYSTEM_MSG, "create strategy market failed");
			}
			planMapper.insertPlan(plan);
			Mail mail = new Mail();
			resultJson.put(TransactionConstants.USER_ID, plan.getId());
			mail.setUid(uid);
			mail.setSymbol(symbol);
			if(plan.getState() == 2) {
				mail.setSubject(symbol + "策略" + (plan.getType() == 2 ? "多单" : "空单") + (id != null ? "跟单" : "") 
						+ "创建成功，预计成交价格在" + curPrice + "附近，已提交到币安");
			} else {
				mail.setSubject(symbol + "策略" + (plan.getType() == 2 ? "多单" : "空单") + (id != null ? "跟单" : "") 
						+ "提交币安失败，错误原因：" + temp);
			}
			mail.setContent("提交数量：" + allConfig.getMarketAmount());
			mail.setState(0);
			mail.setCreateTime(format.format(new Date()));
			mail.setUpdateTime(format.format(new Date()));
			insertMail(mail);
		} catch(Exception e) {
			e.printStackTrace();
			resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			resultJson.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
		return resultJson.toString();
	}
	
	//需要保证事务
	public String strategyStop(String symbol, String side, String stopPrice, Integer uid, String id, String apiKey, String secretKey) {
		String result = "";
		try {
			String temp = null;
			Config config = new Config();
			config.setUid(uid);
			config.setType(symbol);
			Config allConfig = configService.findConfigByUid(config);
			boolean firstsd = positionSide(apiKey, secretKey);
			temp = trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, true, side), "" + allConfig.getMarketAmount(), 
				null, ToolsUtils.formatPrice(symbol, Float.parseFloat(stopPrice)), TransactionConstants.TYPE_STOP_MARKET, 
				null, allConfig.getLossWorkingType(), firstsd ? null : "true", apiKey, secretKey);
			Plan plan = planMapper.findPlanById(Integer.parseInt(id));
			plan.setUpdateTime(format.format(new Date()));
			Mail mail = new Mail();
			mail.setUid(uid);
			mail.setSymbol(symbol);
			mail.setContent("提交数量：" + allConfig.getMarketAmount());
			mail.setState(0);
			mail.setCreateTime(format.format(new Date()));
			mail.setUpdateTime(format.format(new Date()));
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
				plan.setStop(Float.parseFloat(stopPrice));
				plan.setOrderIds(plan.getOrderIds() + "," + tempInfo.get(TransactionConstants.BIAN_ORDERID));
				mail.setSubject(symbol + "策略" + (plan.getType() == 2 ? "多单" : "空单") + "止损" + (plan.getPid() == 0 ? "" : "跟") 
						+ "单创建成功，价格" + stopPrice + "，已提交到币安");	
				JSONObject resultJson = new JSONObject();
				resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
				resultJson.put(TransactionConstants.SYSTEM_MSG, "create strategy stop successful");
				result = resultJson.toString();
			} else {
				mail.setSubject(symbol + "策略" + (plan.getType() == 2 ? "多单" : "空单") + "止损" + (plan.getPid() == 0 ? "" : "跟") 
						+ "单创建失败");	
				JSONObject resultJson = new JSONObject();
				resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
				resultJson.put(TransactionConstants.SYSTEM_MSG, "create strategy stop failed");
				result = resultJson.toString();
			}
			planMapper.updateStrategyById(plan);			
			insertMail(mail);
		} catch(Exception e) {
			e.printStackTrace();
			JSONObject resultJson = new JSONObject();
			resultJson.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
			resultJson.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
			result = resultJson.toString();
		}
		return result;
	}
	
    public String cancelAll(String symbol, Integer uid, String apiKey, String secretKey) throws Exception {
    	JSONObject result = new JSONObject();
    	try {
    		StringBuffer uri = new StringBuffer();
    		uri.append("timestamp=").append(System.currentTimeMillis());
    		if(StringUtils.isNotEmpty(symbol)) {
    			uri.append("&symbol=").append(symbol);
    		}
            String signature = SHA256.HMACSHA256(uri.toString().getBytes(), secretKey.getBytes());
    		uri.append("&signature=").append(signature);
    		Request request = new Request.Builder()
    			.url(HttpClient.baseUrl + "/fapi/v1/allOpenOrders?" + uri.toString())
    			.header("X-MBX-APIKEY", apiKey)
    			.delete().build();
    		logger.info(request.url().toString());
    		Call call = HttpClient.okHttpClient.newCall(request);
			Response response = call.execute();
			String temp = response.body().string();
			logger.info("cancelAll = " + temp);
        	result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_OK);
        	result.put(TransactionConstants.SYSTEM_MSG, temp);
    		Mail mail = new Mail();
    		mail.setUid(uid);
    		mail.setSymbol(symbol);
    		mail.setSubject(symbol + "的全部即时单撤销成功，已提交到币安");
    		mail.setContent(symbol + "的全部即时单撤销成功，已提交到币安");
    		mail.setState(0);
    		mail.setCreateTime(format.format(new Date()));
    		mail.setUpdateTime(format.format(new Date()));
    		insertMail(mail);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    		result.put(TransactionConstants.SYSTEM_STATUS, TransactionConstants.SYSTEM_STATUS_ERROR);
    		result.put(TransactionConstants.SYSTEM_MSG, e.getMessage());
		}
    	return result.toJSONString();
    }
}
