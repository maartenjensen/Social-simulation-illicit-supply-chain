package supplyChainModel.common;

import java.awt.Font;

public class Constants {
	
	// See SupplyChainModel.rs/context.xml
	public static final String ID_CONTEXT = "supply chain";
	public static final String ID_CONTINUOUS_SPACE = "continuous space";
	public static final String ID_GRID = "grid";
	public static final String ID_SC_NETWORK = "sc network";
	public static final String ID_SC_NETWORK_REVERSED = "sc network reversed";
	
	public static final int GRID_WIDTH = 21;
	public static final int GRID_HEIGHT = 30;
	public static final int GRID_CELL_SIZE = 40;
	
	public static final Font FONT_LABEL = new Font("Tahoma", Font.PLAIN , 10);
	
	public static final double PRICE_PRODUCTION = 5; //800; //Leaves then, per kilogram
	public static final double PRICE_BUY_FROM_PRODUCER = 10; //2147; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_INTERNATIONAL = 15; //34700; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_WHOLESALER = 20; //100000; //Cocaine in brick form in Europe, per kilogram (this is not from source)
	public static final double PRICE_BUY_FROM_RETAIL = 25; //120000; //Street level cocaine, per kilogram
	public static final double PRICE_CONSUMER_INCOME = 125; //120000;
	
	public static final int SHIPMENT_MAX_1TO2 = 100;
	public static final int SHIPMENT_MAX_2TO3 = 500;
	public static final int SHIPMENT_MAX_3TO4 = 100;
	public static final int SHIPMENT_MAX_4TO5 = 10;
	public static final int SHIPMENT_MIN_PERCENTAGE = 10;
	
	// Number of nodes per country
	public static final int N_PRODUCERS	= 2;
	public static final int N_INTERNATIONALS = 1;
	public static final int N_WHOLESALERS = 1;
	public static final int N_RETAILERS	= 1;
	public static final int N_CONSUMERS	= 1;
	
	// Consumer specific
	public static final int CONSUMER_REMOVE_TICKS = 200;
	public static final int CONSUMER_LIMIT_WITHOUT_SATISFACTION = 10;
	
	// Visualization constants
	public static final int COUNTRY_CONSUMERS_MAX = 2;
}