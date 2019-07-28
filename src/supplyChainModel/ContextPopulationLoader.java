package supplyChainModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import frameworkTrust.RelationC;
import frameworkTrust.RelationS;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.agents.Agent1Producer;
import supplyChainModel.agents.Agent2International;
import supplyChainModel.agents.Agent3Wholesaler;
import supplyChainModel.agents.Agent4Retailer;
import supplyChainModel.agents.Agent5Consumer;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.agents.CountryAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;

public class ContextPopulationLoader {

	/**
	 * Generates a new population from the given constants
	 */
	public void generatePopulation() {
		
		if (SCType.getScLayers() <= 2)
			Logger.logError("To few supply chain layers, minimum of 3 required:" + SCType.getScLayers());
		
		ArrayList<CountryAgent> countryAgents = SU.getObjectsAll(CountryAgent.class);		
		for (CountryAgent country : countryAgents) {
			
			if (country.containsSCType(SCType.PRODUCER)) {
				for (int i = 0; i < Constants.N_PRODUCERS; i++) {
					country.spawnAgent(SCType.PRODUCER);
				}
			}
			
			if (country.containsSCType(SCType.INTERNATIONAL)) {
				for (int i = 0; i < Constants.N_INTERNATIONALS; i++) {
					country.spawnAgent(SCType.INTERNATIONAL);
				}
			}
			
			if (country.containsSCType(SCType.WHOLESALER)) {
				for (int i = 0; i < Constants.N_WHOLESALERS; i++) {
					country.spawnAgent(SCType.WHOLESALER);
				}
			}
			
			if (country.containsSCType(SCType.RETAIL)) {
				for (int i = 0; i < Constants.N_RETAILERS; i++) {
					country.spawnAgent(SCType.RETAIL);
				}
			}
			
			if (country.containsSCType(SCType.CONSUMER)) {
				for (int i = 0; i < Constants.N_CONSUMERS; i++) {
					country.spawnAgent(SCType.CONSUMER);
				}
			}
		}
		
		// Set possible new suppliers and clients
		for (BaseAgent agent : SU.getObjectsAll(BaseAgent.class)) {
			agent.setPossibleNewSuppliersAndClients();
		}
		
		SU.getDataCollector().addAllCurrentStock();
	}
	
	/**
	 * Generates a population from the given file
	 * @param filePathAndName
	 */
	public void generatePopulation(String filePathAndName) {
		
		List<String> dataAll = SU.readFile(filePathAndName);
		List<String> dataAgents = new ArrayList<String>();
		List<String> dataStock = new ArrayList<String>();
		List<String> dataRelations = new ArrayList<String>();
		
		int typeOfData = -1;
		for (String datum : dataAll) {
			if (!datum.startsWith("%")) {
				switch(typeOfData) {
				case 0:	dataAgents.add(datum); break;
				case 1: dataStock.add(datum); break;
				case 2: dataRelations.add(datum); break;
				}
			}
			else {
				typeOfData = Integer.parseInt(datum.substring(1,2));
			}
		}
		
		generateAgents(dataAgents);
		generateStock(dataStock);
		generateRelations(dataRelations);
		
		SU.getDataCollector().addAllCurrentStock();
	}
	
	private void generateAgents(List<String> dataAgents) {
		
		for (String agentString : dataAgents) {
			if (!agentString.startsWith("%")) {
				List<String> agentVars = Arrays.asList(agentString.split(","));

				createAgent(Integer.parseInt(agentVars.get(0)), agentVars.get(1), agentVars.get(2), new NdPoint(Double.parseDouble(agentVars.get(3)), Double.parseDouble(agentVars.get(4))),
						Double.parseDouble(agentVars.get(5)), Double.parseDouble(agentVars.get(6)), Double.parseDouble(agentVars.get(7)), Double.parseDouble(agentVars.get(8)),
						Double.parseDouble(agentVars.get(9)), Double.parseDouble(agentVars.get(10)), Double.parseDouble(agentVars.get(11)), Double.parseDouble(agentVars.get(12)),
						Double.parseDouble(agentVars.get(13)), Integer.parseInt(agentVars.get(14)), Byte.parseByte(agentVars.get(15)), Double.parseDouble(agentVars.get(16)) );
			}
		}
		
		// Set possible new suppliers and clients
		for (BaseAgent agent : SU.getObjectsAll(BaseAgent.class)) {
			agent.setPossibleNewSuppliersAndClients();
		}
	}
	
	private void generateStock(List<String> dataStock) {
		
		for (String stockString : dataStock) {
			List<String> sVars = Arrays.asList(stockString.split(","));
			BaseAgent agent = SU.getBaseAgent(Integer.parseInt(sVars.get(0)));
			HashMap<Byte, Double> goods = new HashMap<Byte, Double>();
			for (int i = 0; i < Integer.parseInt(sVars.get(1)); i ++) {
				goods.put(Byte.parseByte(sVars.get(2+i*2)), Double.parseDouble(sVars.get(3+i*2)));
			}
			agent.addToStock(goods);
		}
	}
	
