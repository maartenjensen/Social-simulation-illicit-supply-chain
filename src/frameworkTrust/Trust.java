package frameworkTrust;

import java.util.HashMap;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import supplyChainModel.common.Logger;

public class Trust {

	// This is a container that should be used in a map, linked to buyers or sellers
	// This object will contain the history on which trust is calculated
	
	private int otherId;
	
	protected Map<Integer, Double> orders = new HashMap<Integer, Double>();
	protected Map<Integer, Double> shipments = new HashMap<Integer, Double>();
	
	public Trust(int id) {
		this.otherId = id;
	}
	
	/**
	 * Returns the trust in the agent with given id
	 */
	public double getTrustLevel() {
		
		double trustLevel = 0.5;
		double amountOrders = 0;
		double amountShipments = 0;
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		for (Integer step : orders.keySet()) {
			
			// For each order that could have had a shipment, add order and shipment amount
			if (orders.containsKey(step) && step <= (tick - 6)) {
				amountOrders += orders.get(step);
				// If there is no shipment, the shipped amount is zero so it will not be added
				if (shipments.containsKey(step + 6)) { 
					amountShipments += shipments.get(step + 6); //TODO make this dependent on the time it can take, maybe orders have to get an ID (this line, previous line and before)
				}
			}
		}
		
		// Return trust amount when there are some orders
		if (amountOrders > 0) {
			Logger.logInfo("Trust in " + otherId + ":" + trustLevel);
			return amountShipments / amountOrders;
		}
		else {
			Logger.logInfo("Trust in " + otherId + ":" + trustLevel);
			return 0.5;
		}
	}

	public int getId() {
		return otherId;
	}
	
	public void addOrderSend(double size) {
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		orders.put(tick, size);
	}
	
	public void addShipmentReceived(double size) {
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		shipments.put(tick, size);
	}
}
