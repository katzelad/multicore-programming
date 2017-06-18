package mpp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ex3q7 {

	private static interface PriorityQueue<T> {

		void add(T item, int score);

		T removeMin();

	}

	private static class LazyPriorityQueue<T> implements PriorityQueue<T> {

		private class Node {

			private final T item;
			private final int score;
			private volatile Node next;
			private volatile boolean deleted;
			private final Lock lock = new ReentrantLock();

			private Node() {
				item = null;
				score = 0;
			}

			private Node(T item, int score) {
				this.item = item;
				this.score = score;
			}

		}

		private final Node head = new Node();

		@Override
		public void add(T item, int score) {
			while (true) {
				Node pred = head, curr = pred.next;
				while (curr != null && curr.score < score)
					curr = (pred = curr).next;
				pred.lock.lock();
				try {
					if (curr == null) {
						if (!pred.deleted && pred.next == curr) {
							pred.next = new Node(item, score);
							return;
						}
					} else {
						curr.lock.lock();
						try {
							if (!pred.deleted && !curr.deleted && pred.next == curr) {
								(pred.next = new Node(item, score)).next = curr;
								return;
							}
						} finally {
							curr.lock.unlock();
						}
					}
				} finally {
					pred.lock.unlock();
				}
			}
		}

		@Override
		public T removeMin() {
			while (true) {
				Node min = head.next;
				if (min == null)
					return null;
				head.lock.lock();
				try {
					min.lock.lock();
					try {
						if (!min.deleted && head.next == min) {
							min.deleted = true;
							head.next = min.next;
							return min.item;
						}
					} finally {
						min.lock.unlock();
					}
				} finally {
					head.lock.unlock();
				}
			}
		}

		@Override
		public String toString() {
			return "Lazy priority queue";
		}

	}

	private static class LFPriorityQueue<T> implements PriorityQueue<T> {

		private class Node {

			private final T item;
			private final int score;
			private volatile AtomicMarkableReference<Node> next = new AtomicMarkableReference<Ex3q7.LFPriorityQueue<T>.Node>(
					null, false);

			private Node() {
				item = null;
				score = 0;
			}

			private Node(T item, int score) {
				this.item = item;
				this.score = score;
			}

		}

		private final Node head = new Node();

		@Override
		public void add(T item, int score) {
			retry: while (true) {
				Node pred = head, curr = pred.next.getReference();
				last: while (curr != null) {
					boolean[] deleted = { false };
					Node succ = curr.next.get(deleted);
					while (deleted[0]) {
						if (!pred.next.compareAndSet(curr, succ, false, false))
							continue retry;
						curr = succ;
						if (curr == null)
							break last;
						succ = curr.next.get(deleted);
					}
					if (curr.score >= score)
						break;
					pred = curr;
					curr = succ;
				}
				Node node = new Node(item, score);
				node.next = new AtomicMarkableReference<>(curr, false);
				if (pred.next.compareAndSet(curr, node, false, false))
					return;
			}
		}

		@Override
		public T removeMin() {
			retry: while (true) {
				Node pred = head, curr = pred.next.getReference();
				if (curr == null)
					return null;
				boolean[] deleted = { false };
				Node succ = curr.next.get(deleted);
				while (deleted[0]) {
					if (!pred.next.compareAndSet(curr, succ, false, false))
						continue retry;
					curr = succ;
					if (curr == null)
						return null;
					succ = curr.next.get(deleted);
				}
				if (!curr.next.attemptMark(succ, true))
					continue;
				pred.next.compareAndSet(curr, succ, false, false);
				return curr.item;
			}
		}

		@Override
		public String toString() {
			return "Lock-free priority queue";
		}

	}

	static volatile boolean done;
	public static Map<String, Double> benchmark = new HashMap<>();

	public static void main(String[] args) throws InterruptedException {

		class CounterThread extends Thread {

			PriorityQueue<Object> queue;
			int count;

			public CounterThread(PriorityQueue<Object> queue) {
				this.queue = queue;
			}

			@Override
			public void run() {
				Random rnd = new Random();
				for (count = 0; !done; count++) {
					queue.add(new Object(), rnd.nextInt());
					queue.removeMin();
				}
			}

		}

		int numThreads = Integer.parseInt(args[0]);
		CounterThread[] threads = new CounterThread[numThreads];
		List<PriorityQueue<Object>> queues = Arrays.asList(new LazyPriorityQueue<Object>(),
				new LFPriorityQueue<Object>());

		for (PriorityQueue<Object> queue : queues) {
			for (int i = 0; i < numThreads; i++)
				threads[i] = new CounterThread(queue);
			done = false;
			for (int i = 0; i < numThreads; i++)
				threads[i].start();
			Thread.sleep(10000);
			done = true;
			int count = 0;
			for (int i = 0; i < numThreads; i++) {
				threads[i].join();
				count += threads[i].count;
			}
			System.out.printf("%s: %.1f operations per second\n", queue, count / 10.);
			benchmark.put(queue.toString(), count / 10.);
		}

	}

}
