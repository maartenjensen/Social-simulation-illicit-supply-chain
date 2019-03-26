package frameworkTrust;

import java.util.HashMap;

import repast.simphony.engine.environment.RunEnvironment;

public class RelationC {

	// State variables
	private int otherId;
	private int supplyTime;
	private boolean active;

	private HashMap<Integer, HashMap<Byte, Double>> otherOrders;
	private HashMap<Integer, HashMap<Byte, Double>> myShipments;
	
	// Can also be derived from the otherOrders variables therefore it is not a state variable
	private int firstOrder;
	
	public RelationC(int otherId, int supplyTime) {
		
		this.otherId = otherId;
		this.supplyTime = supplyTime;
		active = true;
		firstOrder = -1;
		
		otherOrders = new HashMap<Integer, HashMap<Byte, Double>>();
		myShipments = new HashMap<Integer, HashMap<Byte, Double>>();
	}
	
	public void addOtherOrder(HashMap<Byte, Double> otherOrder) {
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		otherOrders.put(tick, otherOrder);
		
		if (firstOrder == -1) {
			firstOrder = tick;
		}
	}
	
	public void addMyShipment(HashMap<Byte, Double> myShipment) {
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		myShipments.put(tick, myShipment);
	}
	
	// Getters
	
	public int getId() {
		return otherId;
	}
	
	/**
	 * Trust is defined differently with a client than with a supplier
	 * it is dependent on the time they know each other (received an order),
	 * higher firstOrder difference from tick is a higher trust
	 */
	public double getTrustLevel() {

		if (firstOrder > -1) {
			int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			return (tick - (double) firstOrder) / tick;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Retrieves the previous order (with the given quality) of the previous tick (else return 0.0),
	 * this is used to estimate how much all the clients want to order
	 * @param quality
	 * @return
	 */
	public Double getPreviousOtherOrder(Byte quality) {
		
		int previousTick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - 1;
		if (otherOrders.containsKey(previousTick)) {
			if (otherOrders.get(previousTick).containsKey(quality)) 
				return otherOrders.get(previousTick).get(quality);
			else
				return 0.0;
		}
		else
			return 0.0;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setInActive() {
		active = false;
	}
	
	public int getSupplyTime() {
		return supplyTime;
	}
}