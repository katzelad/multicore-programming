package mpp;

import java.util.concurrent.locks.ReentrantLock;

public class Ex1q7 {

	private static int counter;
	private static ReentrantLock lock = new ReentrantLock();

	public static void main(String[] args) throws InterruptedException {

		int numThreads = Integer.parseInt(args[0]);
		Thread[] threads = new Thread[numThreads];
		counter = 0;

		for (int i = 0; i < numThreads; i++)
			threads[i] = new Thread() {
				@Override
				public void run() {
					for (int j = 0; j < 1000000; j++) {
						lock.lock();
						try {
							int localCounter = counter;
							localCounter++;
							counter = localCounter;
						} finally {
							lock.unlock();
						}
					}
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
