package supplyChainModel.support;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.SU;

public class Shipment {


	private double size;
	private int step;
	private BaseAgent supplier;
	private BaseAgent buyer;
	private double price;
	
	private double moveSpeed;
	private double moveHeading;
	
	public Shipment(final Context<Object> context, double size, int step, BaseAgent supplier, BaseAgent buyer, double price) {

		context.add(this);
		this.size = size;
		this.step = step;
		this.supplier = supplier;
		this.buyer = buyer;
		this.price = price;
		
		setStartPosition();
	}

	/**
	 * 1. Set starting position of the shipment (on the supplier)
	 * 2. Calculate direction and movement speed (for visualization)
	 * 3. Do one step of movement
	 */
	public void setStartPosition() {
				
		final ContinuousSpace<Object> space = SU.getContinuousSpace();
		NdPoint supplierLoc = space.getLocation(supplier);
		NdPoint buyerLoc = space.getLocation(buyer);
		space.moveTo(this, supplierLoc.getX(), supplierLoc.getY());
		
		moveSpeed = SU.point_distance(supplierLoc.getX(), supplierLoc.getY(), buyerLoc.getX(), buyerLoc.getY()) / (1 + step);
		moveHeading = SU.point_direction(supplierLoc.getX(), supplierLoc.getY(), buyerLoc.getX(), buyerLoc.getY());
		
		move();
	}

	public void move() {
		
		SU.getContinuousSpace().moveByVector(this, moveSpeed, moveHeading,0);
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = -2, shuffle=true)
	public void step() {
		step -= 1;
		move();
		//space.moveByVector(this, 1, Math.toRadians(heading),0,0);
		if (step == 0) {
			buyer.receivePackage(supplier, size, price);
			SU.getContext().remove(this);
		}
	}
	
	public double getSize() {
		return size;
	}

	public String getLabel() {
		return String.format("%.1f", size);
	}
}