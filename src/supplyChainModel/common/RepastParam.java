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
	private static int shipmentStep = 3;
	private static double spawnRate = 0.01;
	private static int ticksInitPopulation = 100;
	
	public static void setRepastParameters() {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		runLength = p.getInteger("pRunLength");
		securityStock = p.getDouble("pSecurityStock");
		learningRate = p.getDouble("pLearningRate");
		consumptionMax = p.getDouble("pConMax");
		consumptionMin = p.getDouble("pConMin");
		shipmentStep = p.getInteger("pShipmentStep");
		spawnRate = p.getDouble("pSpawnRate");
		ticksInitPopulation = p.getInteger("pTicksInitPopulation");
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
	
	public static int getShipmentStep() {
		return shipmentStep;
	}
	
	public static double getSpawnRate() {
		return spawnRate;
	}
	
	public static int getTicksInitPopulation() {
		return ticksInitPopulation;
	}
}