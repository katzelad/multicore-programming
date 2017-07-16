import mpp.Ex4q7;

public class MppRunner {

	public static void main(String[] args) throws Exception {

		for (int numThreads : new int[] { 1, 2, 4, 8, 16, 32, 64 }) {

			System.out.print(numThreads + " threads: ");
			Ex4q7.main(new String[] { Integer.toString(numThreads), "/tmp/random-graph.txt" });

		}

	}

}
