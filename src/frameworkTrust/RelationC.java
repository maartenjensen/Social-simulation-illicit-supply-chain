package frameworkTrust;

import java.util.HashMap;

import repast.simphony.engine.environment.RunEnvironment;

public class RelationC {

	private int otherId;
	private int supplyTime;
	
	private HashMap<Integer, HashMap<Byte, Double>> otherOrders;
	private HashMap<Integer, HashMap<Byte, Double>> myShipments;
	
	public RelationC(int otherId, int supplyTime) {
		
		this.otherId = otherId;
		this.supplyTime = supplyTime;
		
		otherOrders = new HashMap<Integer, HashMap<Byte, Double>>();
		myShipments = new HashMap<Integer, HashMap<Byte, Double>>();
	}
	
	public void addOtherOrder(HashMap<Byte, Double> otherOrder) {
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		otherOrders.put(tick, otherOrder);
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
	 * therefore we don't look at the order shipment ratio here
	 */
	public double getTrustLevel() {
		
		//TODO makes this a general trust
		return 0.5;
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
}
