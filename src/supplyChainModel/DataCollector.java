package supplyChainModel;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import repast.simphony.context.Context;
import supplyChainModel.agents.Agent1Producer;
import supplyChainModel.agents.Agent2International;
import supplyChainModel.agents.Agent3Wholesaler;
import supplyChainModel.agents.Agent4Retailer;
import supplyChainModel.agents.Agent5Consumer;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;

/**
 * Collects data from objects, to make it easy to write the data
 * to plots and filesinks.
 * @author Maarten Jensen
 *
 */
public class DataCollector {

	//private HashMap<SCType, Integer> removedAgents = ;
	private HashMap<Byte, Double> producedStock   = new HashMap<Byte, Double>();
	private Double cutSourceStock  = 0.0;
	private Double cutProductStock = 0.0;
	private HashMap<Byte, Double> deletedStock    = new HashMap<Byte, Double>();
	private HashMap<Byte, Double> consumedStock   = new HashMap<Byte, Double>();
	
	//private HashMap<Byte, Double> wholesalerSendNL = new HashMap<Byte, Double>();
	//private HashMap<Byte, Double> wholesalerSendES = new HashMap<Byte, Double>();
	
	protected List<String> relationsData = new ArrayList<String>();
	
	public DataCollector(final Context<Object> context) {
		context.add(this);
		relationsData.add("\"tick\",\"Id\",\"OtherId\",\"Type\",\"Trust\"");
		move();
	}

	/*================================
	 * Stock analyzing
	 *===============================*/

	/**
	 * Is called after initializing of the agents to save their initialized stock.
	 */
	public void addAllCurrentStock() {
		
		ArrayList<BaseAgent> agents = SU.getObjectsAll(BaseAgent.class);
		for (BaseAgent agent : agents) {
			addIdCurrentStock(agent.getId());			
		}
	}
	
	/**
	 * Add stock for the specific agent ID
	 */
	public void addIdCurrentStock(int id) {
		BaseAgent agent = SU.getBaseAgent(id);
		if (agent == null) {
			Logger.logError("DataCollector.addCurrentStock: Agent with id " + id + " does not exist");
			return ;
		}
		for (Byte quality : agent.getStock().keySet()) {
			if (producedStock.containsKey(quality))
				producedStock.put(quality, producedStock.get(quality) + agent.getStock().get(quality));
			else
				producedStock.put(quality, agent.getStock().get(quality));
		}
	}
	
	/**
	 * Called by Agent1Producers.addProducedStock() //TODO add this at wholesaler level
	 * @param producedGoods
	 */
	public void addProducedStock(HashMap<Byte, Double> producedGoods) {
		
		for (Byte quality : producedGoods.keySet()) {
			if (producedStock.containsKey(quality))
				producedStock.put(quality, producedStock.get(quality) + producedGoods.get(quality));
			else
				producedStock.put(quality, producedGoods.get(quality));
		}
	}

	/**
	 * Called by Agent5Consumer.stepSendShipment(), this function represents the consumed goods
	 * @param consumedGoods
	 */
	public void addConsumedStock(HashMap<Byte, Double> consumedGoods) {
		
		for (Byte quality : consumedGoods.keySet()) {
			if (consumedStock.containsKey(quality))
				consumedStock.put(quality, consumedStock.get(quality) + consumedGoods.get(quality));
			else
				consumedStock.put(quality, consumedGoods.get(quality));
		}
	}
	
	/**
	 * Called by BaseAgent.remove(), this represents goods that got lost
	 * @param deletedGoods
	 */
	public void addDeletedStock(HashMap<Byte, Double> deletedGoods) {
		
		for (Byte quality : deletedGoods.keySet()) {
			if (deletedStock.containsKey(quality))
				deletedStock.put(quality, deletedStock.get(quality) + deletedGoods.get(quality));
			else
				deletedStock.put(quality, deletedGoods.get(quality));
		}
	}
	
	public void addCuttingStock(double lowQuality, double highQuality) {
		cutSourceStock += highQuality;
		cutProductStock += lowQuality;
	}
	
	public double getStockProducedTot() {
		if (producedStock.containsKey(Constants.QUALITY_MINIMUM)) {
			if (producedStock.containsKey(Constants.QUALITY_MAXIMUM))
				return producedStock.get(Constants.QUALITY_MINIMUM) + producedStock.get(Constants.QUALITY_MAXIMUM);
			else
				return producedStock.get(Constants.QUALITY_MINIMUM);
		}
		else if (producedStock.containsKey(Constants.QUALITY_MAXIMUM))
			return producedStock.get(Constants.QUALITY_MAXIMUM);
		else
			return 0.0;
	}
	
	public double getStockConsumedTot() {
		if (consumedStock.containsKey(Constants.QUALITY_MINIMUM)) {
			if (consumedStock.containsKey(Constants.QUALITY_MAXIMUM))
				return consumedStock.get(Constants.QUALITY_MINIMUM) + consumedStock.get(Constants.QUALITY_MAXIMUM);
			else
				return consumedStock.get(Constants.QUALITY_MINIMUM);
		}
		else if (consumedStock.containsKey(Constants.QUALITY_MAXIMUM))
			return consumedStock.get(Constants.QUALITY_MAXIMUM);
		else
			return 0.0;
	}
	
	public double getStockDeletedTot() {
		if (deletedStock.containsKey(Constants.QUALITY_MINIMUM)) {
			if (deletedStock.containsKey(Constants.QUALITY_MAXIMUM))
				return deletedStock.get(Constants.QUALITY_MINIMUM) + deletedStock.get(Constants.QUALITY_MAXIMUM);
			else
				return deletedStock.get(Constants.QUALITY_MINIMUM);
		}
		else if (deletedStock.containsKey(Constants.QUALITY_MAXIMUM))
			return deletedStock.get(Constants.QUALITY_MAXIMUM);
		else
			return 0.0;
	}
	
	public double getStockCutSourceTot() {
		return cutSourceStock;
	}
	
	public double getStockCutProductTot() {
		return cutProductStock;
	}
	
	/*================================
	 * Other
	 *===============================*/
	public void addRelationData(String relationInfo) {
		relationsData.add(relationInfo);
	}
	
	public List<String> getRelationsData() {
		return relationsData;
	}

	//Agent count methods
	public int getCountProducers() {
		return SU.getObjectsCount(Agent1Producer.class);
	}
	
	public int getCountInternationals() {
		return SU.getObjectsCount(Agent2International.class);
	}
	
	public int getCountWholesalers() {
		return SU.getObjectsCount(Agent3Wholesaler.class);
	}
	
	public int getCountRetailers() {
		return SU.getObjectsCount(Agent4Retailer.class);
	}
	
	public int getCountConsumers() {
		return SU.getObjectsCount(Agent5Consumer.class);
	}
	
	public String getLabel() {
		
		if (SU.isInitializing()) {
			return "DataCollector" + ": INITIALIZATION PHASE";
		}
		else {
			return "DataCollector" + ": running phase";
		}
	}
	
	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	public void move() {

		Point newPos = new Point(1, Constants.GRID_HEIGHT - 1);
		Logger.logInfo("DataCollector pos:[" + newPos.x + ", " + newPos.y + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, newPos.x, newPos.y);
	}
}