package cn.itcast.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cn.itcast.pojo.Mail;

public class ToolsUtils {

	private static Map<String, Float> curPrice = new HashMap<String, Float>();

	private static List<Properties> platformMail;

	private static int offset;
	
	private static DecimalFormat decimalFormatFor5 = new DecimalFormat("0.00000");
	
	private static DecimalFormat decimalFormatFor4 = new DecimalFormat("0.0000");
	
	private static DecimalFormat decimalFormatFor3 = new DecimalFormat("0.000");
	
	private static DecimalFormat decimalFormatFor2 = new DecimalFormat("0.00");
	
	private static DecimalFormat decimalFormatFor1 = new DecimalFormat("0.0");
	
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
	
	public static String formatQuantity(String symbol, Float value) {
		String result;
		if(symbol.toUpperCase().equals("TRXUSDT")) {
			result = "" + (value.intValue());
		} else if(symbol.toUpperCase().equals("XRPUSDT") || symbol.toUpperCase().equals("EOSUSDT")) {
			result = decimalFormatFor1.format(value);
		} else {
			result = decimalFormatFor3.format(value);
		}
		return result;
	}
	
	public static String formatPrice(String symbol, Float value) {
		String result;
		if(symbol.toUpperCase().equals("TRXUSDT")) {
			result = decimalFormatFor5.format(value);
		} else if(symbol.toUpperCase().equals("XRPUSDT")) {
			result = decimalFormatFor4.format(value);
		} else if(symbol.toUpperCase().equals("EOSUSDT")) {
			result = decimalFormatFor3.format(value);
		} else {
			result = decimalFormatFor2.format(value);
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
}
