package mpp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ex4q7 {

	private static class Edge {

		private final int weight;
		private final int dest;

		private Edge(int weight, int dest) {
			this.weight = weight;
			this.dest = dest;
		}

	}

	private static class Node {

		private final int id;
		private final List<Edge> edges = new ArrayList<>();

		private Node(int id) {
			this.id = id;
		}

		private void addEdge(Edge edge) {
			edges.add(edge);
		}

	}

	public static void main(String[] args) throws Exception {

		final String[] lines = Files.readAllLines(Paths.get(args[1])).toArray(new String[0]);
		final int numThreads = Integer.parseInt(args[0]);
		final String[] header = lines[0].split(" ");
		final int numNodes = Integer.parseInt(header[2]), numEdges = Integer.parseInt(header[3]);
		final List<List<Node>> table = new ArrayList<>(numNodes);
		final List<Lock> locks = new ArrayList<>(numNodes);
		for (int i = 0; i < numNodes; i++) {
			table.add(new ArrayList<>());
			locks.add(new ReentrantLock());
		}

		class GraphBuilderThread extends Thread {

			private final int from, to;

			public GraphBuilderThread(int from, int to) {
				this.from = from;
				this.to = to;
			}

			@Override
			public void run() {
				for (int i = from; i < to; i++) {
					String[] line = lines[i].split(" ");
					int src = Integer.parseInt(line[1]), dst = Integer.parseInt(line[2]),
							weight = Integer.parseInt(line[3]), hc = Integer.hashCode(src) % numNodes;
					Lock lock = locks.get(hc);
					lock.lock();
					try {
						List<Node> bucket = table.get(hc);
						Node srcNode = null;
						for (Node node : bucket)
							if (node.id == src) {
								srcNode = node;
								break;
							}
						if (srcNode == null) {
							srcNode = new Node(src);
							bucket.add(srcNode);
						}
						srcNode.addEdge(new Edge(weight, dst));
					} finally {
						lock.unlock();
					}
				}
			}

		}

		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++)
			threads[i] = new GraphBuilderThread(numEdges * i / numThreads + 1, numEdges * (i + 1) / numThreads + 1);
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < numThreads; i++)
			threads[i].start();
		for (int i = 0; i < numThreads; i++)
			threads[i].join();
		long endTime = System.currentTimeMillis();
		
		System.out.println("Elapsed time: " + (endTime - startTime) + "ms");

	}

}
