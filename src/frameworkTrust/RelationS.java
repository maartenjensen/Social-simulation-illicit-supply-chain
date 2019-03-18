package frameworkTrust;

import java.util.HashMap;

import repast.simphony.engine.environment.RunEnvironment;
import supplyChainModel.common.Constants;

public class RelationS {

	// This is a container that should be used in a map, linked to buyers or sellers
	// This object will contain the history on which trust is calculated
	
	private int otherId;
	private int supplyTime;
	
	private HashMap<Integer, HashMap<Byte, Double>> myOrders;
	private HashMap<Integer, HashMap<Byte, Double>> otherShipments;

	public RelationS(int otherId, int supplyTime) {
		
		this.otherId = otherId;
		this.supplyTime = supplyTime;
		
		myOrders       = new HashMap<Integer, HashMap<Byte, Double>>();
		otherShipments = new HashMap<Integer, HashMap<Byte, Double>>();
	}
	
	/**
	 * Add order to relation and add it to the smoothed orders
	 * @param orderedGoods
	 */
	public void addMyOrder(HashMap<Byte, Double> orderedGoods) {
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		myOrders.put(tick, orderedGoods);
	}
	
	public void addOtherShipment(HashMap<Byte, Double> shippedGoods) {
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		otherShipments.put(tick, shippedGoods);
	}
	
	// Getters
	public int getId() {
		return otherId;
	}
	
	public Double getPreviousMyOrder(Byte quality) {
		int previousTick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - 1;
		if (myOrders.containsKey(previousTick)) {
			if (myOrders.get(previousTick).containsKey(quality)) 
				return myOrders.get(previousTick).get(quality);
			else
				return 0.0;
		}
		else
			return 0.0;
	}
	
	/**
	 * Returns the trust in the agent with given id
	 */
	public double getTrustLevel() {
		
		double trust = 0;
		double differentQualities = 0;

		double trustMinimum = getTrustForQuality(Constants.QUALITY_MINIMUM);
		if (trustMinimum >= 0) {
			trust += trustMinimum;
			differentQualities ++;
		}
		
		double trustMaximum = getTrustForQuality(Constants.QUALITY_MAXIMUM);
		if (trustMaximum >= 0) {
			trust += trustMaximum;
			differentQualities ++;
		}
		
		if (differentQualities > 0)
			return trust / differentQualities;
		else
			return 0.5;
	}
	
	/**
	 * Retrieves the trust of deliveries for the given quality,
	 * returns -1 when this quality does not have any orders on it
	 * @param quality
	 * @return
	 */
	public double getTrustForQuality(Byte quality) {
		
		double amountOrders = 0;
		double amountShipments = 0;
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		for (Integer step : myOrders.keySet()) {

			// The order step should be smaller than the current step minus the total supply time
			if (step <= (tick - supplyTime) && myOrders.get(step).containsKey(quality)) {
				amountOrders += myOrders.get(step).get(quality);
			
				if (otherShipments.containsKey(step + supplyTime)) {
					if (otherShipments.get(step + supplyTime).containsKey(quality))
						amountShipments += otherShipments.get(step + supplyTime).get(quality);
				}
			}
		}
		
		// Return trust amount only when there are some orders
		if (amountOrders > 0) {
			return amountShipments / amountOrders;
		}
		else {
			return -1;
		}
	}
}
