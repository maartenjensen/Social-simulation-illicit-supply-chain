package supplyChainModel.agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import frameworkTrust.RelationC;
import frameworkTrust.RelationS;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

/**
 * The Base Agent is the parent of all the supply chain agents
 * @author Maarten Jensen
 *
 */
public class BaseAgent {

	// State variables
	protected int id;
	protected CountryAgent baseCountry;
	protected SCType scType;
	
	protected double minPackageSize;
	protected double maxPackageSize;
	
	protected double securityStockMultiplier;
	protected HashMap<Byte, Double> stock;
	protected HashMap<Byte, Double> reservedStock;
	
	protected double personalRisk;
	protected double personalRiskThreshold;
	
	protected double desperation;
	
	protected double money;
	protected double sellPrice;
	protected double averageBuyCost = -1;
	protected double profitPercentage = Constants.PROFIT_PERC_START;
	
	protected int newSupplierCooldown = 0;
	protected int newClientCooldown = 0;
	
	protected int inactivityTimer = 0;

	protected HashMap<Integer, RelationS> relationsS = new HashMap<Integer, RelationS>(); // Key: node id
	protected HashMap<Integer, RelationC> relationsC = new HashMap<Integer, RelationC>(); // Key: node id

	protected ArrayList<Integer> possibleNewSuppliers = new ArrayList<Integer>();
	protected ArrayList<Integer> possibleNewClients   = new ArrayList<Integer>();
	
	// Visualization or data
	protected HashMap<Byte, Double> vsl_stock_current = new HashMap<Byte, Double>();
	protected HashMap<Byte, Double> vsl_stock_total = new HashMap<Byte, Double>();

	/**
	 * Constructor for new agents
	 * - Adds the agent to the context
	 * - Moves the agent to an appropriate location
	 */
	public BaseAgent(CountryAgent baseCountry, SCType scType, double sellPrice, double maxPackageSize) {
		
		SU.getContext().add(this);
		
		this.id = SU.getNewId();
		this.baseCountry = baseCountry;
		this.scType = scType;

		this.sellPrice = sellPrice;
		this.averageBuyCost = this.sellPrice;
		this.money = sellPrice * maxPackageSize * Constants.PRICE_MONEY_START_MULT;
		
		this.minPackageSize = maxPackageSize * (Constants.SHIPMENT_MIN_PERCENTAGE / 100);
		this.maxPackageSize = maxPackageSize;
		this.securityStockMultiplier = RandomHelper.nextDoubleFromTo(Constants.STOCK_SECURITY_MULT_MIN, Constants.STOCK_SECURITY_MULT_MAX);
		this.stock = new HashMap<Byte, Double>();
		
		this.personalRisk = 0;
		this.personalRiskThreshold = RandomHelper.nextDoubleFromTo(Constants.PERSONAL_RISK_THRESHOLD_MIN, Constants.PERSONAL_RISK_THRESHOLD_MAX);
		this.desperation = Constants.DESPERATION_RESET;
		
		move();
		if (!SU.getIsInitializing()) //When initializing this function is called manually for each agent in the ContextBuilder
			setPossibleNewSuppliersAndClients();
	}

	public BaseAgent(	int id, CountryAgent baseCountry, SCType scType, NdPoint newPos, double money, double sellPrice, double averageBuyCost, double profitPercentage,
						double maxPackageSize, double securityStockMultipier, double personalRisk, double personalRiskThreshold, double desperation, int inactivityTimer) {
		
		SU.getContext().add(this);
		
		this.id = id;
		this.baseCountry = baseCountry;
		this.scType = scType;
		
		this.money = money;
		this.sellPrice = sellPrice;
		this.averageBuyCost = averageBuyCost;
		this.profitPercentage = profitPercentage;
		
		this.minPackageSize = maxPackageSize * (Constants.SHIPMENT_MIN_PERCENTAGE / 100);
		this.maxPackageSize = maxPackageSize;
		this.securityStockMultiplier = securityStockMultipier;
		this.stock = new HashMap<Byte, Double>();
		
		this.personalRisk = personalRisk;
		this.personalRiskThreshold = personalRiskThreshold;
		this.desperation = desperation;
		
		this.inactivityTimer = inactivityTimer;
		
		move(newPos);
	}

