package mpp;

import java.util.concurrent.atomic.AtomicInteger;

public class Ex1q8 {

	private static AtomicInteger counter;

	public static void main(String[] args) throws InterruptedException {

		int numThreads = Integer.parseInt(args[0]);
		Thread[] threads = new Thread[numThreads];
		counter = new AtomicInteger();

		for (int i = 0; i < numThreads; i++)
			threads[i] = new Thread() {
				@Override
				public void run() {
					for (int j = 0; j < 1000000; j++)
						counter.getAndIncrement();
				}
			};

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numThreads; i++)
			threads[i].start();
		for (int i = 0; i < numThreads; i++)
			threads[i].join();
		long endTime = System.currentTimeMillis();
		System.out.println("Counter: " + counter + "\nElapsed time: " + (endTime - startTime));

	}

}
