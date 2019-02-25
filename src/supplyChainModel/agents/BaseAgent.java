package supplyChainModel.agents;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import frameworkTrust.Trust;
import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

@AgentAnnot(displayName = "Redundant name")
public class BaseAgent {

	// Agent variables
	protected int id;
	protected CountryAgent baseCountry;
	protected SCType scType;
	protected String name;
	protected double sellPrice;
	protected int minPackageSize;
	protected int maxPackageSize;
	
	protected double securityStock;
	protected double stock;
	protected double money;
	
	protected double supplyNeeded = 0;
	protected double supplyAsked = 0;
	
	protected double outputCurrentImport = 0;
	protected double outputTotalImport = 0;

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
	public BaseAgent(final Context<Object> context, CountryAgent baseCountry, SCType scType, double sellPrice, int maxPackageSize) {
		context.add(this);
		this.baseCountry = baseCountry;
		this.id = SU.getNewId();
		this.scType = scType;
		this.name = getScType().getScCharacter();
		this.sellPrice = sellPrice;
		this.money = sellPrice * maxPackageSize;
		
		this.minPackageSize = maxPackageSize / Constants.SHIPMENT_MIN_PERCENTAGE;
		this.maxPackageSize = maxPackageSize;
		this.securityStock = maxPackageSize;
		this.stock = securityStock;
		
		move();
	}
	
	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	public void move() {

		Point newPos = baseCountry.getFreePosition(this, scType);
		Logger.logInfo(getNameId() + " " + baseCountry.getName() + " pos:[" + newPos.x + ", " + newPos.y + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, newPos.x, newPos.y);
	}
	
	/*====================================
	 * The main steps of the agents
	 *====================================*/
	
	public void stepRemoval() {
		if (money < 0) {
			removeNode();
		}
	}
	
	public void stepResetParameters() {
		outputCurrentImport = 0;
	}
	
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
	 *===============================*/

	public void removeNode() {
		
		Logger.logInfo("Remove " + getNameId() + " " + baseCountry.getName());
		final Iterable<RepastEdge<Object>> edges = (Iterable<RepastEdge<Object>>) SU.getNetworkSC().getEdges(this);
		for (final RepastEdge<Object> edge : edges) {
			SU.getNetworkSC().removeEdge(edge);
		}
		final Iterable<RepastEdge<Object>> edgesRev = (Iterable<RepastEdge<Object>>) SU.getNetworkSCReversed().getEdges(this);
		for (final RepastEdge<Object> edge : edgesRev) {
			SU.getNetworkSCReversed().removeEdge(edge);
		}
		for (BaseAgent agent : SU.getObjectsAllExclude(BaseAgent.class, this)) {
			agent.removeOrdersFrom(this);
		}
		SU.getContext().remove(this);
	}

	/**
	 * This function is 
	 * @param size
	 */
	public void receivePackage(BaseAgent supplier, double size, double price) {
		// Update trust relation
		if (trustOther.containsKey(supplier.getId()))
			trustOther.get(supplier.getId()).addShipmentReceived(size);
		else
			Logger.logError("BaseAgent.receivePackage():supplier " + supplier.getId() + " not found in key");
		// Make payment
		money -= price;
		supplier.receivePayment(price);
		// Change stock
		stock += size;
		outputTotalImport += size;
		outputCurrentImport += size;
	}

	public void receivePayment(double payment) {
		money += payment;
	}

	/**
	 * Retrieve buyers that are connected to this supplier
	 * @return the ArrayList of buyers
	 */
	public ArrayList<BaseAgent> myBuyers() {
	
		final Iterable<RepastEdge<Object>> objects = (Iterable<RepastEdge<Object>>) SU.getNetworkSC().getOutEdges(this);
		final ArrayList<BaseAgent> objectList = new ArrayList<BaseAgent>();
		for (final RepastEdge<Object> edge : objects) {
			objectList.add((BaseAgent) edge.getTarget());
		}
		Collections.shuffle(objectList);
		return objectList;
	}

