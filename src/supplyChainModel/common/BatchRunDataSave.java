package supplyChainModel.common;

import java.util.HashMap;
import java.util.List;

public class BatchRunDataSave {
	
	private static int runNumber = 0;
	
	private static HashMap<Integer, Boolean> paramRisk = new HashMap<Integer, Boolean>();
	private static HashMap<Integer, Double> outputCocaineConsumed = new HashMap<Integer, Double>();
	
	public static void resetData() {
		
		if (runNumber > 0) {
			runNumber = 0;
			
			paramRisk.clear();
			outputCocaineConsumed.clear();
		}
	}

	public static void addData(boolean pParamRisk, double pOutputCocaineConsumed) {
		
		paramRisk.put(runNumber, pParamRisk);
		outputCocaineConsumed.put(runNumber, pOutputCocaineConsumed);
		runNumber ++;
	}
	
	public static void saveData(String filePathAndName, boolean pParamRisk, double pOutputCocaineConsumed) {
				
		Logger.logMain("Saving Batch Data " + runNumber + ": " + pParamRisk + ", " + pOutputCocaineConsumed);
		List<String> data = SU.readFile(filePathAndName);
		
		String datum = pParamRisk + "," + pOutputCocaineConsumed;
		data.add(datum);
		
		SU.writeToFile(filePathAndName, data);
	}
}
