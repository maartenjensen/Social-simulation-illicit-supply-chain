package frameworkTrust;

import java.util.HashMap;

import repast.simphony.engine.environment.RunEnvironment;
import supplyChainModel.common.Logger;

public class Relation {

	// This is a container that should be used in a map, linked to buyers or sellers
	// This object will contain the history on which trust is calculated
	
	private int otherId;
	private boolean isSupplier; // If false then the other is a client
	private double learnRateExpectedOrder;
	private int supplyTime;
	
	protected HashMap<Integer, HashMap<Byte, Double>> orders;
	protected HashMap<Integer, HashMap<Byte, Double>> shipments;
	
	private HashMap<Byte, Double> expectedClientOrders;
	private HashMap<Byte, Double> previousOrders;

	public Relation(int id, boolean isSupplier, double learnRateExpectedOrder, int supplyTime) {
		
		this.otherId = id;
		this.isSupplier = isSupplier;
		this.learnRateExpectedOrder = learnRateExpectedOrder;
		this.supplyTime = supplyTime;
		
		orders = new HashMap<Integer, HashMap<Byte, Double>>();
		shipments = new HashMap<Integer, HashMap<Byte, Double>>();
		
		expectedClientOrders = new HashMap<Byte, Double>();
		previousOrders = new HashMap<Byte, Double>();
	}

	/**
	 * Returns the trust in the agent with given id
	 */
	public double getTrustLevel() {
		
		//TODO makes this a general trust
		return getTrustForQuality((byte) 90);
	}
	
	/**
	 * Retrieves the trust of deliveries for the given quality
	 * @param quality
	 * @return
	 */
	public double getTrustForQuality(Byte quality) {
		
		double amountOrders = 0;
		double amountShipments = 0;
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		for (Integer step : orders.keySet()) {

			// For each order that could have had a shipment, add order and shipment amount
			if (orders.containsKey(step) && step <= (tick - supplyTime)) {
				amountOrders += orders.get(step).get(quality);
				// If there is no shipment, the shipped amount is zero so it will not be added
				if (shipments.containsKey(step + supplyTime)) {
					amountShipments += shipments.get(step + supplyTime).get(quality);
				}
			}
		}
		
		// Return trust amount only when there are some orders
		if (amountOrders > 0) {
			return amountShipments / amountOrders;
		}
		else {
			return 0.5;
		}
	}

	public int getId() {
		return otherId;
	}

	public double getPreviousOrder(Byte quality) {
		if (previousOrders.containsKey(quality)) {
			return previousOrders.get(quality);
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Add order to relation and add it to the smoothed orders
	 * @param orderedGoods
	 */
	public void addOrderToSupplier(HashMap<Byte, Double> orderedGoods) {
		if (!isSupplier)
			Logger.logError("This relation " + otherId + " is a client, not a supplier");
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		orders.put(tick, orderedGoods);
		
		addCurrentOrder(orderedGoods);
	}
	
	/**
	 * The learning function is placed in the agent itself,
	 * based on the smoothedOrdersToSuppliers, the given value
	 * is smoothed and can thus directly replace the previous values
	 * @param orderedGoods
	 */
	public void addCurrentOrder(HashMap<Byte, Double> orderedGoods) {
		
		for (Byte quality : orderedGoods.keySet()) {
			previousOrders.put(quality, orderedGoods.get(quality));
		}
	}

	public void addShipmentFromSupplier(HashMap<Byte, Double> deliveredGoods) {
		if (!isSupplier)
			Logger.logError("This relation " + otherId + " is a client, not a supplier");
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		shipments.put(tick, deliveredGoods);
	}

	/**
	 * Updates the expectedOrders, all qualities so if a certain
	 * quality is not in the orderedGoods the expected will be 
	 * updated with the value zero (there is no ELSE since 'learnRate * 0 = 0')
	 * @param orderedGoods
	 */
	public void addOrderFromClient(HashMap<Byte, Double> orderedGoods) {
		if (isSupplier)
			Logger.logError("This relation " + otherId + " is a supplier, not a client");
		for (Byte quality : expectedClientOrders.keySet()) {
			double newExpected = expectedClientOrders.get(quality) * (1 - learnRateExpectedOrder);
			if (orderedGoods.containsKey(quality))
				newExpected += learnRateExpectedOrder * orderedGoods.get(quality);
			expectedClientOrders.put(quality, newExpected);
		}
	}
	
	public void addShipmentToClient(HashMap<Byte, Double> deliveredGoods) {
		if (isSupplier)
			Logger.logError("This relation " + otherId + " is a supplier, not a client");
	}
	
	public HashMap<Byte, Double> getExpectedOrders() {
		return expectedClientOrders;
	}
}
