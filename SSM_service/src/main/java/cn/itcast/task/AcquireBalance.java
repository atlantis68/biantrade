package cn.itcast.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.itcast.client.HttpClient;
import cn.itcast.client.SHA256;
import cn.itcast.dao.BalanceMapper;
import cn.itcast.pojo.Balance;
import cn.itcast.pojo.User;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class AcquireBalance implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AcquireBalance.class);
    
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static SimpleDateFormat format1 = new SimpleDateFormat("HH");
    
    @Autowired
    private BalanceMapper balanceMapper;
    
    public AcquireBalance() {
    	new Thread(this).start();
    }
    
	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while(true) {
    		if(format1.format(new Date()).equals("16")) {
    			try {
    				List<User> users = balanceMapper.findUserByStatus();
    				for(User user : users) {
    					try {
    						if(StringUtils.isNotEmpty(user.getSecretKey()) && StringUtils.isNotEmpty(user.getApiKey())) {
    							StringBuffer uri = new StringBuffer();
    							uri.append("timestamp=").append(System.currentTimeMillis());  		
    							String signature = SHA256.HMACSHA256(uri.toString().getBytes(), user.getSecretKey().getBytes());
    							uri.append("&signature=").append(signature);
    							Request request = new Request.Builder()
	    							.url(HttpClient.baseUrl + "/fapi/v1/balance?" + uri.toString())
	    							.header("X-MBX-APIKEY", user.getApiKey())
	    							.get().build();
    							Call call = HttpClient.okHttpClient.newCall(request);
    							Response response = call.execute();
    							String temp = response.body().string();
    							List<String> lists = JSON.parseArray(temp, String.class);
    							for(String accout : lists) {
    								JSONObject json = JSON.parseObject(accout);
    								String asset = json.getString("asset");
    								if(StringUtils.isNotEmpty(asset) && asset.toLowerCase().equals("usdt")) {
    									Balance balance = new Balance();
    									balance.setUid(user.getId());
    									balance.setBalance(json.getString("balance"));
    									balance.setUpdateTime(format.format(new Date()));
    									balanceMapper.insertBalance(balance);
    									break;
    								}
    							}
    						}
    					} catch(Exception e) {
    						e.printStackTrace();
    					}	
    				}
    			} catch(Exception e) {
    				e.printStackTrace();
    			} 
    		}
    		try {
    			Thread.sleep(1000 * 60 * 60);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    }
}
