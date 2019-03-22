package supplyChainModel;

import java.awt.Point;
import java.util.ArrayList;

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

	
	public DataCollector(final Context<Object> context) {
		context.add(this);
		move();
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