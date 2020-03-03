package cn.itcast.task;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import cn.itcast.dao.PlanMapper;
import cn.itcast.pojo.Plan;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;

public class DealState2 implements Runnable {

    @Autowired
    private PlanMapper planMapper;
    
    @Autowired
    private OrderService orderService;

    public DealState2() {
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
	    		List<Plan> plans = planMapper.findPlansByState1();
	    		for(Entry<String, Float> entry : ToolsUtils.getCurPrice().entrySet()) {
	    			String symbol = entry.getKey();
	    			Float curPrice = entry.getValue();
	    			for(Plan plan : plans) {
	    				if(plan.getSymbol().equals(symbol)) {
	    	    			if(plan.getCompare1() == 0 && curPrice > plan.getTrigger1()) {
	    	    				orderService.cancelPlan(plan.getUid(), plan.getSymbol(), plan.getId().toString(), 
	    	    						plan.getOrderIds(), 4, plan.getCreateTime(), plan.getUpdateTime());
	    	    			} else if(plan.getCompare1() == 1 && curPrice < plan.getTrigger1()) {
	    	    				orderService.cancelPlan(plan.getUid(), plan.getSymbol(), plan.getId().toString(), 
	    	    						plan.getOrderIds(), 4, plan.getCreateTime(), plan.getUpdateTime());
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
