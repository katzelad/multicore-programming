package mpp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mpp.Ex3q7.PriorityQueue;

public class Ex4q7 {

	private static class Edge {

		private final int weight;
		private final Node dest;

		private Edge(int weight, Node dest) {
			this.weight = weight;
			this.dest = dest;
		}

	}

	private static class Node {

		private final List<Edge> edges = new ArrayList<>();

		private void addEdge(Edge edge) {
			edges.add(edge);
		}

	}

	public static void main(String[] args) throws Exception {

		class GraphBuilderThread extends Thread {

			private final String[] lines;
			private final int from, to;

			public GraphBuilderThread(String[] lines, int from, int to) {
				this.lines = lines;
				this.from = from;
				this.to = to;
			}

			@Override
			public void run() {
				for (int i = from; i < to; i++) {
					String[] line = lines[i].split(" ");
					int src = Integer.parseInt(line[1]), dst = Integer.parseInt(line[2]),
							weight = Integer.parseInt(line[3]);
					Edge edge = new Edge(weight, dst);
					
				}
			}

		}

		int numThreads = Integer.parseInt(args[0]);
		String[] lines = Files.readAllLines(Paths.get(args[1])).toArray(new String[0]);
		String[] header = lines[0].split(" ");
		int numNodes = Integer.parseInt(header[2]), numEdges = Integer.parseInt(header[3]);

		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++)
			threads[i] = new Thread(numEdges * i / numThreads + 1, numEdges * (i + 1) / numThreads + 1) {
				@Override
				public void run() {
					for (int j = 0; j < 1000000; j++)
						counter.getAndIncrement();
				}
			};

	}

}
