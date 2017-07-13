package mpp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

		private void add(Edge edge) {
			edges.add(edge);
		}

	}

	public static void main(String[] args) throws Exception {

		int numThreads = Integer.parseInt(args[0]);
		String[] lines = Files.readAllLines(Paths.get(args[1])).toArray(new String[0]);
		String[] header = lines[0].split(" ");
		int numNodes = Integer.parseInt(header[2]), numEdges = Integer.parseInt(header[3]);

	}

}
