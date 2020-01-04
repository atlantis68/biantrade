package cn.itcast.back;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
	
	public static void execute(Runnable runnable) {
		fixedThreadPool.execute(runnable);
	}
}
