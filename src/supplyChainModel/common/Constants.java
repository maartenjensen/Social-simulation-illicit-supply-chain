package supplyChainModel.common;

import java.awt.Font;

public class Constants {
	
	// See SupplyChainModel.rs/context.xml
	public static final String ID_CONTEXT = "supply chain";
	public static final String ID_CONTINUOUS_SPACE = "continuous space";
	public static final String ID_GRID = "grid";
	public static final String ID_SC_NETWORK = "sc network";
	public static final String ID_SC_NETWORK_REVERSED = "sc network reversed";
	
	public static final int GRID_WIDTH = 35;
	public static final int GRID_HEIGHT = 40;
	public static final int GRID_CELL_SIZE = 25;
	
	public static final Font FONT_LABEL = new Font("Tahoma", Font.PLAIN , 10);
	
	public static final int PRODUCER_PRODUCE_AMOUNT = 50;
	
	public static final double PRICE_PRODUCTION = 0.1; //800; //Leaves then, per kilogram
	public static final double PRICE_BUY_FROM_PRODUCER = 1; //2147; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_INTERNATIONAL = 2.5; //34700; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_WHOLESALER = 43; //100000; //Cocaine in brick form in Europe, per kilogram (this is not from source)
	public static final double PRICE_BUY_FROM_RETAIL = 125; //120000; //Street level cocaine, per kilogram
	public static final double PRICE_CONSUMER_INCOME = 1500; //120000;
	
	public static final double SHIPMENT_MAX_1TO2 = 100;
	public static final double SHIPMENT_MAX_2TO3 = 500;
	public static final double SHIPMENT_MAX_3TO4 = 100;
	public static final double SHIPMENT_MAX_4TO5 = 20;
	public static final double SHIPMENT_MIN_PERCENTAGE = 5;
	
	public static final double SEND_ORDER_LEARN_RATE = 0.2;
	
	public static final double SECURITY_STOCK = 2;
	
	// Number of nodes per country
	public static final int N_PRODUCERS	= 2;
	public static final int N_INTERNATIONALS = 1;
	public static final int N_WHOLESALERS = 0;
	public static final int N_RETAILERS	= 0;
	public static final int N_CONSUMERS	= 0;

	public static final byte MAX_GOOD_QUALITY = 100;
	
	// Consumer specific
	public static final int CONSUMER_REMOVE_TICKS = 200;
	public static final int CONSUMER_LIMIT_WITHOUT_SATISFACTION = 10;
	
	public static final int COUNTRY_CONSUMERS_MAX = 2;
	
	// Visualization constants
	public static final int VSL_COUNTRY_X = 6;
	public static final int VSL_COUNTRY_WIDTH = 6;
	public static final double VSL_ORD_SHP_DIF_Y = 0.2;
	public static final double VSL_ORD_SHP_DIF_MOVE = 0.2;
}