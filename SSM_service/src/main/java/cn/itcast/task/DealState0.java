package cn.itcast.task;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.itcast.dao.PlanMapper;
import cn.itcast.pojo.Plan;
import cn.itcast.service.OrderService;
import cn.itcast.utils.ToolsUtils;

public class DealState0 implements Runnable {

    @Autowired
    private PlanMapper planMapper;
    
    @Autowired
    private OrderService orderService;
    
    private static final Logger logger = LoggerFactory.getLogger(DealState0.class);
    
    public DealState0() {
    	new Thread(this).start();
    }
    
	@Override
	public void run() {
    	while(true) {
	    	try {
	    		List<Plan> plans = planMapper.findPlansByState("0");
	    		for(Plan plan : plans) {
	    			Float curPrice = ToolsUtils.getCurPriceByKey(plan.getSymbol());
	    			List<String> orderIds = new ArrayList<String>();
	    			if(plan.getCompare() == 0 && curPrice > plan.getTrigger()) {
	    				int status = orderService.generateAndDealOrder(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
	    						plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), plan.getUid(), 
	    						plan.getCreateTime(), plan.getUpdateTime(), orderIds);
	    				if(status == 1) {
	    					planMapper.updatePlanById(plan.getId(), 1);
	    				} else {
	    					planMapper.updatePlanById(plan.getId(), 4);
	    				}
	    				
	    			} else if(plan.getCompare() == 1 && curPrice < plan.getTrigger()) {
	    				int status = orderService.generateAndDealOrder(plan.getSymbol(), plan.getFirst().toString(), plan.getSecond().toString(), 
	    						plan.getThird().toString(), plan.getStop().toString(), plan.getTrigger().toString(), plan.getCompare(), plan.getUid(), 
	    						plan.getCreateTime(), plan.getUpdateTime(), orderIds);
	    				if(status == 1) {
	    					planMapper.updatePlanById(plan.getId(), 1);
	    				} else {
	    					planMapper.updatePlanById(plan.getId(), 4);
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
