package supplyChainModel.agents;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import frameworkTrust.Trust;
import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

@AgentAnnot(displayName = "Redundant name")
public class BaseAgent {

	// Agent variables
	protected int id = -1;
	protected CountryAgent baseCountry;
	protected SCType scType;
	protected String name = "None";
	
	protected double securityStock = RepastParam.getSecurityStock();
	protected double stock = securityStock;
	protected double money = 0;
	
	protected double supplyNeeded = 0;
	protected double supplyAsked = 0;
	
	// Other variables
	protected Map<Integer, Trust> trustOther = new HashMap<Integer, Trust>(); // Key: node id
	
	// Lists and other structures
	protected ArrayList<Order> orders = new ArrayList<Order>();
	protected ArrayList<Order> unreceivedOrders = new ArrayList<Order>();
	
	/**
	 * Constructor
	 * - Adds the agent to the context
	 * - Moves the agent to an appropriate location
	 */
	public BaseAgent(final Context<Object> context, CountryAgent baseCountry, SCType scType) {
		context.add(this);
		this.baseCountry = baseCountry;
		this.id = SU.getNewId();
		this.scType = scType;
		move();
	}
	
	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	public void move() {

		Point newPos = baseCountry.getFreePosition(this, scType);
		Logger.logInfo(getNameId() + " pos:[" + newPos.x + ", " + newPos.y + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, newPos.x, (int) newPos.y);
	}
	
	/*====================================
	 * The main steps of the agents
	 *====================================*/
	
	public void step_1_shipment_intervention() {
		// Override by subclasses
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = -2, shuffle=true)
	public void step_2_receive_shipment() {
		// Override by subclasses
		// This is regulated by the shipments (which is bad)
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = -3, shuffle=true)
	public void step_3_choose_suppliers_and_buyers() {
		// Override by subclasses
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = -4, shuffle=true)
	public void step_4_send_shipment() {
		// Override by subclasses
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = -5, shuffle=true)
	public void step_5_receive_order() {
		// Override by subclasses
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = -6, shuffle=true)
	public void step_6_send_order() {
		// Override by subclasses
	}
	
	/*================================
	 * Functions
	 *===============================/
	
	/**
	 * This function is 
	 * @param size
	 */
	public void receivePackage(int supplierId, double size) {
		if (trustOther.containsKey(supplierId)) {
			trustOther.get(supplierId).addShipmentReceived(size);
		}
		stock += size;
	}

	public ArrayList<BaseAgent> getBuyers() { //TODO shuffle buyers
	
		final Iterable<RepastEdge<Object>> objects = (Iterable<RepastEdge<Object>>) SU.getNetworkSC().getOutEdges(this);
		final ArrayList<BaseAgent> objectList = new ArrayList<BaseAgent>();
		for (final RepastEdge<Object> edge : objects) {
			objectList.add((BaseAgent) edge.getTarget());
		}
		return objectList;
	}

	public ArrayList<BaseAgent> getSuppliers() { //TODO shuffle suppliers
		
		final Iterable<RepastEdge<Object>> objects = (Iterable<RepastEdge<Object>>) SU.getNetworkSC().getInEdges(this);
		final ArrayList<BaseAgent> objectList = new ArrayList<BaseAgent>();
		for (final RepastEdge<Object> edge : objects) {
			objectList.add((BaseAgent) edge.getSource());
		}
		return objectList;
	}
	
	public void searchSuppliers() {
		
		Network<Object> net = SU.getNetworkSC();
		if (net.getInDegree(this) == 0) {
			
			ArrayList<BaseAgent> suppliers = SU.getObjectsAllRandom(BaseAgent.class);
			for (BaseAgent supplier : suppliers) {
				if (supplier.getScLayer() == (scType.getScLayer() - 1)) {
					
					addSupplier(supplier);
					supplier.addBuyer(this);
					break;
				}
			}
		}
	}

