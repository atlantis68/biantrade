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
	private String trigger1;
	private Integer compare1;
	private Integer uid;
	private String apiKey;
	private String secretKey;
	private Float curPrice;
	
	
	public FollowPlanTask(OrderService orderService, Integer id, String symbol, String first, String second, String third, String stop, 
			String trigger, Integer compare, String trigger1, Integer compare1, Integer uid, String apiKey, String secretKey, Float curPrice) {
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
		this.trigger1 = trigger1;
		this.compare1 = compare1;
		this.uid = uid;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.curPrice = curPrice;
	}


	@Override
	public void run() {
		try {
			orderService.follow(id, symbol, first, second, third, stop, trigger, compare, trigger1, compare1, uid, apiKey, secretKey, curPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
