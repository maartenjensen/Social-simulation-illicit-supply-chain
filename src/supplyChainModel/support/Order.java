package supplyChainModel.support;

import supplyChainModel.agents.BaseAgent;

public class Order {

	private double size = 0;
	private int step = 0;
	private BaseAgent buyer = null;
	
	public Order(double size, int step, BaseAgent buyer) {

		this.size = size;
		this.step = step;
		this.buyer = buyer;
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
	
	public BaseAgent getBuyer() {
		return buyer;
	}
}
