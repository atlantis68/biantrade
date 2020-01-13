package cn.itcast.back;

import cn.itcast.service.OrderService;

public class CancelPlanTask implements Runnable {

	private OrderService orderService;
	private Integer uid;
	private Integer id;
	private String symbol;
	private String orderIds;
	private Integer state;
	private String apiKey;
	private String secretKey;

	public CancelPlanTask(OrderService orderService, Integer uid, Integer id, String symbol, String orderIds, Integer state, String apiKey,
			String secretKey) {
		super();
		this.uid = uid;
		this.orderService = orderService;
		this.id = id;
		this.symbol = symbol;
		this.orderIds = orderIds;
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.state = state;
	}

	@Override
	public void run() {
		try {
			orderService.cancelPlan(uid, symbol, id.toString(), orderIds, state, apiKey, secretKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
