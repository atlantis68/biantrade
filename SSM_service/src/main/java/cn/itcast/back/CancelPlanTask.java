package cn.itcast.back;

import cn.itcast.service.OrderService;

public class CancelPlanTask implements Runnable {

	private OrderService orderService;
	private Integer id;
	private String symbol;
	private String orderIds;
	private String apiKey;
	private String secretKey;

	public CancelPlanTask(OrderService orderService, Integer id, String symbol, String orderIds, String apiKey,
			String secretKey) {
		super();
		this.orderService = orderService;
		this.id = id;
		this.symbol = symbol;
		this.orderIds = orderIds;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
	}

	@Override
	public void run() {
		try {
			orderService.cancelPlan(symbol, id.toString(), orderIds, apiKey, secretKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
