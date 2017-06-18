package mpp;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.*;
import java.lang.Thread;
import java.util.LinkedList;

@SuppressWarnings("all")
public class Ex2q7 {

	public enum QueueOperationEnum {
		ENQUEUE, DEQUEUE, TOP, IS_EMPTY
	}

	public interface Consensus<T> {
		T decide(T value);
	}

	public interface IApplyable {
		Object apply(Operation operation);
	}

	public interface ICopyable {
		ICopyable copy();
	}

	public interface Operation<T> {
		Object apply(T ds);
	}

	public static class QueueOperation<T> implements Operation<MyQueue<T>> {

		private QueueOperationEnum operation;
		private T arg;

		public QueueOperation(QueueOperationEnum operation) {
			this.operation = operation;
		}

		public QueueOperation(QueueOperationEnum operation, T arg) {
			this(operation);
			this.arg = arg;
		}

		public Object apply(MyQueue queue) {
			switch (operation) {
			case ENQUEUE:
				return queue.enqueue(arg);

			case DEQUEUE:
				return queue.dequeue();

			case TOP:
				return queue.top();

			case IS_EMPTY:
				return queue.isEmpty();

			default:
				return null;

			}
		}
	}

	public static class LockFreeQueue<T> implements IApplyable {
		private GCEnabledUniversal multiThreadedQueue;

		public LockFreeQueue(int numOfThreads) {
			multiThreadedQueue = new GCEnabledUniversal(new MyQueue<>(), numOfThreads);
		}

		public Object apply(Operation operation) {
			return multiThreadedQueue.apply(operation);
		}
	}

	public static class MyConsensus<T> implements Consensus<T> {
		private AtomicReference<T> atomic;

		public MyConsensus() {
			atomic = new AtomicReference<T>(null);
		}

		public T decide(T value) {
			atomic.compareAndSet(null, value);
			return atomic.get();
		}
	}

	public static class MyQueue<T> implements ICopyable {
		private LinkedList<T> list;

		public MyQueue() {
			list = new LinkedList<>();
		}

		public MyQueue(LinkedList<T> list) {
			this.list = list;
		}

		public MyQueue copy() {
			LinkedList<T> newList = new LinkedList<>();
			newList.addAll(list);
			return new MyQueue(newList);
		}

		public boolean enqueue(T input) {
			try {
				list.add(input);
				return true;
			} catch (Throwable o) {
				return false;
			}
		}

		public Object dequeue() {
			try {
				T number = list.remove(0);
				return number;
				// return list.remove(0);
			} catch (Throwable o) {
				return null;
			}
		}

		public Object top() {
			try {
				return list.get(0);
			} catch (Throwable o) {
				return null;
			}
		}

		public boolean isEmpty() {
			return (list.size() == 0);
		}
	}

	public static class MyThread extends Thread {
		private long iterations;
		private IApplyable queue;
		private volatile Boolean isFinished;

		public MyThread(IApplyable queue, Boolean isFinished) {
			iterations = 0;
			this.queue = queue;
			this.isFinished = isFinished;
		}

		public void run() {
			QueueOperation<Integer> operation;
			Random rn = new Random();
			int number;
			while (!isFinished) {
				if (iterations % 2 == 0) { // even iteration
					number = rn.nextInt(10);
					operation = new QueueOperation<>(QueueOperationEnum.ENQUEUE, number);
					queue.apply(operation);
				} else { // odd iteration
					operation = new QueueOperation<>(QueueOperationEnum.DEQUEUE);
					queue.apply(operation);
				}
				iterations++;
			}
		}

		public long getIterations() {
			return iterations;
		}
	}

	public static class Node {
		public Operation operation; // method name and args
		public Consensus<Node> decideNext; // decide next mpp.Node in list
		public Node next; // the next node
		public int seq; // sequence number
		public int[] seenBy; // seenBy[i] == 1 iff the node was seen by thread i

		public Node(Operation operation, int numOfThreads) {
			this.operation = operation;
			decideNext = new MyConsensus<Node>();
			seq = 0;
			seenBy = new int[numOfThreads];
			for (int i = 0; i < numOfThreads; i++)
				seenBy[i] = 0;
		}

		public boolean isSeenByAll() {
			for (int i : seenBy)
				if (i == 0)
					return false;
			return true;
		}

		public static Node max(Node[] array) {
			Node max = array[0];
			for (int i = 1; i < array.length; i++)
				if (max.seq < array[i].seq)
					max = array[i];
			return max;
		}

		public static Node min(Node[] array) {
			Node min = array[0];
			for (int i = 1; i < array.length; i++)
				if (min.seq > array[i].seq)
					min = array[i];
			return min;
		}
	}

	public static class DeadlockFreeQueue<T> implements IApplyable {
		private MyQueue<T> queue;
		private Lock lock;

		public DeadlockFreeQueue() {
			queue = new MyQueue<T>();
			lock = new ReentrantLock();
		}

		public Object apply(Operation operation) {
			try {
				lock.lock();
				return operation.apply(queue);
			} finally {
				lock.unlock();
				return null;
			}
		}
	}

	public static class GCEnabledUniversal {
		int numOfThreads;
		private Node[] head;
		private Node tail;
		private ICopyable[] states;

		public GCEnabledUniversal(ICopyable initialState, int n) { // n is the
																	// number of
																	// threads
			numOfThreads = n;
			head = new Node[numOfThreads];
			tail = new Node(null, numOfThreads);
			tail.seq = 1;
			for (int i = 0; i < n; i++)
				head[i] = tail;
			states = new ICopyable[numOfThreads];
			for (int i = 0; i < n; i++)
				states[i] = initialState.copy();
		}

		public Object apply(Operation operation) {
			int i = Integer.parseInt(Thread.currentThread().getName());
			Node prefer = new Node(operation, numOfThreads);

			Node current = head[i];

			while (prefer.seq == 0) {
				Node before = Node.max(head);
				Node after = before.decideNext.decide(prefer);
				before.next = after;
				after.seq = before.seq + 1;
				head[i] = after;
			}

			ICopyable myObject = states[i];
			current = current.next;

			while (current != prefer) {
				current.operation.apply(myObject);
				current = current.next;
			}
			current.operation.apply(myObject);
			states[i] = myObject;
			tail = Node.min(head);
			return myObject;
		}
	}

	private static Boolean isFinished;
	private static final int NUM_OF_SECONDS = 10;
	private static int totalIterations = 0;

	public static void main(String[] args) throws InterruptedException {
		int numOfThreads = Integer.parseInt(args[0]);
		int implementationNumber = Integer.parseInt(args[1]);
		MyThread[] myThreads = new MyThread[numOfThreads];

		isFinished = false;
		IApplyable queue = (implementationNumber == 1) ? new LockFreeQueue<Integer>(numOfThreads)
				: new DeadlockFreeQueue<Integer>();

		for (int i = 0; i < numOfThreads; i++) {
			myThreads[i] = new MyThread(queue, isFinished);
			myThreads[i].setName(String.valueOf(i));
		}

		for (int i = 0; i < numOfThreads; i++)
			myThreads[i].start();
		Thread.sleep(NUM_OF_SECONDS * 1000);
		isFinished = true;
		for (MyThread myThread : myThreads) {
			totalIterations += myThread.getIterations();
			myThread.interrupt();
		}

		float throughput = totalIterations / NUM_OF_SECONDS;
		System.out.println("Throughput for implementation number " + implementationNumber + " and " + numOfThreads
				+ " threads is " + throughput);
	}
}
