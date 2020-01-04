package cn.itcast.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.itcast.dao.PlanMapper;
import cn.itcast.pojo.Plan;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;

public class DealState1 implements Runnable {

    @Autowired
    private PlanMapper planMapper;
    
    @Autowired
    private OrderService orderService;
    
    private static final Logger logger = LoggerFactory.getLogger(DealState1.class);
    
    public DealState1() {
    	new Thread(this).start();
    }
    
	@Override
	public void run() {
    	while(true) {
	    	try {
	    		List<Plan> plans = planMapper.findPlansByState("1");
	    		for(Plan plan : plans) {
	    			Float curPrice = ToolsUtils.getCurPriceByKey(plan.getSymbol());
	    			if(plan.getFirst() > plan.getSecond() && plan.getFirst() > curPrice) {
	    				planMapper.updatePlanById(plan.getId(), 2);
	    			} 
	    			if(plan.getFirst() < plan.getSecond() && plan.getFirst() < curPrice) {
	    				planMapper.updatePlanById(plan.getId(), 2);
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
