package cn.itcast.task;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import cn.itcast.client.HttpClient;
import cn.itcast.utils.ToolsUtils;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class GetLatestPrice implements Runnable {

    private Request request;
    
    private static final Logger logger = LoggerFactory.getLogger(GetLatestPrice.class);
    
    public GetLatestPrice() {
    	request = new Request.Builder()
			.url(HttpClient.baseUrl + "/fapi/v1/ticker/price")
			.get().build();
    	new Thread(this).start();
    }
    
	@Override
	public void run() {
    	while(true) {
	    	try {
				Call call = HttpClient.okHttpClient.newCall(request);
    			Response response = call.execute();
	    		String temp = response.body().string();
	    		List<Map<String, String>> prices = JSON.parseObject(temp, new TypeReference<List<Map<String, String>>>(){} );
	    		for(Map<String, String> price : prices) {
	    			ToolsUtils.setCurPrice(price.get("symbol").toString(), Float.parseFloat(price.get("price").toString()));
	    		}
				logger.info("{}", ToolsUtils.getCurPrice());
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    	} finally {
	    		try {
					Thread.sleep(1000 * 3);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
    	}
    }
}
