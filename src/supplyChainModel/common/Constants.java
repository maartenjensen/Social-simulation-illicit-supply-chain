package supplyChainModel.common;

import java.awt.Font;
import java.util.HashMap;

/**
 * Contains all the parameters that are used in the simulation, 
 * except for the parameters that are influencable by the repast HUD.
 * This class should be used for parameters instead of hard-coding them.
 * @author Maarten Jensen
 *
 */
public class Constants {
	
	// See SupplyChainModel.rs/context.xml
	public static final String ID_CONTEXT = "supply chain";
	public static final String ID_CONTINUOUS_SPACE = "continuous space";
	public static final String ID_GRID = "grid";
	public static final String ID_SC_NETWORK = "sc network";
	public static final String ID_SC_NETWORK_REVERSED = "sc network reversed";
	
	public static final int GRID_WIDTH = 100;
	public static final int GRID_HEIGHT = 50;
	public static final int GRID_CELL_SIZE = 25;
	
	public static final Font FONT_LABEL = new Font("Tahoma", Font.PLAIN , 10);
	public static final Font FONT_BIG   = new Font("Tahoma", Font.BOLD , 14);
	public static final Font FONT_HUGE  = new Font("Tahoma", Font.BOLD , 28);
	
	// Simulation parameters
	public static final int PRODUCER_PRODUCE_AMOUNT = 50;
	public static final int MAX_NUMBER_OF_ACTIVE_SUPPLIERS = 10;
	public static final int MAX_NUMBER_OF_ACTIVE_CLIENTS = 10;
	public static final int INITIALIZE_TICKS = 50;
	public static final double SPAWN_RATE = 0.015;
	public static final double SPAWN_RATE_CONSUMERS = 0.06;
	public static final int INACTIVITY_REMOVAL = 104; //Represents two years
	
	public static final int SHIPMENT_STEP = 3;
	
	// Pricing parameters	
	public static final double PRICE_LIVING_COST_MULT = 0.01;
	public static final double PRICE_SAVED_STOCK_MULT = 0.01;
	public static final double PRICE_MONEY_START_MULT = 5;
	
	public static final double PRICE_PRODUCTION = 0.35; //800; //Leaves then, per kilogram
	public static final double PRICE_BUY_FROM_PRODUCER = 1; //2147; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_INTERNATIONAL = 16; //34700; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_WHOLESALER = 47; //100000; //Cocaine in brick form in Europe, per kilogram (this is not from source)
	public static final double PRICE_BUY_FROM_RETAIL = 56; //120000; //Street level cocaine, per kilogram
	public static final double PRICE_CONSUMER_BUY_MIN = 1; // +- 2150 per kilo, 2$ per gram
	public static final double PRICE_CONSUMER_BUY_MAX = 158; // +- 330.000 per kilo, 330 per gram
	
	public static final double SHIPMENT_MAX_1TO2 = 100;
	public static final double SHIPMENT_MAX_2TO3 = 500;
	public static final double SHIPMENT_MAX_3TO4 = 100;
	public static final double SHIPMENT_MAX_4TO5 = 20;
	public static final double SHIPMENT_MIN_PERCENTAGE = 5;
	
	public static final double SEND_ORDER_LEARN_RATE = 0.05;
	public static final double LATE_SHIPMENT_PENALIZE_MULT = 0.5;
	
	public static final double STOCK_SECURITY_MULT_MIN = 1; //Security stock is dependent on the minimum package size
	public static final double STOCK_SECURITY_MULT_MAX = 1.5;
	public static final double STOCK_SEARCH_NEW_SUPPLIER = 0.1;
	
	public static final double PROB_POSSIBLE_NEW_MIN = 0.1;
	public static final double PROB_POSSIBLE_NEW_MULT = 0.5;
	public static final int    NEW_CONNECTION_COOLDOWN = 10;
	
	// Risk concepts	
	public static final double PERSONAL_RISK_DRAIN = 0.1;
	public static final double PERSONAL_RISK_THRESHOLD_MIN = 0.5;
	public static final double PERSONAL_RISK_THRESHOLD_MAX = 0.5;
	
