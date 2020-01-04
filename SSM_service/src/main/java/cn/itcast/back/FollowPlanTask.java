package cn.itcast.back;

import cn.itcast.service.OrderService;

public class FollowPlanTask implements Runnable {

	private OrderService orderService;
	private Integer id;
	private String symbol;
	private String first;
	private String second;
	private String third;
	private String stop;
	private String trigger;
	private Integer compare;
	private Integer uid;
	private String apiKey;
	private String secretKey;
	
	
	public FollowPlanTask(OrderService orderService, Integer id, String symbol, String first, String second,
			String third, String stop, String trigger, Integer compare, Integer uid, String apiKey, String secretKey) {
		super();
		this.orderService = orderService;
		this.id = id;
		this.symbol = symbol;
		this.first = first;
		this.second = second;
		this.third = third;
		this.stop = stop;
		this.trigger = trigger;
		this.compare = compare;
		this.uid = uid;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
	}


	@Override
	public void run() {
		try {
			orderService.follow(id, symbol, first, second, third, stop, trigger, compare, uid, apiKey, secretKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
