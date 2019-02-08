package supplyChainModel.support;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.SU;

public class Shipment {


	private double size = 0;
	private int step = 0;
	private BaseAgent supplier = null;
	private BaseAgent buyer = null;
	
	private double moveSpeed = 0;
	private double moveHeading = 0;
	
	public Shipment(double size, int step, BaseAgent supplier, BaseAgent buyer) {

		this.size = size;
		this.step = step;
		this.supplier = supplier;
		this.buyer = buyer;
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
			buyer.receivePackage(supplier.getId(), size);
			SU.getContext().remove(this);
		}
	}
	
	public double getSize() {
		return size;
	}
	
	public String getLabel() {
		return String.format("%.2f", size);
	}
}