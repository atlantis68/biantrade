package cn.itcast.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.itcast.dao.MailMapper;
import cn.itcast.dao.PlanMapper;
import cn.itcast.model.Result;
import cn.itcast.pojo.Mail;
import cn.itcast.pojo.Plan;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;

public class DealState0 implements Runnable {

    @Autowired
    private PlanMapper planMapper;
    
    @Autowired
    private MailMapper mailMapper;
    
    @Autowired
    private OrderService orderService;
    
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static final Logger logger = LoggerFactory.getLogger(DealState0.class);
    
    public DealState0() {
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
	    		List<Plan> plans = planMapper.findPlansByState("0");
	    		for(Entry<String, Float> entry : ToolsUtils.getCurPrice().entrySet()) {
	    			String symbol = entry.getKey();
	    			Float curPrice = entry.getValue();
	    			for(Plan plan : plans) {
	    				if(plan.getSymbol().equals(symbol)) {
	    	    			List<String> orderIds = new ArrayList<String>();
	    	    			if(plan.getCompare() == 0 && curPrice > plan.getTrigger()) {
	    	    				Result res = orderService.generateAndDealOrder(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
	    	    						plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), 
	    	    						plan.getTrigger1().toString(), plan.getCompare1(), plan.getUid(), plan.getCreateTime(), plan.getUpdateTime(), 
	    	    						orderIds, curPrice, false);
	    	    				String orders = "";
	    	    				if(res.getState() == 1) {
	    	    					for(String orderId : orderIds) {
	    	    						orders += orderId + ",";
	    	    					}
	    	    					orders = orders.substring(0, orders.length() - 1);
	    	    				}
	    	    				plan.setOrderIds(orders);
	    	    	        	plan.setState(res.getState() == 1 ? 1 : 3);
	    	    	        	plan.setUpdateTime(format.format(new Date()));
	    	    				planMapper.updatePlanById(plan);
	    	    				Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), null, null, 
	    	    						0, format.format(new Date()), format.format(new Date()));
	    	    				if(res.getState() == 1) {
	    	    					mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
	    	    							+ plan.getFirst() + "，被系统提交到币安");
	    	    					mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
	    	    							+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    	    					
	    	    				} else {
	    	    					mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
	    	    							+ "，提交到币安失败，异常编码：" + res.getState());
	    	    					mail.setContent("异常详情：" + res.getMsg());
	    	    				}
	    	    				mailMapper.insertMail(mail);
	    	    			} else if(plan.getCompare() == 1 && curPrice < plan.getTrigger()) {
	    	    				Result res = orderService.generateAndDealOrder(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
	    	    						plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), 
	    	    						plan.getTrigger1().toString(), plan.getCompare1(), plan.getUid(), plan.getCreateTime(), plan.getUpdateTime(), 
	    	    						orderIds, curPrice, false);
	    	    				String orders = "";
	    	    				if(res.getState() == 1) {
	    	    					for(String orderId : orderIds) {
	    	    						orders += orderId + ",";
	    	    					}
	    	    					orders = orders.substring(0, orders.length() - 1);
	    	    				}
	    	    				plan.setOrderIds(orders);
	    	    	        	plan.setState(res.getState() == 1 ? 1 : 3);
	    	    	        	plan.setUpdateTime(format.format(new Date()));
	    	    				planMapper.updatePlanById(plan);
	    	    				Mail mail = ToolsUtils.generateMail(plan.getUid(), plan.getSymbol(), null, null, 
	    	    						0, format.format(new Date()), format.format(new Date()));
	    	    				if(res.getState() == 1) {
	    	    					mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
	    	    							+ plan.getFirst() + "，被系统提交到币安");
	    	    					mail.setContent("计划单详情：第一档：" + plan.getFirst() + "，第二档：" + plan.getSecond() 
	    	    							+ "，第三档：" + plan.getThird() + "，止损档：" + plan.getStop());	    	    					
	    	    				} else {
	    	    					mail.setSubject(plan.getSymbol() + "计划单" + (plan.getThird() > plan.getStop() ? "（多单）" : "（空单）")
	    	    							+ "，提交到币安失败，异常编码：" + res.getState());
	    	    					mail.setContent("异常详情：" + res.getMsg());
	    	    				}
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
