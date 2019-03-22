package supplyChainModel.common;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public final class RepastParam {

	// Default settings
	private static int runLength = 1000;
	private static double consumptionMin = 0.5;
	private static double consumptionMax = 5;
	private static double productionMax = 10;
	private static int shipmentStep = 3;
	private static double spawnRate = 0.01;
	private static int ticksInitPopulation = 100;
	private static double sendShipmentProbability = 1;
	private static int producerNumberCap = 8;
	
	public static void setRepastParameters() {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		runLength = p.getInteger("pRunLength");
		consumptionMin = p.getDouble("pConMin");
		consumptionMax = p.getDouble("pConMax");
		productionMax = p.getDouble("pProdMax");
		shipmentStep = p.getInteger("pShipmentStep");
		spawnRate = p.getDouble("pSpawnRate");
		ticksInitPopulation = p.getInteger("pTicksInitPopulation");
		sendShipmentProbability = p.getDouble("pSendShipmentProbability");
		producerNumberCap = p.getInteger("pProducerNumberCap");
	}

	public static double getRunLength() {
		return runLength;
	}
	
	public static double getConsumptionMin() {
		return consumptionMin;
	}
	
	public static double getConsumptionMax() {
		return consumptionMax;
	}
	
	public static double getProductionMax() {
		return productionMax;
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
	
	public static double getSendShipmentProbability() {
		return sendShipmentProbability;
	}
	
	public static int getProducerNumberCap() {
		return producerNumberCap;
	}
}