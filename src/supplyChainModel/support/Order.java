package supplyChainModel.support;

import supplyChainModel.agents.BaseAgent;

public class Order {

	// State variables
	private double size = 0;
	private int step = 0;
	private BaseAgent client = null;
	
	public Order(double size, int step, BaseAgent client) {

		this.size = size;
		this.step = step;
		this.client = client;
	}
	
	/**
	 * Returns true when the order arrived.
	 * @return
	 */
	public boolean updateOrder() {
		
		this.step -= 1;
		if (this.step == 0) {
			return true;
		}
		return false;
	}

	
	public double getSize() {
		return size;
	}
	
	public BaseAgent getClient() {
		return client;
	}
}