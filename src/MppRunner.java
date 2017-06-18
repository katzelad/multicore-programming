
import java.util.HashMap;
import java.util.Map;

import mpp.*;

public class MppRunner {

	public static void main(String[] args) throws InterruptedException {

		final int NUM_OF_BENCHMARKS = 10;

		for (int i : new int[] { 1, 2, 4, 8, 16, 32 }) {
			String[] arg = new String[] { String.valueOf(i) };
			System.out.println("Question 7, " + i + " threads:");
			Map<String, Double> benchmark = new HashMap<>();
			for (int j = 0; j < NUM_OF_BENCHMARKS; j++) {
				Ex3q7.main(arg);
				for (String queue : Ex3q7.benchmark.keySet()) {
					if (benchmark.containsKey(queue))
						benchmark.put(queue, benchmark.get(queue) + Ex3q7.benchmark.get(queue));
					else
						benchmark.put(queue, Ex3q7.benchmark.get(queue));
				}
			}
			for (String queue : benchmark.keySet())
				System.out.printf("%s: average of %.1f operations per second\n", queue,
						benchmark.get(queue) / NUM_OF_BENCHMARKS);
			System.out.println("Question 8, " + i + " threads:");
			benchmark.clear();
			for (int j = 0; j < NUM_OF_BENCHMARKS; j++) {
				Ex3q8.main(arg);
				for (String lock : Ex3q8.benchmark.keySet()) {
					if (benchmark.containsKey(lock))
						benchmark.put(lock, benchmark.get(lock) + Ex3q8.benchmark.get(lock));
					else
						benchmark.put(lock, Ex3q8.benchmark.get(lock));
				}
			}
			for (String lock : benchmark.keySet())
				System.out.printf("%s: average of %.1f ms per million iterations\n", lock,
						benchmark.get(lock) / NUM_OF_BENCHMARKS);
		}

	}

}
