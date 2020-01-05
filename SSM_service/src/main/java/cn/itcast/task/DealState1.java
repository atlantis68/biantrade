package cn.itcast.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.itcast.dao.MailMapper;
import cn.itcast.dao.PlanMapper;
import cn.itcast.pojo.Mail;
import cn.itcast.pojo.Plan;
import cn.itcast.utils.ToolsUtils;

public class DealState1 implements Runnable {

    @Autowired
    private PlanMapper planMapper;
    
    @Autowired
    private MailMapper mailMapper;
    
    private static final Logger logger = LoggerFactory.getLogger(DealState1.class);
    
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DealState1() {
    	new Thread(this).start();
    }
    
	@Override
	public void run() {
    	while(true) {
	    	try {
	    		List<Plan> plans = planMapper.findPlansByState("1");
	    		for(Entry<String, Float> entry : ToolsUtils.getCurPrice().entrySet()) {
	    			String symbol = entry.getKey();
	    			Float curPrice = entry.getValue();
	    			for(Plan plan : plans) {
	    				if(plan.getSymbol().equals(symbol)) {
	    					if(plan.getFirst() > plan.getSecond() && plan.getFirst() > curPrice) {
	    						planMapper.updatePlanById(plan.getId(), 2);
	    						Mail mail = new Mail();
	    						mail.setUid(plan.getUid());
	    						mail.setSymbol(plan.getSymbol());
	    						mail.setSubject(plan.getSymbol() + "计划单（多单）" + plan.getFirst() + "满足第一档价格" + plan.getFirst() + "大于当前价格" + curPrice + "，预估已成交");
	    						mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
	    								+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());
	    						mail.setState(0);
	    						mail.setCreateTime(format.format(new Date()));
	    						mail.setUpdateTime(format.format(new Date()));
	    						mailMapper.insertMail(mail);
	    					} 
	    					if(plan.getFirst() < plan.getSecond() && plan.getFirst() < curPrice) {
	    						planMapper.updatePlanById(plan.getId(), 2);
	    						Mail mail = new Mail();
	    						mail.setUid(plan.getUid());
	    						mail.setSymbol(plan.getSymbol());
	    						mail.setSubject(plan.getSymbol() + "计划单（空单）" + plan.getFirst() + "满足第一档价格" + plan.getFirst() + "小于于当前价格" + curPrice + "，预估已成交");
	    						mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
	    								+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());
	    						mail.setState(0);
	    						mail.setCreateTime(format.format(new Date()));
	    						mail.setUpdateTime(format.format(new Date()));
	    						mailMapper.insertMail(mail);
	    					} 
	    				}
	    			}
	    		}
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	} finally {
	    		try {
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
    	}
    }
}
