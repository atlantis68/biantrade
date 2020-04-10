package cn.itcast.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.itcast.dao.MailMapper;
import cn.itcast.dao.MonitorMapper;
import cn.itcast.pojo.Mail;
import cn.itcast.pojo.Monitor;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;

public class MonitorUser implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MonitorUser.class);
    
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private MonitorMapper monitorMapper;
    
    @Autowired
    private MailMapper mailMapper;
    
    @Autowired
    private OrderService orderService;
    
    public MonitorUser() {
    	new Thread(this).start();
    }
    
	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while(true) {
	    	try {
	    		List<Monitor> monitors = monitorMapper.findMoniteInfo();
	    		for(Monitor monitor : monitors) {
	        		String temp = orderService.positionRisk(monitor.getApikey(), monitor.getSecretkey());
	    			List<String> lists = JSON.parseArray(temp, String.class);
	    			for(String list : lists) {
	    				JSONObject json = JSON.parseObject(list);
	    				String name = monitor.getNickname() + "（" + monitor.getUsername() + "）" + json.getString("symbol") + "的" 
	    						+ ToolsUtils.parsePositionSide(json.getString("positionSide"));
	    				String value = ToolsUtils.getUserPositionAmt(name);
	    				if(StringUtils.isEmpty(value)) {
	    					ToolsUtils.setUserPositionAmt(name, json.getString("positionAmt"));
	    				} else {
	    					if(!value.equals(json.getString("positionAmt"))) {
	    						ToolsUtils.setUserPositionAmt(name, json.getString("positionAmt"));
	    						String mails = monitor.getMails();
	    						if(StringUtils.isNotEmpty(mails)) {
	    							String[] mailIds = mails.split(",");
	    							for(String mailId : mailIds) {
	    								Mail mail = ToolsUtils.generateMail(Integer.parseInt(mailId), json.getString("symbol"), 
	    										name + "的仓位发生变化：" + value + "->" + json.getString("positionAmt"), 
	    										name + "的仓位发生变化：" + value + "->" + json.getString("positionAmt"), 
	    										0, format.format(new Date()), format.format(new Date()));
	    								mailMapper.insertMail(mail);	
	    							}
	    						}
	    						
	    					}
	    				}
	    				value = ToolsUtils.getUserLeverage(name);
	    				if(StringUtils.isEmpty(value)) {
	    					ToolsUtils.setUserLeverage(name, json.getString("leverage"));
	    				} else {
	    					if(!value.equals(json.getString("leverage"))) {
	    						ToolsUtils.setUserLeverage(name, json.getString("leverage"));
	    						String mails = monitor.getMails();
	    						if(StringUtils.isNotEmpty(mails)) {
	    							String[] mailIds = mails.split(",");
	    							for(String mailId : mailIds) {
	    	    						Mail mail = ToolsUtils.generateMail(Integer.parseInt(mailId), json.getString("symbol"), 
	    	    								name + "的杠杆发生变化：" + value + "->" + json.getString("leverage"), 
	    	    								name + "的杠杆发生变化：" + value + "->" + json.getString("leverage"), 
	    	    								0, format.format(new Date()), format.format(new Date()));
	    	    						mailMapper.insertMail(mail);	
	    							}
	    						}

	    					}
	    				}
	    			}
	    		}
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	} finally {
	    		try {
					Thread.sleep(1000 * 30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
    	}
    }
}
