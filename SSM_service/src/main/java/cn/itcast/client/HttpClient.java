package cn.itcast.client;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class HttpClient {

	private static OkHttpClient.Builder builder = new OkHttpClient.Builder()
	    .readTimeout(10, TimeUnit.SECONDS)
	    .writeTimeout(10, TimeUnit.SECONDS)
	    .connectTimeout(5, TimeUnit.SECONDS)
	    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)))
		.sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
		.hostnameVerifier(SSLSocketClient.getHostnameVerifier());
    
    public static OkHttpClient okHttpClient = builder.build();

	public static String baseUrl = "https://fapi.binance.com"; 
	
	public static String advancedUrl = "https://api.binance.com";
}
