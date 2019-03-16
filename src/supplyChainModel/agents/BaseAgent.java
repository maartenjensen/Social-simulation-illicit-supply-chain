package supplyChainModel.agents;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import frameworkTrust.RelationC;
import frameworkTrust.RelationS;
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

	// State variables
	protected int id;
	protected CountryAgent baseCountry;
	protected SCType scType;
	protected double sellPrice;
	protected double minPackageSize;
	protected double maxPackageSize;
	
	protected double securityStock;
	protected Map<Byte, Double> stock;
	protected double money;
	
	protected double supplyNeeded = 0;
	protected double supplyAsked = 0;

	protected Map<Integer, RelationS> relationsS = new HashMap<Integer, RelationS>(); // Key: node id
	protected Map<Integer, RelationC> relationsC = new HashMap<Integer, RelationC>(); // Key: node id
	
	// Visualization
	protected double out_currentImport = 0;
	protected double out_totalImport = 0;

	/**
	 * Constructor
	 * - Adds the agent to the context
	 * - Moves the agent to an appropriate location
	 */
	public BaseAgent(CountryAgent baseCountry, SCType scType, double sellPrice, double maxPackageSize) {
		
		SU.getContext().add(this);
		
		this.id = SU.getNewId();
		this.baseCountry = baseCountry;
		this.scType = scType;
		
		this.sellPrice = sellPrice;
		this.money = sellPrice * maxPackageSize * 100;
		
		this.minPackageSize = maxPackageSize * (Constants.SHIPMENT_MIN_PERCENTAGE / 100);
		this.maxPackageSize = maxPackageSize;
		this.securityStock = Constants.SECURITY_STOCK;
		this.stock = new HashMap<Byte, Double>();
		
		move();
	}

	/*====================================
	 * The main steps of the agents
	 *====================================*/
	
	public void stepCheckRemoval() {
		if (money < 0) {
			remove();
		}
	}
	
	public void stepResetParameters() {
		out_currentImport = 0;
	}
	
	public void stepProcessArrivedShipments() {
		// handle shipments
	}
	
	public void stepChooseSuppliersAndClients() {
		// Override by subclasses
	}
	
	public void stepSendShipment() {
		// Override by subclasses
	}
	
	public void stepSendOrder() {
		// sendOrders();
	}
	
	/*================================
	 * Functions (Non-decision making)
	 *===============================*/
	
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
		
		if (!relationsS.keySet().contains(supplier.getId())) {
			SU.getNetworkSCReversed().addEdge(this, supplier);
			relationsS.put(supplier.getId(), new RelationS(supplier.getId(), RepastParam.getShipmentStep() * 2));
		}
		Logger.logInfoId(id, getNameId() + " added supplier: " + supplier.getNameId());
	}
	
	public void addClient(BaseAgent client) {
		
		if (!relationsC.keySet().contains(client.getId())) {
			SU.getNetworkSC().addEdge(this, client);
			relationsC.put(client.getId(), new RelationC(client.getId(), RepastParam.getShipmentStep() * 2));
		}
		
		Logger.logInfoId(id, getNameId() + " added client: " + client.getNameId());
	}
	
	/**
	 * Adds the orders who are placed by this agent to the relationsS
	 * @param placedOrders
	 */
	public void addOrdersToRelation(HashMap<Integer, Order> placedOrders) {
		
		for (int supplierId: placedOrders.keySet()) {
			relationsS.get(supplierId).addMyOrder(placedOrders.get(supplierId).getGoods());
		}
	}
	
	/**
	 * Updates the arrived shipments which are shipments from the suppliers.
	 * This function should not be called from the Agent1Producer, since he is
	 * the Supplier itself and therefore the getSupplier() function of the shipment
	 * will return null.
	 */
	public void updateArrivedShipments() {
		
		for (Shipment shipment : getArrivedShipments()) {
			
			if (!relationsS.containsKey(shipment.getSupplier().getId())) //java.lang.NullPointerException? See comment of this function
				Logger.logError("BaseAgent.updateArrivedShipments() in " + getId() + " : relationsS does not contain ID:" + shipment.getSupplier().getId());
			RelationS relationSupplier = relationsS.get(shipment.getSupplier().getId());
			relationSupplier.addOtherShipment(shipment.getGoods());
		}
	}

	/**
	 * Updates the arrived orders which are orders from the clients. 
	 */
	public void updateArrivedOrders() {
		
		for (Order order : getArrivedOrders()) {
			
			if (!relationsC.containsKey(order.getClient().getId()))
				Logger.logError("BaseAgent.updateArrivedOrders() in " + getId() + " : relationsC does not contain ID:" + order.getClient().getId());
			RelationC relationClient = relationsC.get(order.getClient().getId());
			relationClient.addOtherOrder(order.getGoods());
		}
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
		
		if (relationsS.containsKey(otherId)) 
			return relationsS.get(otherId).getTrustLevel();
		else if (relationsC.containsKey(otherId)) 
			return relationsC.get(otherId).getTrustLevel();
		
		Logger.logError("BaseAgent.getTrustLevel " + getNameId() + ": id " + otherId + " not in relationsS and/or relationsC.");
		return -1;
	}
	
	/**
	 * This function retrieves all the suppliers, then converts them to
	 * TrustCompare objects who can easily be sorted according to their 
	 * trust. The suppliers are sorted with higher trust levels first.
	 * @param quality
	 * @return
	 */
	public ArrayList<TrustCompare> retrieveSortedSuppliers(int quality) {
		
		ArrayList<TrustCompare> sortedSuppliers = new ArrayList<TrustCompare>();
		for (BaseAgent supplier : getSuppliers()) {
			RelationS relation = relationsS.get(supplier.getId());
			sortedSuppliers.add(new TrustCompare(SU.getBaseAgent(relation.getId()),
								relation.getTrustForQuality((byte) quality)));
		}
		
		Collections.sort(sortedSuppliers);
		Collections.reverse(sortedSuppliers);
		return sortedSuppliers;
	}
	
	/**
	 * Add the goods to the stock, if there is already stock of the
	 * same quality, it will be added to that stock
	 * @param goods
	 */
	public void addToStock(Map<Byte, Double> goods) {

		Logger.logInfo("Add:" + goods + " to " + stock);
		for (Byte quality : goods.keySet()) {
			if (stock.keySet().contains(quality))
				stock.put(quality, stock.get(quality) + goods.get(quality));
			else 
				stock.put(quality, goods.get(quality));
		}
		Logger.logInfo("New stock: " + stock);
		
		//out_totalImport += size; TODO correctly add it as history
		//out_currentImport += size;
	}
	
	/**
	 * Find the given quality and amount in the stock,
	 * removes it from the stock and returns the amount
	 * when the stock is lower than the craved amount it
	 * is removed from the Map
	 */
	public HashMap<Byte, Double> findGoodsInStock(Map<Byte, Double> cravedGoods) {

		HashMap<Byte, Double> choosenGoods = new HashMap<Byte, Double>();
		for (Byte quality : cravedGoods.keySet()) {
			if (stock.containsKey(quality)) {
				if (stock.get(quality) <= cravedGoods.get(quality)) {
					choosenGoods.put(quality, stock.get(quality));
					stock.remove(quality);
				}
				else {
					choosenGoods.put(quality, cravedGoods.get(quality));
					Logger.logInfo("Before:" + stock.toString());
					stock.put(quality, stock.get(quality) - cravedGoods.get(quality));
					Logger.logInfo("After:" + stock.toString());
				}
			}
		}
		return choosenGoods;
	}

	/**
	 * Removes this object from the simulation
	 * This removes the edges, reversed edges, shipments and orders
	 */
	public void remove() {
		
		Logger.logInfo("Remove " + getNameId() + " " + baseCountry.getName());
		final Iterable<RepastEdge<Object>> edges = (Iterable<RepastEdge<Object>>) SU.getNetworkSC().getEdges(this);
		for (final RepastEdge<Object> edge : edges) {
			SU.getNetworkSC().removeEdge(edge);
		}
		final Iterable<RepastEdge<Object>> edgesRev = (Iterable<RepastEdge<Object>>) SU.getNetworkSCReversed().getEdges(this);
		for (final RepastEdge<Object> edge : edgesRev) {
			SU.getNetworkSCReversed().removeEdge(edge);
		}
		for (Shipment shipment : getAllMyShipments()) {
			shipment.remove();
		}
		for (Order order : getAllMyOrders()) {
			order.remove();
		}
		SU.getContext().remove(this);
	}

	/*================================
	 * Getters and setters
	 *===============================*/	
	protected ArrayList<Shipment> getArrivedShipments() {
		
		ArrayList<Shipment> arrivedShipments = new ArrayList<Shipment>();
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			if (shipment.isArrived() && shipment.getClient() == this)
				arrivedShipments.add(shipment);
		}
		return arrivedShipments;
	}

	protected ArrayList<Order> getArrivedOrders() {
		
		ArrayList<Order> arrivedOrders = new ArrayList<Order>();
		for (Order order : SU.getObjectsAll(Order.class)) {
			if (order.isArrived() && order.getSupplier() == this)
				arrivedOrders.add(order);
		}
		return arrivedOrders;
	}
	
	protected ArrayList<Shipment> getAllMyShipments() {
		
		ArrayList<Shipment> allMyShipments = new ArrayList<Shipment>();
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			if (shipment.getClient() == this || shipment.getSupplier() == this)
				allMyShipments.add(shipment);
		}
		return allMyShipments;
	}
	
	protected ArrayList<Order> getAllMyOrders() {
		
		ArrayList<Order> allMyOrders = new ArrayList<Order>();
		for (Order order : SU.getObjectsAll(Order.class)) {
			if (order.getClient() == this || order.getSupplier() == this)
				allMyOrders.add(order);
		}
		return allMyOrders;
	}
	
	public Color getColor() {
		return Color.DARK_GRAY;
	}

	public Map<Byte, Double> getStock() {
		return stock;
	}
	
	public String getStockStr() {
		return stock.toString();
	}
	
	public String getRelationsSStr() {
		return relationsS.toString();
	}
	
	public String getRelationsCStr() {
		return relationsC.toString();
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
	
	public double getMinPackageSize() {
		return minPackageSize;
	}
	
	public double getMaxPackageSize() {
		return maxPackageSize;
	}
	
	public String getNameId() {
		return scType.getScCharacter() + id;
	}
	
	public String getLabel() {
		double totalQuantity = 0;
		for (Byte quality : stock.keySet()) {
			totalQuantity += stock.get(quality);
		}
		return id + String.format("  $%.0f, s:%.1f", money, totalQuantity);
	}
	
	public String toString() {
		return id + ", stock " + stock.toString();//String.format("%.0f", stock);
	}
	
	/*================================
	 * Visualization
	 *===============================*/
	
	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	public void move() {

		Point newPos = baseCountry.getFreePosition(this, scType);
		Logger.logInfo(getNameId() + " " + baseCountry.getName() + " pos:[" + newPos.x + ", " + newPos.y + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, newPos.x, newPos.y);
	}
}