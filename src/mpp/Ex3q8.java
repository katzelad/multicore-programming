package mpp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Ex3q8 {

	private static interface Lock {

		void lock() throws InterruptedException;

		void unlock();

	}

	private static class CLH implements Lock {

		private class Node {
			private volatile boolean locked;
		}

		private AtomicReference<Node> tail = new AtomicReference<>(new Node());
		private ThreadLocal<Node> pred = new ThreadLocal<Node>() {
			@Override
			protected Node initialValue() {
				return null;
			}
		}, curr = new ThreadLocal<Node>() {
			@Override
			protected Node initialValue() {
				return new Node();
			}
		};

		@Override
		public void lock() {
			Node node = curr.get();
			node.locked = true;
			Node old = tail.getAndSet(node);
			pred.set(old);
			while (old.locked)
				;
		}

		@Override
		public void unlock() {
			curr.get().locked = false;
			curr.set(pred.get());
		}

		@Override
		public String toString() {
			return "CLH lock";
		}

	}

	private static class BackoffLock implements Lock {

		private final int minDelay = 1, maxDelay;
		private AtomicBoolean state = new AtomicBoolean();
		private Random rnd = new Random();

		public BackoffLock(int maxDelay) {
			this.maxDelay = maxDelay;
		}

		@Override
		public void lock() throws InterruptedException {
			int delay = minDelay;
			while (true) {
				while (state.get())
					;
				if (!state.getAndSet(true))
					return;
				Thread.sleep(rnd.nextInt(delay));
				if (delay < maxDelay)
					delay *= 2;
			}
		}

		@Override
		public void unlock() {
			state.set(false);
		}

		@Override
		public String toString() {
			return "Backoff lock (maximal delay of " + this.maxDelay + " ms)";
		}

	}

	private static int counter;
	public static Map<String, Double> benchmark = new HashMap<>();

	public static void main(String[] args) throws InterruptedException {

		int numThreads = Integer.parseInt(args[0]);
		Thread[] threads = new Thread[numThreads];

		for (Lock lock : new Lock[] { new CLH(), new BackoffLock(4), new BackoffLock(64) }) {
			counter = 0;
			for (int i = 0; i < numThreads; i++)
				threads[i] = new Thread() {
					@Override
					public void run() {
						try {
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
						} catch (InterruptedException e) {
						}
					}
				};
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < numThreads; i++)
				threads[i].start();
			for (int i = 0; i < numThreads; i++)
				threads[i].join();
			long endTime = System.currentTimeMillis();
			double timePerMIterations = (endTime - startTime) * 1000000. / counter;
			System.out.printf("%s: %d iterations, %.1f ms per million iterations\n", lock, counter, timePerMIterations);
			benchmark.put(lock.toString(), timePerMIterations);
		}

	}

}
