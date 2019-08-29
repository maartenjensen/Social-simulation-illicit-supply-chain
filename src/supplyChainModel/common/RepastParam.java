package supplyChainModel.common;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

/**
 * Class that upon running setRepastParameters() saves the
 * parameters that are set in the Repast HUD. The code can 
 * retrieve these parameters through this class.
 * @author Maarten Jensen
 *
 */
public final class RepastParam {

	// Default settings
	//private static int runLength = 1000;
	private static double consumptionMin = 0.5;
	private static double consumptionMax = 5;
	private static double productionMax = 10;
	private static double sendShipmentProbability = 1;
	private static boolean enablePersonalRisk = true;
	private static boolean enableDesperation = true;
	private static boolean enableSupplierOnPriceSelection = true;
	//private static boolean enableDesperationOnOrder = true;
	private static int producerNumberCap = 8;
	private static boolean limitedSuppliersClients = false;
	private static boolean realisticMap = false;
	private static boolean settingLoadPopulationFile = false;
	private static int settingInitializeTime = 100;
	
	private static String interventionType = "none";
	private static int interventionGlobalPercentage = 5;
	private static int interventionWholesalerNLPercentage = 0;
	private static double interventionRiskIncreaseOther = 0.25;
	
	public static void setRepastParameters() {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		//runLength = p.getInteger("pRunLength");
		consumptionMin = p.getDouble("pConMin");
		consumptionMax = p.getDouble("pConMax");
		productionMax = p.getDouble("pProdMax");
		sendShipmentProbability = p.getDouble("pSendShipmentProbability");
		producerNumberCap = p.getInteger("pProducerNumberCap");
		limitedSuppliersClients = p.getBoolean("pLimitedSuppliersClients");
		realisticMap = p.getBoolean("pRealisticMap");
		enablePersonalRisk = p.getBoolean("pEnablePersonalRisk");
		enableDesperation = p.getBoolean("pDesperation");
		enableSupplierOnPriceSelection = p.getBoolean("pEnableSupplierOnPriceSelection");
		//enableDesperationOnOrder = p.getBoolean("pDesperationOnOrder");
		settingLoadPopulationFile = p.getBoolean("pSettingLoadPopulationFile");
		settingInitializeTime = p.getInteger("pSettingInitializeTime");
		
		interventionType 		= p.getString("pInterventionType");
		interventionGlobalPercentage = p.getInteger("pInterventionGlobalPercentage");
		interventionWholesalerNLPercentage = p.getInteger("pInterventionWholesalerNLPercentage");
		
		interventionRiskIncreaseOther = p.getDouble("pInterventionRiskIncreaseOther");
	}

	/**
	 * Run length is just taken from the parameters each time,
	 * so it can be changed during a run by the user
	 * @return
	 */
	public static double getRunLength() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		return p.getInteger("pRunLength");
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
	
	public static double getSendShipmentProbability() {
		return sendShipmentProbability;
	}
	
	public static int getProducerNumberCap() {
		return producerNumberCap;
	}
	
	public static boolean getLimitedSuppliersClients() {
		return limitedSuppliersClients;
	}
	
	public static boolean getRealisticMap() {
		return realisticMap;
	}
	
	public static boolean getEnablePersonalRisk() {
		return enablePersonalRisk;
	}
	
	public static boolean getEnableDesperation() {
		return enableDesperation;
	}
	
	//public static boolean enableDesperationOnOrder() {
	//	return enableDesperationOnOrder;
	//}
	
	public static boolean getSettingLoadPopulationFile() {
		return settingLoadPopulationFile;
	}
	
	public static int getSettingInitializeTime() {
		return settingInitializeTime;
	}
	
	public static boolean getEnableSupplierOnPriceSelection() {
		return enableSupplierOnPriceSelection;
	}
	
	public static String getInterventionType() {
		return interventionType;
	}
	
	public static int getInterventionGlobalPercentage() {
		return interventionGlobalPercentage;
	}
	
	public static int getInterventionWholesalerNLPercentage() {
		return interventionWholesalerNLPercentage;
	}
	
	public static double getInterventionRiskIncreaseOther() {
		return interventionRiskIncreaseOther;
	}
}