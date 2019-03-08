package supplyChainModel.agents;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import frameworkTrust.Trust;
import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

public class BaseAgent {

	// Agent variables
	protected int id;
	protected CountryAgent baseCountry;
	protected SCType scType;
	protected double sellPrice;
	protected int minPackageSize;
	protected int maxPackageSize;
	
	protected double securityStock;
	protected double stock;
	protected double money;
	
	protected double supplyNeeded = 0;
	protected double supplyAsked = 0;
	
	protected double out_currentImport = 0;
	protected double out_totalImport = 0;

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
		out_currentImport = 0;
	}
	
	public void stepChooseSuppliersAndClients() {
		// Override by subclasses
	}
	
	public void stepSendShipment() {
		// Override by subclasses
	}
	
	public void stepReceiveOrder() {
		// Override by subclasses
	}
	
	public void stepSendOrder() {
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
		out_totalImport += size;
		out_currentImport += size;
	}

	public void receivePayment(double payment) {
		money += payment;
	}

	/**
	 * Retrieve clients that are connected to this supplier
	 * @return the ArrayList of clients
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
	 * Retrieve suppliers that are connected to this client
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
					supplier.addClient(this);
					break;
				}
			}
		}
	}

	public void searchClients() {
		
		Network<Object> net = SU.getNetworkSC();
		
		if (net.getOutDegree(this) == 0) {
			
			ArrayList<BaseAgent> clients = SU.getObjectsAllRandom(BaseAgent.class);
			for (BaseAgent client : clients) {
				if (client.getScLayer() == (scType.getScLayer() + 1)) {

					// Countries should match up between retail and consumers
					if (client.getScType() == SCType.CONSUMER && !client.getCountry().equals(this.getCountry())) {
						break;
					}
					addClient(client);
					client.addSupplier(this);
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
	
	public void addClient(BaseAgent client) {
		
		if (!trustOther.keySet().contains(client.getId())) {
			SU.getNetworkSC().addEdge(this, client);
			trustOther.put(client.getId(), new Trust(client.getId()));
		}
		
		Logger.logInfoId(id, getNameId() + " added client: " + client.getNameId());
	}

	public void sendShipment() {
				
		for (Order order : orders) {
			
			double size = Math.min(stock, order.getSize());
			double price = size * sellPrice;
			
			if (size >= minPackageSize && size <= maxPackageSize) {
				new Shipment(SU.getContext(), size, RepastParam.getShipmentStep(), this, order.getClient(), price, 0.1);
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
			
			if (order.getClient() == baseAgent)
				unreceivedOrdersRemove.add(order);
		}
		unreceivedOrders.removeAll(unreceivedOrdersRemove);
		
		ArrayList<Order> ordersRemove = new ArrayList<Order>();
		for (Order order : orders) {
			
			if (order.getClient() == baseAgent)
				ordersRemove.add(order);
		}
		orders.removeAll(ordersRemove);
	}

	/**
	 * This method is purposely called retrieveTrustLevel and not getTrustLevel, 
	 * since repast automatically calls functions that start with get... when an
	 * agent is clicked in the Repast HUB. This happens due to a problem with 
	 * repast and getters WITH one or more parameters
	 * @param otherId
	 * @return the trust level to that agent
	 */
	public double retrieveTrustLevel(int otherId) {
		//Logger.logInfo("Trust from" + name + id );
		
		if (trustOther.containsKey(otherId)) 
			return trustOther.get(otherId).getTrustLevel();
		
		Logger.logError("BaseAgent.getTrustLevel " + getNameId() + ": id " + otherId + " not in trust map.");
		return -1;
	}

	public void addUnreceivedOrder(Order unreceivedOrder) {
		unreceivedOrders.add(unreceivedOrder);
	}

	/*================================
	 * Standard getters
	 *===============================*/
	
	public Color getColor() {
		return Color.DARK_GRAY;
	}

	public double getStock() {
		return stock;
	}
	
	public double getTotalImport() {
		return out_totalImport;
	}
	
	public double getCurrentImport() {
		return out_currentImport;
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
		return scType.getScCharacter() + id;
	}
	
	public String getLabel() {
		return id + String.format("  $%.0f  s:%.1f", money, stock);
	}
	
	public String toString() {
		return id + ", stock " + String.format("%.0f", stock);
	}
}