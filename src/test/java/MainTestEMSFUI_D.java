import old.AlgoMineEMSFUI_D;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Example of how to use the EMSFUI-D algorithm
 * Thanks to the SPMF library for providing the datasets and the compared algorithms' codes.
 */
public class MainTestEMSFUI_D {
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
			String output = ".//Patterns_EMSFUI_D_" + strInput[i];
			for (int j = 0; j < DataSize[i].length; j++) {
//				System.gc();
//				Thread.sleep(10000);
//				MemoryLogger.getInstance().reset();
				AlgoMineEMSFUI_D EMSFUI_D = new AlgoMineEMSFUI_D();
				EMSFUI_D.runAlgorithm(input,DataSize[i][j], output);
				EMSFUI_D.printStats(runTime, memory, candidates, jointCount, patterns);

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
		String experimentFile = ".//Exp_EMSFUI_D_"+input;
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
		URL url = MainTestEMSFUI_D.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
