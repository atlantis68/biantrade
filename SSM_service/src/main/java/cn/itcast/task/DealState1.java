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
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while(true) {
	    	try {
	    		List<Plan> plans = planMapper.findPlansByState("1");
	    		for(Entry<String, Float> entry : ToolsUtils.getCurPrice().entrySet()) {
	    			String symbol = entry.getKey();
	    			Float curPrice = entry.getValue();
	    			for(Plan plan : plans) {
	    				if(plan.getSymbol().equals(symbol)) {
	    					if(plan.getThird() > plan.getStop() && getMax(plan.getFirst(), plan.getSecond(), plan.getThird()) > curPrice) {
	    	    	        	plan.setState(2);
	    	    	        	plan.setUpdateTime(format.format(new Date()));
	    	    				planMapper.updatePlanById(plan);
	    	    				Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), plan.getSymbol() + "计划单（多单）" 
	    	    						+ getMax(plan.getFirst(), plan.getSecond(), plan.getThird()) + "，预估已成交", 
	    	    						"计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
	    								+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop(), 
	    	    						0, format.format(new Date()), format.format(new Date()));
	    						mailMapper.insertMail(mail);
	    					} 
	    					if(plan.getThird() < plan.getStop() && getMin(plan.getFirst(), plan.getSecond(), plan.getThird()) < curPrice) {
	    	    	        	plan.setState(2);
	    	    	        	plan.setUpdateTime(format.format(new Date()));
	    	    				planMapper.updatePlanById(plan);
	    	    				Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), plan.getSymbol() + "计划单（空单）" 
	    	    						+ getMin(plan.getFirst(), plan.getSecond(), plan.getThird()) + "，预估已成交", 
	    	    						"计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
	    								+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop(), 
	    	    						0, format.format(new Date()), format.format(new Date()));
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
	
	private float getMax(Float first, Float second, Float third) {
		float max = first;
		if(second > max) {
			max = second;
		}
		if(third > max) {
			max = third;
		}
		return max;
	}
	
	private float getMin(Float first, Float second, Float third) {
		float min = first;
		if(second < min) {
			min = second;
		}
		if(third < min) {
			min = third;
		}
		return min;
	}
}
