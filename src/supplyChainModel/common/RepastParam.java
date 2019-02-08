package supplyChainModel.common;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public final class RepastParam {

	// Default settings
	private static int runLength = 1000;
	private static double learningRate = 0.5;
	private static double securityStock = 1;
	private static double consumptionMax = 0;
	private static double consumptionMin = 0;
	private static double maxPackage = 0;
	private static int shipmentStep = 3;
	
	public static void setRepastParameters() {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		runLength = p.getInteger("pRunLength");  
		securityStock = p.getDouble("pSecurityStock");
		learningRate = p.getDouble("pLearningRate");
		consumptionMax = p.getDouble("pConMax");
		consumptionMin = p.getDouble("pConMin");  
		maxPackage = p.getDouble("pMaxPackage");
		shipmentStep = p.getInteger("pShipmentStep");
	}
	
	public static double getRunLength() {
		return runLength;
	}
	
	public static double getSecurityStock() {
		return securityStock;
	}
	
	public static double getLearningRate() {
		return learningRate;
	}
	
	public static double getConsumptionMax() {
		return consumptionMax;
	}
	
	public static double getConsumptionMin() {
		return consumptionMin;
	}
	
	public static double getMaxPackage() {
		return maxPackage;
	}
	
	public static int getShipmentStep() {
		return shipmentStep;
	}
}