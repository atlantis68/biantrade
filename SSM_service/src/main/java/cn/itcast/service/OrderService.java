package cn.itcast.service;

import java.util.List;

import cn.itcast.model.Result;
import cn.itcast.pojo.Mail;
import cn.itcast.pojo.Plan;

public interface OrderService {
    
    public List<Plan> findPlanByUid(Integer uid);
    
    public List<Plan> findFllowPlans(Integer level);
    
    public Plan findPlanById(Integer id);
    
    public List<Plan> findPlansById(Integer id);
    
    public int updatePlanById(Plan plan);
    
    public int insertMail(Mail mail);
    
    public List<Plan> findPlanByTime(Integer time, Integer level);
    
	public Result generateAndDealOrder(String symbol, String first, String second, String third, String stop, 
			String trigger, Integer compare, String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, 
			List<String> orderIds, Float curPrice, boolean mock);
			
    public String plan(String symbol, String first, String second, String third, String stop, String trigger, Integer compare, 
    		String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, Float curPrice, Integer level);
    
    public String trade(String symbol, String side, String quantity, String price, String stopPrice, String type, 
    		String timeInForce, String workingType, String reduceOnly, String apiKey, String secretKey) throws Exception;
    
    public int cancelPlan(Integer uid, String symbol, String id, String orderIds, int state, String apiKey, String secretKey) throws Exception;
    
    public String cancel(String symbol, String orderId, String apiKey, String secretKey) throws Exception;
  
    public String follow(Integer id, String symbol, String first, String second, String third, String stop, String trigger, 
    		Integer compare, String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, Float curPrice) throws Exception;
    
    public String leverage(String symbol, int leverage, String apiKey, String secretKey) throws Exception;
    
    public String positionRisk(String apiKey, String secretKey) throws Exception;
    
    public int warn(String id) throws Exception;
}
