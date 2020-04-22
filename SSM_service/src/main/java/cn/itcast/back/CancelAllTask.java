package cn.itcast.back;

import cn.itcast.service.OrderService;

public class CancelAllTask implements Runnable {

	private OrderService orderService;
	private Integer uid;
	private String symbol;
	private String apiKey;
	private String secretKey;

	public CancelAllTask(OrderService orderService, Integer uid, String symbol, String apiKey, String secretKey) {
		super();
		this.uid = uid;
		this.orderService = orderService;
		this.symbol = symbol;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
	}

	@Override
	public void run() {
		try {
			orderService.cancelAll(symbol, uid, apiKey, secretKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