	/**
	 * Retrieve suppliers that are connected to this buyer
	 * @return the ArrayList of suppliers
	 */
	public ArrayList<BaseAgent> getSuppliers() {
		
		final Iterable<RepastEdge<Object>> objects = (Iterable<RepastEdge<Object>>) SU.getNetworkSC().getInEdges(this);
		final ArrayList<BaseAgent> objectList = new ArrayList<BaseAgent>();
		for (final RepastEdge<Object> edge : objects) {
			objectList.add((BaseAgent) edge.getSource());
		}
		Collections.shuffle(objectList);
		return objectList;
	}
	
	public void searchSuppliers() {
		
		Network<Object> net = SU.getNetworkSC();
		if (net.getInDegree(this) == 0) {
			
			ArrayList<BaseAgent> suppliers = SU.getObjectsAllRandom(BaseAgent.class);
			for (BaseAgent supplier : suppliers) {
				
				// Condition for layer
				if (supplier.getScLayer() == (scType.getScLayer() - 1)) {
					
					// Countries should match up between retail and consumers
					if (supplier.getScType() == SCType.RETAIL && !supplier.getCountry().equals(this.getCountry())) {
						break;
					}
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

					// Countries should match up between retail and consumers
					if (buyer.getScType() == SCType.CONSUMER && !buyer.getCountry().equals(this.getCountry())) {
						break;
					}
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
			double price = size * sellPrice;
			
			if (size >= minPackageSize && size <= maxPackageSize) {
				new Shipment(SU.getContext(), size, RepastParam.getShipmentStep(), this, order.getBuyer(), price);
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

	/**
	 * Orders have a limit on size, they should be within minPackageSize and maxPackageSize
	 * Needs the only order when having enough money to pay
	 */
	public void sendOrders() {
				
		supplyAsked = supplyNeeded * RepastParam.getLearningRate() + (1 - RepastParam.getLearningRate()) * supplyAsked;
		double tempSupplyAsked = supplyAsked;
		
		ArrayList<BaseAgent> suppliers = getSuppliers();
		for (BaseAgent supplier : suppliers) {
			
			double size = Math.min(tempSupplyAsked, supplier.getMaxPackageSize());
			if (size > 0) {
				//Ask minimum
				size = Math.max(size, supplier.getMinPackageSize());
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
	
	public void removeOrdersFrom(BaseAgent baseAgent) {
		
		ArrayList<Order> unreceivedOrdersRemove = new ArrayList<Order>();
		for (Order order : unreceivedOrders) {
			
			if (order.getBuyer() == baseAgent)
				unreceivedOrdersRemove.add(order);
		}
		unreceivedOrders.removeAll(unreceivedOrdersRemove);
		
		ArrayList<Order> ordersRemove = new ArrayList<Order>();
		for (Order order : orders) {
			
			if (order.getBuyer() == baseAgent)
				ordersRemove.add(order);
		}
		orders.removeAll(ordersRemove);
	}

	public double getTrustLevel(int otherId) {
		//Logger.logInfo("Trust from" + name + id );
		
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
	public double getStock() {
		return stock;
	}
	
	public double getTotalImport() {
		return outputTotalImport;
	}
	
	public double getCurrentImport() {
		return outputCurrentImport;
	}

	public CountryAgent getCountry() {
		return baseCountry;
	}
	
	public int getScLayer() {
		return scType.getScLayer();
	}
	
	public SCType getScType() {
		return scType;
	}
	
	public int getId() {
		return id;
	}
	
	public int getMinPackageSize() {
		return minPackageSize;
	}
	
	public int getMaxPackageSize() {
		return maxPackageSize;
	}
	
	public String getNameId() {
		return name + id;
	}
	
	public String getLabel() {
		return id + String.format("  $%.0f  s:%.1f", money, stock);
	}
	
	public String toString() {
		return id + ", stock " + String.format("%.0f", stock);
	}
}