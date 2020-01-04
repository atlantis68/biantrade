package cn.itcast.utils;

import java.util.HashMap;
import java.util.Map;

public class ToolsUtils {

	private static Map<String, Float> curPrice = new HashMap<String, Float>();

	public static Map<String, Float> getCurPrice() {
		return curPrice;
	}
	
	public static Float getCurPriceByKey(String key) {
		return curPrice.get(key);
	}

	public static void setCurPrice(Map<String, Float> curPrice) {
		ToolsUtils.curPrice = curPrice;
	}

	public static void setCurPrice(String key, Float value) {
		ToolsUtils.curPrice.put(key, value);
	}
}