	/*========================================================
	 * The main steps of the agents, called by ContextBuilder
	 *========================================================*/
	/**
	 * Pay the cost for stock saving, standard living cost and remove bankrupt nodes
	 * also remove inactive nodes
	 */
	public void stepCheckRemoval() {
		
		money -= ((Constants.PRICE_LIVING_COST_MULT * maxPackageSize) + Constants.PRICE_SAVED_STOCK_MULT * getTotalGoodsQuantity(stock)) * sellPrice;
		
		if (getAllMyShipments().size() >= 1)
			inactivityTimer = 0;
		else
			inactivityTimer ++;
		
		if (money < 0 || inactivityTimer > Constants.INACTIVITY_REMOVAL) {
			remove();
		}
	}

	/**
	 * Reset current stock for data, and decrease cooldowns
	 */
	public void stepResetParameters() {
		
		vsl_stock_current.clear();
		newSupplierCooldown = Math.max(0, newSupplierCooldown - 1);
		newClientCooldown   = Math.max(0, newClientCooldown - 1);
		personalRisk = Math.max(0, personalRisk - Constants.PERSONAL_RISK_DRAIN);
		
		//Calculate desperation
		if (isDesperationIncrease()) {
			desperation = Math.max(Constants.DESPERATION_RESET, Math.min(1, desperation + Constants.DESPERATION_INCREASE));
			if (desperation == 1) {
				profitPercentage = Math.max(Constants.PROFIT_PERC_MIN, profitPercentage - Constants.PROFIT_PERC_CHANGE);
				desperation = Constants.DESPERATION_RESET;
			}
		}
		else {
			desperation = Math.min(Constants.DESPERATION_RESET, Math.max(0, desperation - Constants.DESPERATION_INCREASE));
			if (desperation == 0) {
				profitPercentage = Math.min(Constants.PROFIT_PERC_MAX, profitPercentage + Constants.PROFIT_PERC_CHANGE);
				desperation = Constants.DESPERATION_RESET;
			}
		}
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

	/**
	 * Step for data collection, add current relations to data output
	 */
	public void stepAddToData() {
		
		int tick = SU.getTick();
		for (Integer id : relationsS.keySet()) {
			RelationS relationS = relationsS.get(id);
			SU.getDataCollector().addRelationData(tick + "," + relationS.getStateString());
		}
		
		for (Integer id : relationsC.keySet()) {
			RelationC relationC = relationsC.get(id);
			SU.getDataCollector().addRelationData(tick + "," + relationC.getStateString());
		}
	}
	
	/*================================
	 * Functions (Non-decision making)
	 *===============================*/
	
	public void receivePayment(double payment) {
		money += payment;
	}
	
	/**
	 * Search for a new supplier if the agent requires a new supplier,
	 * if there is one available, who is also looking for a new client,
	 * then the function returns and adds the new supplier (only one supplier is added)
	 */
	public void searchSuppliers() {
		
		if (!getRequireNewSupplier())
			return ;
		
		if (daringAndAction(Constants.PS_SEARCH_CONNECTION)) {
			Network<Object> net = SU.getNetworkSC();
			if (net.getInDegree(this) < Constants.MAX_NUMBER_OF_ACTIVE_SUPPLIERS) { //TODO change this to a search for active suppliers
				
				ArrayList<TrustCompare> allPossibleSuppliersSorted = sortAverageTrustInSuppliers(possibleNewSuppliers);
				for (TrustCompare sCompare : allPossibleSuppliersSorted) {
					
					// Condition for layer
					if (sCompare.getAgent().getScLayer() == (scType.getScLayer() - 1) && possibleNewSuppliers.contains(sCompare.getAgent().getId()) && sCompare.getAgent().getRequireNewClient()) {
						
						if (isDaring()) {
							addSupplier(sCompare.getAgent());
							sCompare.getAgent().addClient(this);
							return ;
						}
					}
				}
			}
		}
	}

	/**
	 * Sort the ArrayList of suppliers, based on the trust in those suppliers
	 * 
	 * @param possibleSuppliers
	 * @return The supplier with the highest trust will be the first one in the returned ArrayList
	 */
	public ArrayList<TrustCompare> sortAverageTrustInSuppliers(ArrayList<Integer> possibleSuppliers) {
		
		ArrayList<TrustCompare> unsorted = new ArrayList<TrustCompare>();
		for (Integer supplierId : possibleSuppliers) {
			BaseAgent supplier = SU.getBaseAgent(supplierId);
			if (supplier != null) {
				double sumTrust = 0;
				int sumCount = 0;
				for (BaseAgent client : SU.getObjectsAllExcludeRandom(BaseAgent.class, this)) {
					double trustInSupplier = client.retrieveSupplierTrust(supplier.getId());
					if (trustInSupplier >= 0) {
						sumTrust += trustInSupplier;
						sumCount ++;
					}
				}
				
				if (sumCount > 0)
					unsorted.add(new TrustCompare(supplier, sumTrust / sumCount));
				else
					unsorted.add(new TrustCompare(supplier, 0));
			}
		}

		Collections.sort(unsorted);
		Collections.reverse(unsorted);
		return unsorted;
	}

	/**
	 * Search for a new client if the agent requires a new client,
	 * if there is one available that is looking for a new supplier
	 * returns when it found a new client (to only add one at a time)
	 */
	public void searchClients() {
		
		if (!getRequireNewClient())
			return ;
	
		if (daringAndAction(Constants.PS_SEARCH_CONNECTION)) {
			
			Network<Object> net = SU.getNetworkSC();
			if (net.getOutDegree(this) < Constants.MAX_NUMBER_OF_ACTIVE_CLIENTS) { //TODO change this to a search for active clients
				
				ArrayList<TrustCompare> allPossibleClientsSorted = sortAverageTrustInClients(possibleNewClients);
				for (TrustCompare cCompare : allPossibleClientsSorted) {
					if (cCompare.getAgent().getScLayer() == (scType.getScLayer() + 1) && possibleNewClients.contains(cCompare.getAgent().getId()) && cCompare.getAgent().getRequireNewSupplier()) {
	
						if (isDaring()) {
							addClient(cCompare.getAgent());
							cCompare.getAgent().addSupplier(this);
							return ;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Sort the ArrayList of clients, based on the trust in those suppliers
	 * 
	 * @param possibleSuppliers
	 * @return The client with the highest trust will be the first one in the returned ArrayList
	 */
	public ArrayList<TrustCompare> sortAverageTrustInClients(ArrayList<Integer> possibleClients) {
		
		ArrayList<TrustCompare> unsorted = new ArrayList<TrustCompare>();
		for (Integer clientId : possibleClients) {
			BaseAgent client = SU.getBaseAgent(clientId);
			if (client != null) {
				double sumTrust = 0;
				int sumCount = 0;
				for (BaseAgent supplier : SU.getObjectsAllExcludeRandom(BaseAgent.class, this)) {
					double trustInClient = supplier.retrieveSupplierTrust(client.getId());
					if (trustInClient >= 0) {
						sumTrust += trustInClient;
						sumCount ++;
					}
				}
				
				if (sumCount > 0)
					unsorted.add(new TrustCompare(client, sumTrust / sumCount));
				else
					unsorted.add(new TrustCompare(client, 0));
			}
		}

		Collections.sort(unsorted);
		Collections.reverse(unsorted);
		return unsorted;
	}

	public void addSupplier(BaseAgent supplier) {
		
		if (!relationsS.keySet().contains(supplier.getId())) {
			increaseRisk(Constants.PS_NEW_CONNECTION);
			SU.getNetworkSCReversed().addEdge(this, supplier);
			relationsS.put(supplier.getId(), new RelationS(getId(), supplier.getId(), Constants.SHIPMENT_STEP * 2, id + " -> " + supplier.getId()));
			newSupplierCooldown = Constants.NEW_CONNECTION_COOLDOWN;
			Logger.logSCAgent(scType, getNameId() + " added supplier: " + supplier.getNameId());
		}
		
	}

	public void addClient(BaseAgent client) {
		
		if (!relationsC.keySet().contains(client.getId())) {
			resetDesperation();
			increaseRisk(Constants.PS_NEW_CONNECTION);
			SU.getNetworkSC().addEdge(this, client);
			relationsC.put(client.getId(), new RelationC(getId(), client.getId(), Constants.SHIPMENT_STEP * 2, id + " -> " + client.getId()));
			newClientCooldown = Constants.NEW_CONNECTION_COOLDOWN;
			Logger.logSCAgent(scType, getNameId() + " added client: " + client.getNameId());
		}
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
	 * Adds all the arrived orders to the relation with the client (RelationC). 
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
		
		if (relationsS.containsKey(otherId)) 
			return relationsS.get(otherId).getTrustLevel();
		else if (relationsC.containsKey(otherId)) 
			return relationsC.get(otherId).getTrustLevel();
		
		Logger.logError("BaseAgent.getTrustLevel " + getNameId() + ": id " + otherId + " not in relationsS and/or relationsC.");
		return -1;
	}
	
	/**
	 * This function does not have an error check
	 * @param otherId
	 * @return
	 */
	public double retrieveSupplierTrust(int otherId) {
		if (relationsS.containsKey(otherId)) 
			return relationsS.get(otherId).getTrustLevel();
		else
			return -1;
	}
	
	/**
	 * This function does not have an error check
	 * @param otherId
	 * @return
	 */
	public double retrieveClientTrust(int otherId) {
		if (relationsC.containsKey(otherId)) 
			return relationsC.get(otherId).getTrustLevel();
		else
			return -1;
	}
	
	public boolean retrieveRelationIsActive(int otherId) {
		
		if (relationsS.containsKey(otherId))
			return relationsS.get(otherId).isActive();
		else if (relationsC.containsKey(otherId)) 
			return relationsC.get(otherId).isActive();
		
		Logger.logError("BaseAgent.getTrustLevel " + getNameId() + ": id " + otherId + " not in relationsS and/or relationsC.");
		return false;
	}

	public double getBaseSellPrice() {
		
		double expectedCost = averageBuyCost;
		return expectedCost * (1 + (profitPercentage / (1 - profitPercentage)));
	}
	
	public double calculateCostOfGoods(HashMap<Byte, Double> goodsToSend, double sellPrice) {
		
		double cost = 0;
		for (Byte goodsQuality : goodsToSend.keySet()) {
			if (goodsToSend.get(goodsQuality) == Constants.QUALITY_MAXIMUM)
				cost += goodsToSend.get(goodsQuality) * sellPrice * Constants.QUALITY_MAX_EXTRA_COST;
			else
				cost += goodsToSend.get(goodsQuality) * sellPrice;
		}
		return cost;
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
			sortedSuppliers.add(new TrustCompare(SU.getBaseAgent(relation.getOtherId()),
								relation.getTrustForQuality((byte) quality)));
		}
		
		Collections.sort(sortedSuppliers);
		Collections.reverse(sortedSuppliers);
		return sortedSuppliers;
	}
	
	/**
	 * Sorts the clients with higher trust levels first.
	 * This function retrieves all the clients, then converts them to
	 * TrustCompare objects who can easily be sorted according to their 
	 * trust. 
	 * @return
	 */
	public ArrayList<TrustCompare> retrieveSortedClients() {
		
		ArrayList<TrustCompare> sortedClients = new ArrayList<TrustCompare>();
		for (BaseAgent client : getClients()) {
			RelationC relation = relationsC.get(client.getId());
			if (!RepastParam.getEnableSupplierOnPriceSelection()) {
				sortedClients.add( new TrustCompare(SU.getBaseAgent(relation.getOtherId()), relation.getTrustLevel()) ); // Check Trust
			}
			else {
				sortedClients.add( new TrustCompare(SU.getBaseAgent(relation.getOtherId()), SU.getBaseAgent(relation.getOtherId()).getBaseSellPrice()) ); // Check base sell price
			}
		}
		
		Collections.sort(sortedClients);
		if (!RepastParam.getEnableSupplierOnPriceSelection()) {
			Collections.reverse(sortedClients); // Trust needs to be reversed, as we want the highest trust first
		}
		return sortedClients;
	}
	
	/**
	 * Add the goods to the stock, if there is already stock of the
	 * same quality, it will be added to that stock
	 * @param goods
	 */
	public void addToStock(HashMap<Byte, Double> goods) {

		Logger.logSCAgent(scType, "Add:" + goods + " to " + stock);
		for (Byte quality : goods.keySet()) {
			if (stock.keySet().contains(quality))
				stock.put(quality, stock.get(quality) + goods.get(quality));
			else 
				stock.put(quality, goods.get(quality));
		}
		Logger.logSCAgent(scType, "New stock: " + stock);
		
		//out_totalImport += size; TODO correctly add it as history
		//out_currentImport += size;
	}
	
	/**
	 * Find the given quality and amount in the stock,
	 * removes it from the stock and returns the amount
	 * when the stock is lower than the craved amount it
	 * is put at zero. When the requested quality is not 
	 * in the stock it is added as a zero to the stock
	 * There is a check on minimum package size and (TODO maximum package size)
	 */
	public HashMap<Byte, Double> findGoodsInStock(HashMap<Byte, Double> cravedGoods) {

		HashMap<Byte, Double> choosenGoods = new HashMap<Byte, Double>();
		for (Byte quality : cravedGoods.keySet()) {
			if (stock.containsKey(quality)) {
				if (stock.get(quality) <= cravedGoods.get(quality)) {
					if (stock.get(quality) >= minPackageSize) {
						choosenGoods.put(quality, stock.get(quality));
						stock.put(quality, 0.0);
					}
				}
				else {
					choosenGoods.put(quality, cravedGoods.get(quality));
					stock.put(quality, stock.get(quality) - cravedGoods.get(quality));
				}
			}
			else { // This quality is added to the stock to make the agent send orders for this quality
				stock.put(quality, 0.0);
			}
		}
		return choosenGoods;
	}
	
	/**
	 * Combines the goods by addition from the given orders to a single HashMap
	 * @param orders
	 * @return
	 */
	public HashMap<Byte, Double> combineOrderedGoods(ArrayList<Order> orders) {
		
		HashMap<Byte, Double> orderedGoods = new HashMap<Byte, Double>();
		for (Order order : orders) {
			Logger.logSCAgent(scType, "combineOrderedGoods(): " + id + " add order:" + order.getGoodsStr());
			for (Byte quality : order.getGoods().keySet()) {
				if (!orderedGoods.containsKey(quality))
					orderedGoods.put(quality, order.getGoods().get(quality));
				else
					orderedGoods.put(quality, order.getGoods().get(quality) + orderedGoods.get(quality));
			}
		}
		return orderedGoods;
	}

	public void removeRelation(int id) {
		if (relationsS.containsKey(id)) {
			relationsS.remove(id);
		}
		else if (relationsC.containsKey(id)) {
			relationsC.remove(id);
		}
	}
	
	/**
	 * Update the average buy cost
	 * @param price
	 * @param quantity
	 */
	public void updateAverageBuyCost(double price, double quantity) {
		
		double costPerUnit = price / quantity;
		if (averageBuyCost >= 0) {
			averageBuyCost = (1 - Constants.AVERAGE_COST_LEARNING_RATE) * averageBuyCost + Constants.AVERAGE_COST_LEARNING_RATE * costPerUnit;
		}
		else {
			averageBuyCost = costPerUnit;
		}
	}
	
	/**
	 * Removes this object from the simulation
	 * This removes the edges, reversed edges, shipments and orders
	 */
	public void remove() {
		
		Logger.logSCAgent(scType, "Remove " + getNameId() + " " + baseCountry.getName());
		final Iterable<RepastEdge<Object>> edges = (Iterable<RepastEdge<Object>>) SU.getNetworkSC().getEdges(this);
		for (final RepastEdge<Object> edge : edges) {
			SU.getNetworkSC().removeEdge(edge);
		}
		final Iterable<RepastEdge<Object>> edgesRev = (Iterable<RepastEdge<Object>>) SU.getNetworkSCReversed().getEdges(this);
		for (final RepastEdge<Object> edge : edgesRev) {
			SU.getNetworkSCReversed().removeEdge(edge);
		}
		for (int supplierId : relationsS.keySet()) {
			SU.getBaseAgent(supplierId).removeRelation(id);
		}
		for (int clientId : relationsC.keySet()) {
			SU.getBaseAgent(clientId).removeRelation(id);
		}
		
		for (Shipment shipment : getAllMyShipments()) {
			SU.getDataCollector().addDeletedStock(shipment.getGoods());
			shipment.remove();
		}
		for (Order order : getAllMyOrders()) {
			order.remove();
		}
		
		SU.getDataCollector().addDeletedStock(stock);
		
		SU.getContext().remove(this);
	}

	public boolean edgeHasSendOrder(int otherId) {
		if (relationsC.containsKey(otherId)) {
			return relationsC.get(otherId).isOrderActive();
		}
		else if (relationsS.containsKey(otherId)) {
			return relationsS.get(otherId).isOrderActive();
		}
		return false;
	}
	
	public boolean edgeHasSendShipment(int otherId) {
		if (relationsC.containsKey(otherId)) {
			return relationsC.get(otherId).isShipmentActive();
		}
		else if (relationsS.containsKey(otherId)) {
			return relationsS.get(otherId).isShipmentActive();
		}
		return false;
	}
	
	/*================================
	 * Extensive getter and setter functions
	 *===============================*/
	
	/**
	 * Retrieve clients that are connected to this supplier
	 * @return the ArrayList of clients
	 */
	public ArrayList<BaseAgent> getClients() {
	
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
	
	/**
	 * First adds the closest suppplier or client (dependent on the integer)
	 * Find possible new suppliers, if the other agent can be a supplier of this agent, it is added if
	 * No limit on suppliers (legal supply chain) OR probability is high enough
	 * To prevent overlap, the addPossibleNewClient and addPossibleNewSupplier functions are NOT executed when the program is initializing
	 */
	public void setPossibleNewSuppliersAndClients() {
		
		addClosestSupplierOrClient(-1); // To add suppliers
		addClosestSupplierOrClient(1); // To add clients
		for (BaseAgent agent : SU.getObjectsAllRandom(BaseAgent.class)) {
		
			if (agent.getScLayer() == (scType.getScLayer() - 1)) { //Agent == a supplier
				if (!RepastParam.getLimitedSuppliersClients() || agent.checkCanKnowAgent(getCountry())) {
					if (!possibleNewSuppliers.contains(agent.getId()))
						possibleNewSuppliers.add(agent.getId());
				}
				
				//if (!SU.getIsInitializing())
				agent.addPossibleNewClient(this);
			}
			else if (agent.getScLayer() == (scType.getScLayer() + 1)) { //Agent == a client
				if (!RepastParam.getLimitedSuppliersClients() || agent.checkCanKnowAgent(getCountry())) {
					if (!possibleNewClients.contains(agent.getId()))
						possibleNewClients.add(agent.getId());
				}
				
				//if (!SU.getIsInitializing())
				agent.addPossibleNewSupplier(this);
			}
		}
	}
	
	/**
	 * 
	 * @param scLayerDifference should be -1 (for a supplier) and 1 (for a client)
	 */
	public void addClosestSupplierOrClient(int scLayerDifference) {

		int y_distance = Constants.GRID_HEIGHT;
		int agent_id = -1;
		for (BaseAgent agent : SU.getObjectsAllRandom(BaseAgent.class)) {
			
			if (agent.getScLayer() == (scType.getScLayer() + scLayerDifference)) {
				int new_y_distance = Math.abs(SU.getGrid().getLocation(this).getY() - SU.getGrid().getLocation(agent).getY());
				if (y_distance > new_y_distance) {
					agent_id = agent.getId();
					y_distance = new_y_distance;
				}
			}
		}
		if (agent_id >= 0) {
			if (scLayerDifference == -1 && !possibleNewSuppliers.contains(agent_id))
				possibleNewSuppliers.add(agent_id);
			else if (scLayerDifference == 1 && !possibleNewClients.contains(agent_id))
				possibleNewClients.add(agent_id);
		}
	}
	
	public void addPossibleNewSupplier(BaseAgent supplier) {
		
		if (!RepastParam.getLimitedSuppliersClients() || supplier.checkCanKnowAgent(supplier.getCountry()) || possibleNewSuppliers.isEmpty()) {
			if (!possibleNewSuppliers.contains(supplier.getId()))
				possibleNewSuppliers.add(supplier.getId());
		}
	}

	public void addPossibleNewClient(BaseAgent client) {
		
		if (!RepastParam.getLimitedSuppliersClients() || client.checkCanKnowAgent(client.getCountry()) || possibleNewClients.isEmpty()) {
			if (!possibleNewClients.contains(client.getId()))
				possibleNewClients.add(client.getId());
		}
	}

	/**
	 * The closer the agents are to each other, the higher the chance they know each other
	 * with a min and max so it will be excluded that there is a probability of 0 or a probability of 1
	 * @param y
	 * @return
	 */
	public boolean checkCanKnowAgent(CountryAgent otherCountry) {
		
		int borders = baseCountry.retrieveBordersN(otherCountry.getName());
		double probability = Constants.getBordersConnectP(borders);
		if (RandomHelper.nextDouble() <= probability) 
			return true;
		else
			return false;
	}
	
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
	
	protected double getMinPackageSizeBoth() {
		
		double minPackageSizeBoth = minPackageSize;
		
		for (Integer supplierId : relationsS.keySet()) {
			if (SU.getBaseAgent(supplierId) != null)
				minPackageSizeBoth = Math.max(minPackageSizeBoth, SU.getBaseAgent(supplierId).getMinPackageSize());
			else
				Logger.logError("BaseAgent.getMinPackageSizeBoth(): " + id + " supplierId " + supplierId + " not retrievable.");
		}
		
		return minPackageSizeBoth;
	}

	/**
	 * Require a new supplier when stock is zero for any of the qualities
	 * @return
	 */
	public boolean getRequireNewSupplier() {
		
		if (newSupplierCooldown > 0)
			return false;
		
		for (Byte quality : stock.keySet()) {
			if (stock.get(quality) < minPackageSize)
				return true;
		}
		return false;
	}
	
	public static ArrayList<Byte> shuffleSet(Set<Byte> set) {
		
		ArrayList<Byte> array = new ArrayList<Byte>();
		if (set.isEmpty())
			return array;
		
		array.addAll(set);
		Collections.shuffle(array);
		return array;
	}
	
	/*====================================
	 * Simple getter and setter functions
	 *====================================*/
	
	/**
	 * Require a client when all of the stocks are above security level
	 * @return
	 */
	public boolean getRequireNewClient() {
		
		if (newClientCooldown > 0)
			return false;
			
		double securityStock = securityStockMultiplier * minPackageSize;
		for (Byte quality : stock.keySet()) {
			if (stock.get(quality) < securityStock)
				return false;
		}
		return true;
	}
	
	public Double getTotalGoodsQuantity(HashMap<Byte, Double> pGoods) {
		double quantity = 0;
		for (Byte quality : pGoods.keySet()) {
			quantity += pGoods.get(quality);
		}
		return quantity;
	}
	
	public String getPossibleNewSuppliersStr() {
		return possibleNewSuppliers.toString();
	}
	
	public String getPossibleNewClientsStr() {
		return possibleNewClients.toString();
	}
	
	public Color getColor() {
		return scType.getColor();
	}

	/**
	 * Returns true if it has at least one connection with a
	 * supplier AND with a client.
	 * @return 
	 */
	public boolean isConnected() {
		if (!relationsC.isEmpty() && !relationsS.isEmpty())
			return true;
		return false;
	}
	
	public HashMap<Byte, Double> getStock() {
		return stock;
	}
	
	public String getStockStr() {
		return stock.toString();
	}
	
	public String getRelationsSStr() {
		return relationsS.toString();
	}
	
	public HashMap<Integer, RelationS> getRelationsS() {
		return relationsS;
	}
	
	public String getRelationsCStr() {
		return relationsC.toString();
	}
	
	public HashMap<Integer, RelationC> getRelationsC() {
		return relationsC;
	}
	
	public double getTotalImport() {
		return getTotalGoodsQuantity(vsl_stock_total);
	}

	public double getCurrentImport() {
		return getTotalGoodsQuantity(vsl_stock_current);
	}
	
	/**
	 * This is actually a getter function, however it has a parameter so repast
	 * will give an error in the repast HUD when the function name starts with get
	 * @return
	 */
	public double retrieveTotalImportQuality(byte quality) {
		if (vsl_stock_total.containsKey(quality))
			return vsl_stock_total.get(quality);
		return 0;
	}
	
	public double retrieveCurrentImportQuality(byte quality) {
		if (vsl_stock_current.containsKey(quality))
			return vsl_stock_current.get(quality);
		return 0;
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
	
	public String getScTypeName() {
		return scType.name();
	}
	
	public double getSecurityStockMult() {
		return securityStockMultiplier;
	}
	
	public double getMoney() {
		return money;
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
	
	public String getTrustInRelationsS() {
		String trustStr = "<";
		for (Integer supplierId : relationsS.keySet()) {
			if (!trustStr.equals("<"))
				trustStr += ", ";
			trustStr += supplierId + ":" + String.format("%.3f", relationsS.get(supplierId).getTrustLevel());
		}
		trustStr += ">";
		return trustStr;
	}
	
	public String getTrustInRelationsC() {
		String trustStr = "<";
		for (Integer supplierId : relationsC.keySet()) {
			if (!trustStr.equals("<"))
				trustStr += ", ";
			trustStr += supplierId + ":" + String.format("%.3f", relationsC.get(supplierId).getTrustLevel());
		}
		trustStr += ">";
		return trustStr;
	}
		
	
	public String getLabel() {
		String stockStr = ",s:";
		for (Byte quality : stock.keySet()) {
			if (!stockStr.equals(",s:"))
				stockStr += ",";
			if (quality == Constants.QUALITY_MINIMUM)
				stockStr += String.format("L%.1f", stock.get(quality));
			else if (quality == Constants.QUALITY_MAXIMUM)
				stockStr += String.format("H%.1f", stock.get(quality));
		}
		return id + String.format(" $%.0f", money) + stockStr + String.format(",[%.0f", (securityStockMultiplier * getMinPackageSizeBoth()));
	}
	
	public double getSecurityStock() {
		return securityStockMultiplier * getMinPackageSizeBoth();
	}
	
	public String toString() {
		return "" + id;//String.format("%.0f", stock);
	}
	
	public int getLocationX() {
		return SU.getGrid().getLocation(this).getX();
	}
	
	public int getLocationY() {
		return SU.getGrid().getLocation(this).getY();
	}
	
	public String getLocationString() {
		return SU.getContinuousSpace().getLocation(this).getX() + "," + SU.getContinuousSpace().getLocation(this).getY();
	}
	
	public String toStateString() {
		return id + "," + getLocationString() + "," + money;
	}
	
	public double getAverageBuyCost() {
		return averageBuyCost;
	}
	
	public double getProfitPercentage() {
		return profitPercentage;
	}
	
	public double getPersonalRisk() {
		return personalRisk;
	}
	
	public double getPersonalRiskThreshold() {
		return personalRiskThreshold;
	}
	
	public double getDesperation() {
		return desperation;
	}
	
	public boolean isDesperate() {
		if (desperation == 1.0)
			return true;
		return false;
	}
	
	/**
	 * Desperation depends on shipment active and larger than nothing stock
	 * @return
	 */
	public boolean isDesperationIncrease()
	{
		if (getTotalGoodsQuantity(stock) == 0.0)
			return false;
		for (Shipment shipment : getAllMyShipments()) {
			if (shipment.getIdSupplier() == id)
				return false;
		}
		return true;
	}
	
	public void resetDesperation() {
		desperation = Constants.DESPERATION_RESET;
	}
	
	/**
	 * Retrieves the following state variables from the BaseAgent: id, countryName, scTypeChar, x, y, money
	 * @return
	 * double maxPackageSize, double securityStockMultiplier, double personalRisk, double personalRiskThreshold, byte quality
	 */
	public String getVarsAsString() {
		String str = id + "," + baseCountry.getName() + "," + scType.getScCharacter() + "," + getLocationString() + "," + money + "," + sellPrice + "," + averageBuyCost + "," + profitPercentage
					+ "," + maxPackageSize + "," + securityStockMultiplier + "," + personalRisk + "," + personalRiskThreshold + "," + desperation + "," + inactivityTimer;
		if (this instanceof Agent1Producer) {
			str += "," + ((Agent1Producer) this).getQuality() + ",0";
		}
		else if (this instanceof Agent5Consumer) {
			str += "," + ((Agent5Consumer) this).getQuality() + "," + ((Agent5Consumer) this).getSpendableMoney();
		}
		else {
			str += ",0,0";
		}
		return str;
	}
	
	/**
	 * Returns false when the agent has a higher risk than the threshold and when risk is enabled
	 * otherwise returns true
	 * @return
	 */
	public boolean isDaring() {
		if (personalRisk >= personalRiskThreshold && RepastParam.getEnablePersonalRisk()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns false when the agent has a higher risk than the threshold and when risk is enabled
	 * otherwise returns true, it also adds to the personal risk
	 * @return
	 */
	public boolean daringAndAction(double riskAdd) {
		if (personalRisk >= personalRiskThreshold && RepastParam.getEnablePersonalRisk()) {
			return false;
		} 
		personalRisk = Math.min(Constants.PERSONAL_RISK_MAX, personalRisk + riskAdd);
		return true;
	}
	
	public void increaseRisk(double riskAdd) {
		personalRisk = Math.min(Constants.PERSONAL_RISK_MAX, personalRisk + riskAdd);
	}

	/*================================
	 * Visualization
	 *===============================*/
	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	public void move() {

		NdPoint newPos = baseCountry.getFreePosition(this, scType);
		Logger.logSCAgent(scType, getId() + " spawned in " + baseCountry.getName() + " pos:[" + newPos.getX() + ", " + newPos.getY() + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, (int) newPos.getX(), (int) newPos.getY());
	}
	
	public void move(NdPoint newPos) {
		
		Logger.logSCAgent(scType, getId() + " spawned in " + baseCountry.getName() + " pos:[" + newPos.getX() + ", " + newPos.getY() + "]");
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, (int) newPos.getX(), (int) newPos.getY());
	}
}