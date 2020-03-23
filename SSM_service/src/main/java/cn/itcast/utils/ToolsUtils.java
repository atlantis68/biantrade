package cn.itcast.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cn.itcast.pojo.Mail;

public class ToolsUtils {

	private static Map<String, Float> curPrice = new HashMap<String, Float>();
	
	private static Map<String, String> userPositionAmt = new HashMap<String, String>();
	
	private static Map<String, String> userLeverage = new HashMap<String, String>();

	private static List<Properties> platformMail;

	private static int offset;
	
	static {
		platformMail = new ArrayList<Properties>();
		Properties qqProps = new Properties();
		qqProps.put("mail.smtp.host", "smtp.qq.com");
		qqProps.put("mail.smtp.auth", "true");
		qqProps.put("mail.smtps.timeout", 10000);
		qqProps.put("mail.smtps.connectiontimeout", 10000);
		qqProps.put("mail.smtp.port", "587");
		qqProps.put("mail.user", "346411799@qq.com");
		qqProps.put("mail.password", "jfbbcvqurpaycaga");
		platformMail.add(qqProps);
		Properties wyProps = new Properties();
		wyProps.put("mail.smtp.host", "smtp.163.com");
		wyProps.put("mail.smtp.auth", "true");
		wyProps.put("mail.smtps.timeout", 10000);
		wyProps.put("mail.smtps.connectiontimeout", 10000);
		wyProps.put("mail.user", "18980868096@163.com");
		wyProps.put("mail.password", "Atlantis68");
		platformMail.add(wyProps);
		Properties sinaProps = new Properties();
		sinaProps.put("mail.smtp.host", "smtp.sina.com");
		sinaProps.put("mail.smtp.auth", "true");
		sinaProps.put("mail.smtps.timeout", 10000);
		sinaProps.put("mail.smtps.connectiontimeout", 10000);
		sinaProps.put("mail.user", "atlantis68@sina.com");
		sinaProps.put("mail.password", "`1234567890-");
		platformMail.add(sinaProps);
		offset = 0;
	}
	
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
	
	public static Properties getRandomPlat() {
		return platformMail.get(offset++ % platformMail.size());
	}

	public static String getUserPositionAmt(String key) {
		return ToolsUtils.userPositionAmt.get(key);
	}
	
	public static void setUserPositionAmt(String key, String value) {
		ToolsUtils.userPositionAmt.put(key, value);
	}
	
	public static String getUserLeverage(String key) {
		return ToolsUtils.userLeverage.get(key);
	}
	
	public static void setUserLeverage(String key, String value) {
		ToolsUtils.userLeverage.put(key, value);
	}
	
	public static String formatQuantity(String symbol, Float value) {
		BigDecimal number = new BigDecimal(""+ value);
		String result;
		if(symbol.toUpperCase().equals("TRXUSDT") || symbol.toUpperCase().equals("XLMUSDT")
				|| symbol.toUpperCase().equals("ADAUSDT")) {
			result = number.setScale(0, BigDecimal.ROUND_DOWN).toString();
		} else if(symbol.toUpperCase().equals("XRPUSDT") || symbol.toUpperCase().equals("EOSUSDT")
				|| symbol.toUpperCase().equals("ETCUSDT") || symbol.toUpperCase().equals("LINKUSDT")
				|| symbol.toUpperCase().equals("BNBUSDT") || symbol.toUpperCase().equals("ATOMUSDT")) {
			result = number.setScale(1, BigDecimal.ROUND_DOWN).toString();
		} else {
			result = number.setScale(3, BigDecimal.ROUND_DOWN).toString();
		}
		return result;
	}
	
	public static String formatPrice(String symbol, Float value) {
		BigDecimal number = new BigDecimal(""+ value);
		String result;
		if(symbol.toUpperCase().equals("TRXUSDT") || symbol.toUpperCase().equals("XLMUSDT") 
				|| symbol.toUpperCase().equals("ADAUSDT")) {
			result = number.setScale(5, BigDecimal.ROUND_DOWN).toString();
		} else if(symbol.toUpperCase().equals("XRPUSDT")) {
			result = number.setScale(4, BigDecimal.ROUND_DOWN).toString();
		} else if(symbol.toUpperCase().equals("EOSUSDT") || symbol.toUpperCase().equals("ETCUSDT")
				|| symbol.toUpperCase().equals("LINKUSDT") || symbol.toUpperCase().equals("BNBUSDT")
				|| symbol.toUpperCase().equals("ATOMUSDT")) {
			result = number.setScale(3, BigDecimal.ROUND_DOWN).toString();
		} else {
			result = number.setScale(2, BigDecimal.ROUND_DOWN).toString();
		}
		return result;
	}
	
	public static Mail generateMail(Integer uid, String symbol, String subject, String content, 
			Integer state, String createTime, String updateTime) {
		Mail mail = new Mail();
		mail.setUid(uid);
		mail.setSymbol(symbol);
		mail.setSubject(subject);
		mail.setContent(content);
		mail.setState(state);
		mail.setCreateTime(createTime);
		mail.setUpdateTime(updateTime);
		return mail;
	}
	
	public static void main(String[] args) {
		System.out.println(formatQuantity("ETHUSDT", 61.97999201356768f));
		System.out.println(formatQuantity("ETCUSDT", 61.97999201356768f));
		System.out.println(formatQuantity("TRXUSDT", 61.97999201356768f));

		System.out.println(formatPrice("ETHUSDT", 61.97999201356768f));
		System.out.println(formatPrice("ATOMUSDT", 61.97999201356768f));
		System.out.println(formatPrice("XRPUSDT", 61.97999201356768f));
		System.out.println(formatPrice("TRXUSDT", 61.97999201356768f));
	}
}