	private void generateRelations(List<String> dataRelations) {
		
		for (String relationString : dataRelations) {
			List<String> rVars = Arrays.asList(relationString.split(","));
			if (rVars.get(0).equals("S")) {
				BaseAgent agent = SU.getBaseAgent(Integer.parseInt(rVars.get(1)));
				BaseAgent supplier = SU.getBaseAgent(Integer.parseInt(rVars.get(2)));
				agent.addSupplier(supplier);			
			}
			else if (rVars.get(0).equals("C")) {
				BaseAgent agent = SU.getBaseAgent(Integer.parseInt(rVars.get(1)));
				BaseAgent client = SU.getBaseAgent(Integer.parseInt(rVars.get(2)));
				agent.addClient(client);	
			}
		}
	}
	
	/**
	 * 
	 * @param id 0
	 * @param countryAgentName 1
	 * @param scType 2
	 * @param newPos 3, 4
	 * @param money 5
	 * @param sellPrice 6
	 * @param averageBuyCost 7
	 * @param profitPercentage 8
	 * @param maxPackageSize 9
	 * @param securityStockMultiplier 10
	 * @param personalRisk 11
	 * @param personalRiskThreshold 12
	 * @param desperation 13
	 * @param inactivityTimer 14
	 * @param quality 15
	 * @param spendableMoney 16
	 */
	private void createAgent(int id, String countryAgentName, String scType, NdPoint newPos, double money, double sellPrice, double averageBuyCost, double profitPercentage,
							 double maxPackageSize, double securityStockMultiplier, double personalRisk, double personalRiskThreshold, double desperation, int inactivityTimer,
							 byte quality, double spendableMoney) {

		SU.setHigherId(id + 1);
		
		CountryAgent countryAgent = SU.getCountryAgent(countryAgentName);
		switch(scType) {
		case "P":		new Agent1Producer(SU.getContext(), id, countryAgent, newPos, money, sellPrice, averageBuyCost, profitPercentage, maxPackageSize,
										   securityStockMultiplier, personalRisk, personalRiskThreshold, desperation, inactivityTimer, quality);	break;
		case "I":		new Agent2International(SU.getContext(), id, countryAgent, newPos, money, sellPrice, averageBuyCost, profitPercentage, maxPackageSize,
												securityStockMultiplier, personalRisk, personalRiskThreshold, desperation, inactivityTimer); 		break;
		case "W":		new Agent3Wholesaler(SU.getContext(), id, countryAgent, newPos, money, sellPrice, averageBuyCost, profitPercentage, maxPackageSize,
				   							 securityStockMultiplier, personalRisk, personalRiskThreshold, desperation, inactivityTimer);			break;
		case "R":		new Agent4Retailer(SU.getContext(), id, countryAgent, newPos, money, sellPrice, averageBuyCost, profitPercentage, maxPackageSize,
				   						   securityStockMultiplier, personalRisk, personalRiskThreshold, desperation, inactivityTimer);				break;
		case "C":		new Agent5Consumer(SU.getContext(), id, countryAgent, newPos, money, sellPrice, averageBuyCost, profitPercentage, maxPackageSize,
				   						   securityStockMultiplier, personalRisk, personalRiskThreshold, desperation, inactivityTimer, quality, spendableMoney); 	break;
		}
	}

	public void savePopulation(String filePathAndName) {
		
		Logger.logInfo("ContextPopulationLoader.savePopulation() file path and name:" + filePathAndName);
		List<String> dataAgents = new ArrayList<String>();

		dataAgents.add("%0:0id,1countryName,2SCTypeChar,3x,4y,5money,6sellPrice,7averageBuyCost,8profitPercentage,9maxPackageSize,10securityStockMultiplier,11personalRisk,12personalRiskThreshold,13desperation,14inactivityTimer,15quality");
		for (BaseAgent agent : SU.getObjectsAll(BaseAgent.class)) {
			dataAgents.add(agent.getVarsAsString());
		}
		
		List<String> dataStock = getDataStock();
		List<String> dataRelations = getDataRelations();
		
		List<String> data = new ArrayList<String>();
		data.addAll(dataAgents);
		data.addAll(dataStock);
		data.addAll(dataRelations);
		
		SU.writeToFile(filePathAndName, data);
	}
	
	public List<String> getDataStock() {
		
		List<String> data = new ArrayList<String>();
		data.add("%1Stock:agent,n,qual1,quan1,qual2,qual2,etc.");
		for (BaseAgent agent : SU.getObjectsAll(BaseAgent.class)) {
			String str = "";
			HashMap<Byte, Double> stock = agent.getStock();
			for (byte quality : stock.keySet()) {
				str += "," + quality + "," + stock.get(quality);
			}
			data.add(agent.getId() + "," + stock.keySet().size() + str);
		}
		return data;
	}
	
	public List<String> getDataRelations() {
		
		List<String> data = new ArrayList<String>();
		data.add("%2Relations:agent1,agent2");
		for (BaseAgent agent : SU.getObjectsAll(BaseAgent.class)) {
			HashMap<Integer, RelationS> relationsS = agent.getRelationsS();
			for(Integer otherId : relationsS.keySet()) {
				data.add("S," + agent.getId() + "," + otherId);
			}
			HashMap<Integer, RelationC> relationsC = agent.getRelationsC();
			for(Integer otherId : relationsC.keySet()) {
				data.add("C," + agent.getId() + "," + otherId);
			}
		}
		return data;
	}
}