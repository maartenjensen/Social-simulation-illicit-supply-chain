package supplyChainModel.support;

import java.util.Map;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;

public class Shipment {


	// State variables
	private BaseAgent client;
	private BaseAgent supplier;
	private Map<Byte, Double> goods;
	private int stepsLeft;
	private double price;
	//private double seizureChance;
	
	// Visualization variables
	private double vsl_moveSpeed;
	private double vsl_moveHeading;
	private double vsl_size;
	
	public Shipment(BaseAgent client, BaseAgent supplier, Map<Byte, Double> goods, double price, int stepsLeft) {

		SU.getContext().add(this);
		
		this.client = client;
		this.supplier = supplier;
		this.goods = goods;
		this.price = price;
		this.stepsLeft = stepsLeft;
		//this.seizureChance = seizureChance;
		
		setStartPosition();
	}
	
	/**
	 * Decreases the steps, gives an error if there are less than zero steps and moves the order
	 */
	public void stepAdvanceShipment() {
		
		stepsLeft -= 1;
		if (stepsLeft <= -1) {
			Logger.logError("stepAdvanceShipment(): stepsLeft:" + stepsLeft + ", " + toString());
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
	
	public double getPrice() {
		return price;
	}
	
	public boolean isArrived() {
		if (stepsLeft == 0)
			return true;
		return false;
	}
	
	public BaseAgent getSupplier() {
		return supplier;
	}
	
	public BaseAgent getClient() {
		return client;
	}
	
	public double getSize() {
		return vsl_size;
	}
	
	public String getLabel() {
		return String.format("%.1f", 10.0);
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
		NdPoint clientLoc   = new NdPoint(space.getLocation(client).getX(), space.getLocation(client).getY() + Constants.VSL_ORD_SHP_DIF_Y);
		NdPoint supplierLoc = new NdPoint(clientLoc.getX() - Constants.VSL_COUNTRY_X, clientLoc.getY());
		if (supplier != null) 
			supplierLoc = new NdPoint(space.getLocation(supplier).getX(), space.getLocation(supplier).getY() + Constants.VSL_ORD_SHP_DIF_Y);
		
		space.moveTo(this, supplierLoc.getX(), supplierLoc.getY());
		
		vsl_moveSpeed = SU.point_distance(supplierLoc.getX(), supplierLoc.getY(), clientLoc.getX(), clientLoc.getY()) / (1 + stepsLeft);
		vsl_moveHeading = SU.point_direction(supplierLoc.getX(), supplierLoc.getY(), clientLoc.getX(), clientLoc.getY());
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed * (1 + Constants.VSL_ORD_SHP_DIF_MOVE), vsl_moveHeading,0);
	}
	
	public void move() {
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed, vsl_moveHeading,0);
	}
	
	public void setSize() {
		
		vsl_size = 0;
		for (Byte quality : goods.keySet()) {
			vsl_size += goods.get(quality);
		}
	}
}