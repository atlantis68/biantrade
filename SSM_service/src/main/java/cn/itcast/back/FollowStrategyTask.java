package cn.itcast.back;

import cn.itcast.service.OrderService;

public class FollowStrategyTask implements Runnable {

	private OrderService orderService;
	private String symbol;
	private String side;
	private Integer uid;
	private String apiKey;
	private String secretKey;
	private Integer id;
	
	
	public FollowStrategyTask(OrderService orderService, String symbol, String side, Integer uid, String apiKey, String secretKey, Integer id) {
		super();
		this.orderService = orderService;
		this.symbol = symbol;
		this.side = side;
		this.uid = uid;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.id = id;
	}


	@Override
	public void run() {
		try {
			orderService.strategyMarket(symbol, side, uid, apiKey, secretKey, null, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
