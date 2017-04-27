
import mpp.*;

public class MppRunner {

	public static void main(String[] args) throws InterruptedException {

		for (int i: new int[] {1, 4, 8, 16, 32}) {
			String[] arg = new String[] {String.valueOf(i)};
			System.out.println("Question 6, " + i + " threads:");
			Ex1q6.main(arg);
			System.out.println("Question 7, " + i + " threads:");
			Ex1q7.main(arg);
			System.out.println("Question 8, " + i + " threads:");
			Ex1q8.main(arg);
		}

	}

}
