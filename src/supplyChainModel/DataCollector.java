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
import supplyChainModel.enums.SCType;

public class DataCollector {

	//private HashMap<SCType, Integer> removedAgents = ;
	private HashMap<Byte, Double> createdStock = new HashMap<Byte, Double>();
	private HashMap<Byte, Double> deletedStock = new HashMap<Byte, Double>();
	private HashMap<Byte, Double> consumedStock = new HashMap<Byte, Double>();
	
	protected List<String> relationsInfo = new ArrayList<String>();
	
	public DataCollector(final Context<Object> context) {
		context.add(this);
		relationsInfo.add("\"tick\",\"Id\",\"OtherId\",\"Type\",\"Trust\"");
		move();
	}
	
	/*================================
	 * Stock analyzing
	 *===============================*/	
	
	public void addAllCurrentStock() {
		
		ArrayList<BaseAgent> agents = SU.getObjectsAll(BaseAgent.class);
		for (BaseAgent agent : agents) {
			
			for (Byte quality : agent.getStock().keySet()) {
				if (createdStock.containsKey(quality))
					createdStock.put(quality, createdStock.get(quality) + agent.getStock().get(quality));
				else
					createdStock.put(quality, agent.getStock().get(quality));
			}
		}
	}
	
	public void addProducedStock(HashMap<Byte, Double> producedGoods) {
		
		for (Byte quality : producedGoods.keySet()) {
			if (createdStock.containsKey(quality))
				createdStock.put(quality, createdStock.get(quality) + producedGoods.get(quality));
			else
				createdStock.put(quality, producedGoods.get(quality));
		}
	}

	public void addConsumedStock(HashMap<Byte, Double> consumedGoods) {
		
		for (Byte quality : consumedGoods.keySet()) {
			if (consumedStock.containsKey(quality))
				consumedStock.put(quality, consumedStock.get(quality) + consumedGoods.get(quality));
			else
				consumedStock.put(quality, consumedGoods.get(quality));
		}
	}
	
	public void addDeletedStock(HashMap<Byte, Double> deletedGoods) {
		
		for (Byte quality : deletedGoods.keySet()) {
			if (deletedStock.containsKey(quality))
				deletedStock.put(quality, deletedStock.get(quality) + deletedGoods.get(quality));
			else
				deletedStock.put(quality, deletedGoods.get(quality));
		}
	}
	
	public double getStockCreatedTot() {
		if (createdStock.containsKey(Constants.QUALITY_MINIMUM)) {
			if (createdStock.containsKey(Constants.QUALITY_MINIMUM))
				return createdStock.get(Constants.QUALITY_MINIMUM) + createdStock.get(Constants.QUALITY_MAXIMUM);
			else
				return createdStock.get(Constants.QUALITY_MINIMUM);
		}
		else if (createdStock.containsKey(Constants.QUALITY_MAXIMUM))
			return createdStock.get(Constants.QUALITY_MAXIMUM);
		else
			return 0.0;
	}
	
	public double getStockConsumedTot() {
		if (consumedStock.containsKey(Constants.QUALITY_MINIMUM)) {
			if (consumedStock.containsKey(Constants.QUALITY_MINIMUM))
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
	
	/*================================
	 * Other
	 *===============================*/
	public void addRelationData(String relationInfo) {
		relationsInfo.add(relationInfo);
	}
	
	public List<String> getRelationsData() {
		return relationsInfo;
	}
	
	public double getStockCurrent(String countryName) {
		
		double stock = 0;
		ArrayList<BaseAgent> agents = SU.getObjectsAll(BaseAgent.class);
		for (BaseAgent agent : agents) {
			
			if (agent.getCountry().getName().equals(countryName)) {
				stock += 0; //TODO get the right information agent.getStock();
			}
		}
		
		return stock;
	}

	public double getStockCurrentNL() {
		return getStockCurrent("The Netherlands");
	}

	public double getStockCurrentES() {
		return getStockCurrent("Spain");
	}
	
	public double getCurrentImport(String countryName, SCType importLayer) {
		
		double stock = 0;
		ArrayList<BaseAgent> agents = SU.getObjectsAll(BaseAgent.class);
		for (BaseAgent agent : agents) {
			
			if (agent.getCountry().getName().equals(countryName) && agent.getScType() == importLayer) {
				stock += agent.getCurrentImport();
			}
		}
		return stock;
	}
	
	public double getImportCurrentNL() {
		return getCurrentImport("The Netherlands", SCType.WHOLESALER);
	}
	
	public double getImportCurrentES() {
		return getCurrentImport("Spain", SCType.WHOLESALER);
	}
	
	public double getTotalImport(String countryName, SCType importLayer) {
		
		double stock = 0;
		ArrayList<BaseAgent> agents = SU.getObjectsAll(BaseAgent.class);
		for (BaseAgent agent : agents) {
			
			if (agent.getCountry().getName().equals(countryName) && agent.getScType() == importLayer) {
				stock += agent.getTotalImport();
			}
		}
		return stock;
	}
	
	public double getTotalProductionSouthAmerica() {
		return getTotalImport("South America", SCType.PRODUCER);
	}
	
	public double getTotalImportInternational() {
		return getTotalImport("International", SCType.INTERNATIONAL);
	}
	
	public double getTotalImportNL() {
		return getTotalImport("The Netherlands", SCType.WHOLESALER);
	}
	
	public double getTotalImportES() {
		return getTotalImport("Spain", SCType.WHOLESALER);
	}
	
	public double getTotalImportUK() {
		return getTotalImport("United Kingdom", SCType.RETAIL);
	}
	
	public double getTotalImportD() {
		return getTotalImport("Germany", SCType.RETAIL);
	}
	
	public double getTotalImportF() {
		return getTotalImport("France", SCType.RETAIL);
	}
	
	public double getTotalImportI() {
		return getTotalImport("Italy", SCType.RETAIL);
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