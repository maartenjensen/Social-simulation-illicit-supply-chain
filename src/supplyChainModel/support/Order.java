package supplyChainModel.support;

import java.util.Map;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;

public class Order {

	// State variables
	private BaseAgent client;
	private BaseAgent supplier;
	private Map<Byte, Double> goods;
	private int stepsLeft;
	
	// Visualization variables
	private double vsl_moveSpeed;
	private double vsl_moveHeading;
	
	public Order(BaseAgent client, BaseAgent supplier, Map<Byte, Double> goods, int stepsLeft) {

		SU.getContext().add(this);
		
		this.client = client;
		this.supplier = supplier;
		this.goods = goods;
		this.stepsLeft = stepsLeft;
		
		setStartPosition();
	}
	
	/**
	 * Decreases the steps, gives an error if there are less than zero steps and moves the order
	 */
	public void stepAdvanceOrder() {
		
		this.stepsLeft -= 1;
		if (this.stepsLeft <= -1) {
			Logger.logError("stepAdvanceOrder(): stepsLeft:" + stepsLeft + ", " + toString());
		}
		move();
	}
	
	public void remove() {
		
		Logger.logInfo("Remove:" + toString());
		SU.getContext().remove(this);
	}
	
	// Getters and setters
	public Map<Byte, Double> getGoods() {
		return goods;
	}

	public boolean isArrived() {
		if (stepsLeft == 0)
			return true;
		return false;
	}
	
	public BaseAgent getClient() {
		return client;
	}
	
	public BaseAgent getSupplier() {
		return supplier;
	}
	
	public String toString() {
		return "order";
	}
	
	// Visualization
	/**
	 * 1. Set starting position of the order at the client
	 * 2. Get client and supplier location
	 * 2. Calculate direction and movement speed
	 * 3. Do one step of movement, smaller movement because it is an order
	 */
	public void setStartPosition() {
		
		final ContinuousSpace<Object> space = SU.getContinuousSpace(); 
		NdPoint clientLoc = new NdPoint(space.getLocation(client).getX(), space.getLocation(client).getY() + Constants.VSL_ORD_SHP_DIF_Y);
		NdPoint supplierLoc = new NdPoint(space.getLocation(supplier).getX(), space.getLocation(supplier).getY() + Constants.VSL_ORD_SHP_DIF_Y);
		
		space.moveTo(this, clientLoc.getX(), clientLoc.getY());
		
		vsl_moveSpeed = SU.point_distance(supplierLoc.getX(), supplierLoc.getY(), clientLoc.getX(), clientLoc.getY()) / (1 + stepsLeft);
		vsl_moveHeading = SU.point_direction(supplierLoc.getX(), supplierLoc.getY(), clientLoc.getX(), clientLoc.getY());
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed * (1 - Constants.VSL_ORD_SHP_DIF_MOVE), vsl_moveHeading,0);
	}

	public void move() {
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed, vsl_moveHeading,0);
	}
}