	// Desperation concepts
	public static final double DESPERATION_INCREASE = 0.025;
	public static final double DESPERATION_RESET = 0.5;
	
	public static final double AVERAGE_COST_LEARNING_RATE = 0.2;
	
	public static final double PROFIT_PERC_START = 0.5;
	public static final double PROFIT_PERC_MIN = 0.1;
	public static final double PROFIT_PERC_MAX = 0.9;
	public static final double PROFIT_PERC_CHANGE = 0.025;
	
	public static final double PS_SEARCH_CONNECTION = 0.05;
	public static final double PS_NEW_CONNECTION = 0.05;
	public static final double PS_SEND_ORDER = 0.01;
	public static final double PS_SEND_SHIPMENT = 0.05;
	
	//public static final int HashMap<Integer, Double> = ;
	private static final HashMap<Integer, Double> BORDERS_CONNECT_P = new HashMap<Integer, Double>() {
		private static final long serialVersionUID = 1L;
		{
			put(0, 0.4); put(1, 0.3); put(2, 0.2); put(3, 0.1); put(4, 0.05); put(5, 0.025); put(6, 0.0125); put(7, 0.00625);
			//put(0, 0.6); put(1, 0.5); put(2, 0.4); put(3, 0.3);	put(4, 0.2); put(5, 0.1); put(6, 0.05); put(7, 0.025);
		}
	};
	
	//public static final int HashMap<Integer, Double> = ;
	private static final HashMap<Integer, Integer> BORDERS_COST = new HashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put(0, 100); put(1, 250); put(2, 550); put(3, 1000); put(4, 1600); put(5, 2350); put(6, 3250); put(7, 4300);
		}
	};
	
	// Number of nodes per country
	public static final int N_PRODUCERS	= 6;
	public static final int N_INTERNATIONALS = 4;
	public static final int N_WHOLESALERS = 2;
	public static final int N_RETAILERS	= 2;
	public static final int N_CONSUMERS	= 2;

	//public static final byte MAX_GOOD_QUALITY = 100;
	public static final byte QUALITY_MINIMUM = 40;
	public static final byte QUALITY_MAXIMUM = 60;
	public static final double QUALITY_MAX_EXTRA_COST = 1.2;
	
	// Consumer specific
	public static final int CONSUMER_REMOVE_TICKS = 200;
	public static final int CONSUMER_LIMIT_WITHOUT_SATISFACTION = 52;
	public static final int COUNTRY_CONSUMERS_MAX = 2;
	
	// Visualization constants
	public static final double VSL_RAD_CONVERT = 180/Math.PI;
	public static final double VSL_EUROPEAN_MAP_SCALE = GRID_HEIGHT/1000.0 * 1.25;
	public static final double VSL_EUROPEAN_MAP_X_ADD = 600 * VSL_EUROPEAN_MAP_SCALE;
	public static final double VSL_N_AGENT_RADIUS = 12;
	
	public static final int VSL_COUNTRY_X = 4;
	public static final int VSL_COUNTRY_WIDTH = 12;
	public static final int VSL_N_AGENTS_CIRCULAR = 8;
	public static final int VSL_N_AGENTS_LAYERS = 2;
	public static final double VSL_ORD_SHP_DIF_Y = 0.2;
	public static final double VSL_ORD_SHP_DIF_MOVE = 0.2;
	
	public static double getBordersConnectP(int borders) {
		if (BORDERS_CONNECT_P.containsKey(borders)) {
			return BORDERS_CONNECT_P.get(borders);
		}
		else {
			Logger.logError("Constants.getBordersConnectP didn't find border number:" + borders);
			return 0.0;
		}
	}
	
	public static double getBordersCost(int borders) {
		if (BORDERS_COST.containsKey(borders)) {
			return BORDERS_COST.get(borders);
		}
		else {
			Logger.logError("Constants.getBordersCost didn't find border number:" + borders);
			return 0.0;
		}
	}
}