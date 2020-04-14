package cn.itcast.back;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import cn.itcast.constant.TransactionConstants;
import cn.itcast.pojo.Mail;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;

public class ClearMarketTask implements Runnable {

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private OrderService orderService;
	private int uid;
	private String symbol;
	private String side;
	private String quantity;
	private String price;
	private String stopPrice;
	private String type;
	private String timeInForce;
	private String workingType;
	private String apiKey;
	private String secretKey;
	
	
	public ClearMarketTask(OrderService orderService, int uid, String symbol, String side, String quantity, String price, String stopPrice, 
			String type, String timeInForce, String workingType, String apiKey, String secretKey) {
		super();
		this.orderService = orderService;
		this.uid = uid;
		this.symbol = symbol;
		this.side = side;
		this.quantity = quantity;
		this.price = price;
		this.stopPrice = stopPrice;
		this.type = type;
		this.timeInForce = timeInForce;
		this.workingType = workingType;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
	}


	@Override
	public void run() {
		try {
			Thread.sleep((new Random()).nextInt(3000));
			float positionAmt = 0;
			boolean firstsd = orderService.positionSide(apiKey, secretKey);
    		String risks = orderService.positionRisk(apiKey, secretKey);
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
    		quantity = "" + (positionAmt * (Float.parseFloat(quantity) / 100));
			String temp = orderService.trade(symbol, side, ToolsUtils.generatePositionSide(firstsd, true, side), 
					ToolsUtils.formatQuantity(symbol, Float.parseFloat(quantity)), price, 
					stopPrice, type, timeInForce, workingType, firstsd ? null : "true", apiKey, secretKey);
			Map<String, String> tempInfo = JSON.parseObject(temp, new TypeReference<Map<String, String>>(){} );
			if(tempInfo != null && StringUtils.isNotEmpty(tempInfo.get(TransactionConstants.BIAN_ORDERID))) {
				Mail mail = new Mail();
	    		mail.setUid(uid);
	    		mail.setSymbol(symbol);
	    		if(StringUtils.isNotEmpty(stopPrice)) {
	    			mail.setSubject(symbol + "止盈/止损跟单创建成功，挂单价格" + stopPrice + "，已提交到币安");
	    		} else {
	    			mail.setSubject(symbol + "平仓跟单创建成功，成交价格" + ToolsUtils.getCurPriceByKey(symbol) + "，已提交到币安");	    			
	    		}
	    		mail.setContent("提交数量：" + quantity);
	    		mail.setState(0);
	    		mail.setCreateTime(format.format(new Date()));
	    		mail.setUpdateTime(format.format(new Date()));
	    		orderService.insertMail(mail);
			} 	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
