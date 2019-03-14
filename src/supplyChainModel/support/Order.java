package supplyChainModel.support;

import java.awt.Color;
import java.util.HashMap;

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
	private HashMap<Byte, Double> goods;
	private int stepsLeft;
	
	// Visualization variables
	private double vsl_moveSpeed;
	private double vsl_moveHeading;
	private Byte vsl_largest_quality;
	
	public Order(BaseAgent client, BaseAgent supplier, HashMap<Byte, Double> goods, int stepsLeft) {

		SU.getContext().add(this);
		
		this.client = client;
		this.supplier = supplier;
		this.goods = goods;
		this.stepsLeft = stepsLeft;
		
		System.out.println("New order: " + client.getId() + " to " + supplier.getId() + " : " + goods.toString());
		setStartPosition();
		setLargestQuality();
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
	public HashMap<Byte, Double> getGoods() {
		return goods;
	}
	
	public String getGoodsStr() {
		return goods.toString();
	}

	/**
	 * This function should only be allowed at initialization of
	 * the order.
	 * @param quality
	 * @param quantity
	 */
	public void addToGoods(Byte quality, Double quantity) {
		System.out.println(" Added to order: " + client.getId() + " to " + supplier.getId() + " : Q:" + quality + ", " + quantity );
		if (goods.containsKey(quality))
			goods.put(quality, goods.get(quality) + quantity);
		else
			goods.put(quality, quantity);
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
	
	public String getLabel() {
		//return String.format("%.1f", goods.get(vsl_largest_quality));
		return "";
	}
	
	/*
	 * Higher quality means a brighter yellow color for the
	 * shipments
	 */
	public Color getColor() {
		
		float factor = ((float) vsl_largest_quality / Constants.MAX_GOOD_QUALITY) * 255;
		return new Color(factor, factor, 0);		
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
		NdPoint clientLoc = new NdPoint(space.getLocation(client).getX(), space.getLocation(client).getY() - Constants.VSL_ORD_SHP_DIF_Y);
		NdPoint supplierLoc = new NdPoint(space.getLocation(supplier).getX(), space.getLocation(supplier).getY() - Constants.VSL_ORD_SHP_DIF_Y);
		
		space.moveTo(this, clientLoc.getX(), clientLoc.getY());
		
		vsl_moveSpeed = SU.point_distance(clientLoc.getX(), clientLoc.getY(), supplierLoc.getX(), supplierLoc.getY()) / (1 + stepsLeft);
		vsl_moveHeading = SU.point_direction(clientLoc.getX(), clientLoc.getY(), supplierLoc.getX(), supplierLoc.getY());
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed * (1 - Constants.VSL_ORD_SHP_DIF_MOVE), vsl_moveHeading, 0);
	}

	public void move() {
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed, vsl_moveHeading,0);
	}
	
	public void setLargestQuality() {
		
		double quantity = -1;
		for (Byte quality : goods.keySet()) {
			if (quantity == -1) {
				vsl_largest_quality = quality;
				quantity = goods.get(quality);
			}
			else if (goods.get(quality) > quantity) {
				vsl_largest_quality = quality;
				quantity = goods.get(quality);
			}	
		}
	}
}