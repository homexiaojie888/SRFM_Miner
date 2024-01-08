import old.AlgoEMSFUI_B;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Thanks to the SPMF library for providing the datasets and codes of the compared algorithms.
 */
public class MainEMSFUI_B {
	static List<Double> runTime=new ArrayList<>();
	static List<Double> memory=new ArrayList<>();
	static List<Long> candidates=new ArrayList<>();
	static List<Long> jointCount=new ArrayList<>();
	static List<Integer> patterns=new ArrayList<>();

	public static void main(String [] arg) throws IOException, InterruptedException {

		String[] strInput={"DB_Utility2.txt"};

		int[][] DataSize={{6}};

		for (int i = 0; i < 1; i++) {
			String input = fileToPath(strInput[i]);
			String output = ".//Patterns_EMSFUI_B_" + strInput[i];
			for (int j = 0; j < DataSize[i].length; j++) {
				MemoryLogger.getInstance().reset();
				System.gc();
//				Thread.sleep(10000);
				AlgoEMSFUI_B EMSFUI_B = new AlgoEMSFUI_B();
				EMSFUI_B.runAlgorithm(input,DataSize[i][j], output);
				EMSFUI_B.printStats(runTime, memory, candidates, jointCount, patterns);
			}
			OutputExp(DataSize[i],strInput[i]);
			runTime.clear();
			memory.clear();
			candidates.clear();
			jointCount.clear();
			patterns.clear();
		}

	}
	private static void OutputExp(int[] DataSize, String input) throws IOException {
		String experimentFile = ".//Exp_EMSFUI_B_"+input;
		BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(experimentFile));
		bufferedWriter.write("DataSize: ");
		for (int i = 0; i < DataSize.length; i++) {
			if (i==DataSize.length-1){
				bufferedWriter.write(String.valueOf(DataSize[i]));
			}else {
				bufferedWriter.write(DataSize[i]+",");
			}

		}
		bufferedWriter.newLine();
		bufferedWriter.write("Runtime (s): ");
		for (int i = 0; i < DataSize.length; i++) {
			if (i==DataSize.length-1){
				bufferedWriter.write(runTime.get(i)+"");
			}else {
				bufferedWriter.write(runTime.get(i)+",");
			}

		}
		bufferedWriter.newLine();
		bufferedWriter.write("Memory (MB): ");
		for (int i = 0; i < DataSize.length; i++) {
			if (i==DataSize.length-1){
				bufferedWriter.write(memory.get(i)+"");
			}else {
				bufferedWriter.write(memory.get(i)+",");
			}

		}
		bufferedWriter.newLine();
		bufferedWriter.write("# candidates: ");
		for (int i = 0; i < DataSize.length; i++) {
			if (i==DataSize.length-1){
				bufferedWriter.write(candidates.get(i)+"");
			}else {
				bufferedWriter.write(candidates.get(i)+",");
			}

		}
		bufferedWriter.newLine();
		bufferedWriter.write("# jointCount: ");
		for (int i = 0; i < DataSize.length; i++) {
			if (i==DataSize.length-1){
				bufferedWriter.write(jointCount.get(i)+"");
			}else {
				bufferedWriter.write(jointCount.get(i)+",");
			}

		}
		bufferedWriter.newLine();
		bufferedWriter.write("# patterns: ");
		for (int i = 0; i < DataSize.length; i++) {
			if (i==DataSize.length-1){
				bufferedWriter.write(patterns.get(i)+"");
			}else {
				bufferedWriter.write(patterns.get(i)+",");
			}

		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainEMSFUI_B.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
