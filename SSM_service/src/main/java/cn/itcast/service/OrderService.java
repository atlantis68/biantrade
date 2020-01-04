package cn.itcast.service;

import java.util.List;

import cn.itcast.pojo.Plan;

public interface OrderService {
    
    public List<Plan> findPlanByUid(Integer uid, String symbol);
    
    public List<Plan> findFllowPlans(String symbol);
    
    public Plan findPlanById(Integer id);
    
    public List<Plan> findPlansById(Integer id);
    
    public int updatePlanById(Integer id, Integer state);
    
	public int generateAndDealOrder(String symbol, String first, String second, String third, String stop, String trigger, 
			Integer compare, Integer uid, String apiKey, String secretKey, List<String> orderIds);
			
    public String plan(String symbol, String first, String second, String third, 
			String stop, String trigger, Integer compare, Integer uid, String apiKey, String secretKey);
    
    public String trade(String symbol, String side, String quantity, String price, String stopPrice, String type, 
    		String timeInForce, String workingType, String reduceOnly, String apiKey, String secretKey) throws Exception;
    
    public int cancelPlan(String symbol, String id, String orderIds, String apiKey, String secretKey) throws Exception;
    
    public String cancel(String symbol, String orderId, String apiKey, String secretKey) throws Exception;
  
    public String follow(Integer id, String symbol, String first, String second, String third, 
    		String stop, String trigger, Integer compare, Integer uid, String apiKey, String secretKey) throws Exception;
}