	public void searchBuyers() {
		
		Network<Object> net = SU.getNetworkSC();
		
		if (net.getOutDegree(this) == 0) {
			
			ArrayList<BaseAgent> buyers = SU.getObjectsAllRandom(BaseAgent.class);
			for (BaseAgent buyer : buyers) {
				if (buyer.getScLayer() == (scType.getScLayer() + 1)) {

					addBuyer(buyer);
					buyer.addSupplier(this);
					break;
				}
			}
		}
	}

	public void addSupplier(BaseAgent supplier) {
		
		if (!trustOther.keySet().contains(supplier.getId())) {
			SU.getNetworkSCReversed().addEdge(this, supplier);
			trustOther.put(supplier.getId(), new Trust(supplier.getId()));
		}
		Logger.logInfoId(id, getNameId() + " added supplier: " + supplier.getNameId());
	}
	
	public void addBuyer(BaseAgent buyer) {
		
		if (!trustOther.keySet().contains(buyer.getId())) {
			SU.getNetworkSC().addEdge(this, buyer);
			trustOther.put(buyer.getId(), new Trust(buyer.getId()));
		}
		
		Logger.logInfoId(id, getNameId() + " added buyer: " + buyer.getNameId());
	}

	public void sendShipment() {
				
		for (Order order : orders) {
			
			double size = Math.min(stock, order.getSize());
			if (size > 0) {
				Shipment shipment = new Shipment(order.getSize(), RepastParam.getShipmentStep(), this, order.getBuyer());
				SU.getContext().add(shipment);
				shipment.setStartPosition();
				stock -= size;
			}
		}
		
		orders.removeAll(orders);
	}

	public void updateOrders() {
	
		supplyNeeded = Math.max(securityStock - stock, 0);
		ArrayList<Order> newOrders = new ArrayList<Order>();
		for (Order order : unreceivedOrders) {
			if (order.updateOrder()) {
				newOrders.add(order);
				supplyNeeded += order.getSize();
			}
		}
		unreceivedOrders.removeAll(newOrders);
		orders.addAll(newOrders);
		Logger.logInfoId(id, getNameId() + ", supply needed:" + supplyNeeded);
	}

	public void sendOrders() {
				
		supplyAsked = supplyNeeded * RepastParam.getLearningRate() + (1 - RepastParam.getLearningRate()) * supplyAsked;
		double tempSupplyAsked = supplyAsked;
		
		ArrayList<BaseAgent> suppliers = getSuppliers();
		for (BaseAgent supplier : suppliers) {
			
			double size = Math.min(tempSupplyAsked, RepastParam.getMaxPackage());
			if (size > 0) {
				if (trustOther.containsKey(supplier.getId())) {
					trustOther.get(supplier.getId()).addOrderSend(size);
				}
				supplier.addUnreceivedOrder(new Order(size, 3, this) ); //TODO make this a parameter
			}
			tempSupplyAsked -= size;
			if (tempSupplyAsked <= 0)
				return ;
		}
	}

	public double getTrustLevel(int otherId) {
		Logger.logInfo("Trust from" + name + id );
		
		if (trustOther.containsKey(otherId)) 
			return trustOther.get(otherId).getTrustLevel();
		
		Logger.logError("BaseAgent.getTrustLevel " + getNameId() + ": id " + otherId + " not in trust map.");
		return -1;
	}

	public void addUnreceivedOrder(Order unreceivedOrder) {
		unreceivedOrders.add(unreceivedOrder);
	}

	public Color getColor() {
		return Color.DARK_GRAY;
	}
	
	/**
	 * Support functions
	 */
	public int getScLayer() {
		return scType.getScLayer();
	}
	
	public int getId() {
		return id;
	}
	
	public String getNameId() {
		return name + id;
	}
	
	public String getLabel() {
		return name + id + String.format(", $%.1f,*:%.1f", money, stock);
	}
	
	public String toString() {
		return id + ", stock " + String.format("%.0f", stock);
	}
}