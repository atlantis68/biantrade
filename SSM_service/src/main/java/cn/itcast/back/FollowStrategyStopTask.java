package cn.itcast.back;

import cn.itcast.service.OrderService;

public class FollowStrategyStopTask implements Runnable {

	private OrderService orderService;
	private String symbol;
	private String side;
	private String stopPrice;
	private Integer uid;
	private String apiKey;
	private String secretKey;
	private String id;
	
	
	public FollowStrategyStopTask(OrderService orderService, String symbol, String side, String stopPrice, 
			Integer uid, String apiKey, String secretKey, String id) {
		super();
		this.orderService = orderService;
		this.symbol = symbol;
		this.side = side;
		this.stopPrice = stopPrice;
		this.uid = uid;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.id = id;
	}


	@Override
	public void run() {
		try {
			orderService.strategyStop(symbol, side, stopPrice, uid, id, apiKey, secretKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
