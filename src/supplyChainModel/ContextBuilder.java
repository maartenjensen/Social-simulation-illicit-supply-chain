package supplyChainModel;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import supplyChainModel.agents.Agent5Consumer;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.agents.CountryAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

/**
 * The ContextBuilder is the main class of the simulation program.
 * The function build is run upon pressing the start button in the 
 * repast HUD.
 * @author Maarten Jensen
 *
 */
public class ContextBuilder implements repast.simphony.dataLoader.ContextBuilder<Object> {
	
	private ContextPopulationLoader populationLoader;
	
	/**
	 * This function builds the simulation, loading text files should be done in here.
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		
		Logger.logMain("------------------------------------------------------------------------------");
		Logger.logMain("Running ContextBuilder.build");
		Logger.logMain("------------------------------------------------------------------------------");
		createContinuousSpace(context);
		createGrid(context);
		createNetwork(context);
		createNetworkReversed(context);
		
		context.add(this); //Used for ScheduledMethod
		SU.setContext(context);
		SU.resetId();
		
		RepastParam.setRepastParameters();
		
		new DataCollector(context);
		//BatchRunDataSave.resetData();
		
		// Create the supply chain
		countryCreation(context);
		agentsCreation(context);
		
		// If running in batch mode, tell the scheduler when to end each run.
		if (SU.isBatchRun()) {
			double endAt = RepastParam.getRunLength();
			RunEnvironment.getInstance().endAt(endAt);
			Logger.logBatch("Started a new batch run!");
		}
		
		return context;
	}

	private void countryCreation(final Context<Object> context) {
		
		ContextDataLoader countryLoader = new ContextDataLoader();
		if (RepastParam.getRealisticMap())
			countryLoader.readFullFile(context,"./data", "inputDataCountryRealistic.csv", "inputDataBorders.csv");
		else
			countryLoader.readFullFile(context, "./data", "inputDataCountryBetterView2.csv", "inputDataBorders.csv");
	}
	
	private void agentsCreation(final Context<Object> context) {
		
		populationLoader = new ContextPopulationLoader();
		
		if (!RepastParam.getSettingLoadPopulationFile()) {
			populationLoader.generatePopulation();
		}
		else {
			populationLoader.generatePopulation("./data/population.txt");
		}		
	}

	/*====================================
	 * Step method
	 *====================================*/
	/**
	 * Step method, called from ContextBuilder to have more control
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 0, shuffle=false)
	public void step() {
		
		Logger.logMain("----------------------------------");
		Logger.logMain("Step " + SU.getTick());
		Logger.logMain("----------------------------------");
		
		/*Logger.logMain("Step-Shipment: police intervention on shipments");
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			shipment.stepRemoval();
		}*/
		
		Logger.logMain("Step-BaseAgent: remove bankrupt agents");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepCheckRemoval();
		}

		Logger.logMain("Step-BaseAgent: reset output parameters");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepResetParameters();
		}
		
		Logger.logMain("Step-ContextBuilder: spawning of new agents");
		stepSpawning();
		
		Logger.logMain("Step-Shipment: shipments advancements");
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			shipment.stepAdvanceShipment();
		}
		
		Logger.logMain("Step-Shipment: orders advancements");
		for (Order order : SU.getObjectsAll(Order.class)) {
			order.stepAdvanceOrder();
		}
		
		//Temporary
		Logger.logMain("Step-Agent5Consumer: temporary receive income");
		for (Agent5Consumer producer : SU.getObjectsAll(Agent5Consumer.class)) {
			producer.stepReceiveIncome();
		}
		
		Logger.logMain("Step-BaseAgent: receive shipment from suppliers");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepProcessArrivedShipments();
		}
		
		Logger.logMain("Step-BaseAgent: choose suppliers and buyers");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepChooseSuppliersAndClients();
		}
		
		Logger.logMain("Step-BaseAgent: send shipment to clients");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepSendShipment();
		}
		
		Logger.logMain("Step-ContextBuilder: step intervention on shipment Global: " + RepastParam.getInterventionGlobalPercentage() + "%");
		stepInterventionOnShipment();
		
		Logger.logMain("Step-ContextBuilder: step intervention on shipment NL: " + RepastParam.getInterventionWholesalerNLPercentage() + "%");
		stepInterventionNLWholesaler();
		
		Logger.logMain("Step-BaseAgent: send order");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepSendOrder();
		}
		
		Logger.logMain("Step-BaseAgent: add to data");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepAddToData();
		}
		
		if (SU.getTick() == RepastParam.getRunLength()) {
			
			Logger.logMain("------------------------------------------------------------------------------");
			if (!SU.isBatchRun())
				saveRelations();
			
			if (!RepastParam.getSettingLoadPopulationFile())
				populationLoader.savePopulation("./data/population.txt");

			//if (SU.isBatchRun()) {
			//	BatchRunDataSave.saveData(Constants.DATA_PATH + "/output/BatchOutput/BatchRunData" + SU.getCurrentDateTime() + ".txt",
			//							  RepastParam.getEnablePersonalRisk(), SU.getDataCollector().getStockConsumedTot());
			//}
			
			RunEnvironment.getInstance().pauseRun();
			Logger.logMain("Simulation ended at : " + SU.getTick());
			Logger.logMain("------------------------------------------------------------------------------");
		}
	}

	public void saveRelations() {
		
		String filePathAndName = Constants.DATA_PATH;
		if (SU.isBatchRun())
			filePathAndName = "/output/RelationsData" + SU.getCurrentDateTime() + ".txt";
		else
			filePathAndName += "/output/BatchOutput/RelationsData" + SU.getCurrentDateTime() + ".txt";
		Logger.logMain("Relations information saved in: " + filePathAndName);
		List<String> data = new ArrayList<String>();
		data = SU.getDataCollector().getRelationsData();
		SU.writeToFile(filePathAndName, data);
	}
	
	public void stepSpawning() {
		
		if (SU.isInitializing())
			return ;
		
		for (SCType scType : SCType.values()) {
			
			if ((RandomHelper.nextDouble() <= Constants.SPAWN_RATE && scType != SCType.CONSUMER) || 
				(RandomHelper.nextDouble() <= Constants.SPAWN_RATE_CONSUMERS && scType == SCType.CONSUMER)) {
				
				for (CountryAgent country : SU.getObjectsAllRandom(CountryAgent.class)) {
					if (country.containsSCType(scType)) {
						BaseAgent agent = country.spawnAgent(scType);
						if (agent != null)
							SU.getDataCollector().addIdCurrentStock(agent.getId());
						break;
					}
				}
			}
		}
	}

	/**
	 * Only intervene on shipments that have a supplier
	 */
	public void stepInterventionOnShipment() {
		
		if (!(RepastParam.getInterventionGlobalPercentage() > 0) || RepastParam.getInterventionType().equals("none") || SU.getTick() <= RepastParam.getSettingInitializeTime())
			return ;
		
		double interventionProbability = ((double) RepastParam.getInterventionGlobalPercentage()) / Constants.SHIPMENT_STEP * 0.01;
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			if (RandomHelper.nextDouble() <= interventionProbability && shipment.getSupplier() != null) {
				interventShipment(shipment, "Global");
			}
		}
	}
	
	public void stepInterventionNLWholesaler() {
		if (!(RepastParam.getInterventionWholesalerNLPercentage() > 0) || SU.getTick() <= RepastParam.getSettingInitializeTime())
			return ;
			
		double interventionProbabilityNL = ((double) RepastParam.getInterventionWholesalerNLPercentage()) / Constants.SHIPMENT_STEP * 0.01;
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			if (RandomHelper.nextDouble() <= interventionProbabilityNL && shipment.getSupplier() != null &&
				shipment.getClient().getCountry().getName().equals("NL & B") && shipment.getClient().getScType() == SCType.WHOLESALER) {
				
				interventShipment(shipment, "NL");
				SU.getDataCollector().addShipmentNLIntervenedCount();
				SU.getDataCollector().addShipmentNLIntervenedSize(shipment.getRealSize());
			}
		}
	}
	
	public void interventShipment(Shipment shipment, String message) {
		
		SU.getDataCollector().addDeletedStock(shipment.getGoods());
		Logger.logInfo("stepInterventionOnShipment:" + message + " (" + shipment.getIdSupplier() + " -> " + shipment.getIdClient() + ") shipment:" + shipment.toString());
		SU.getDataCollector().addShipmentIntervenedCount();			
		interventShipmentRisk(shipment, shipment.getSupplier(), shipment.getClient());		
		shipment.remove();
	}
	
	/**
	 * This function should not be called when the shipment does not have a
	 * @param shipment
	 */
	public void interventShipmentRisk(Shipment shipment, BaseAgent supplier, BaseAgent client) {
		
		if (!RepastParam.getEnablePersonalRisk())
			return ;
		
		supplier.increaseRisk(Constants.PS_INTERVENED_SHIPMENT);
		client.increaseRisk(Constants.PS_INTERVENED_SHIPMENT_OTHER);
		
		CountryAgent countryC = client.getCountry();
		
		if (RepastParam.getInterventionType().equals("single")) 
			return ;
				
		for (BaseAgent agent : SU.getObjectsAllRandom(BaseAgent.class)) {
			if (agent.getCountry() == countryC && agent != client) {
				if ((RepastParam.getInterventionType().equals("high") && RandomHelper.nextDouble() <= Constants.PS_INTERVENTION_SPREAD_HIGH) ||
					(RepastParam.getInterventionType().equals("low") && RandomHelper.nextDouble() <= Constants.PS_INTERVENTION_SPREAD_LOW)) {
					Logger.logInfo("ContextBuilder.interventShipmentRisk() increase-risk of other:" + agent.getNameId() );
					agent.increaseRisk(Constants.PS_INTERVENED_SHIPMENT_OTHER);
				}
			}
		}
	}
	
	/**
	 * DEAD CODE
	 * @param supplierId
	 * @param clientId
	 * @return
	 */
	public boolean intercept(int supplierId, int clientId) {
		
		/*
		if (SU.getTick() >= RepastParam.getInterventionFromStep()) {
			if (SU.getTick() == RepastParam.getInterventionFromStep()) 
				Logger.logInfo("Intercept:stepInterventionOnShipment on this tick (" + RepastParam.canIntercept() + ")");
			else
				Logger.logInfo("Intercept:stepInterventionOnShipment on a > tick (" + RepastParam.canIntercept() + ")");
			
			if (intercept(RepastParam.getInterventionSupplierId(), RepastParam.getInterventionClientId())) {
				
				RepastParam.interceptionCountUpdate();
			}
		}*/
		
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			if (shipment.getIdSupplier() == supplierId && shipment.getIdClient() == clientId) {
				SU.getDataCollector().addDeletedStock(shipment.getGoods());
				shipment.remove();
				Logger.logInfo("Intercept:intercept(" + supplierId + "," + clientId + ") shipment:" + shipment.toString());
				return true;
			}				
		}
		Logger.logInfo("Intercept:intercept(" + supplierId + "," + clientId + ") not performed, no possible shipments");
		return false;
	}
	
	/**
	 * Create continuous space space for the given context
	 * @param context
	 * @return
	 */
	private ContinuousSpace<Object> createContinuousSpace(final Context<Object> context) {
		
		final ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null).
				createContinuousSpace( 	Constants.ID_CONTINUOUS_SPACE, context,
										new RandomCartesianAdder<Object>(),
										new repast.simphony.space.continuous.BouncyBorders(),
										Constants.GRID_WIDTH, Constants.GRID_HEIGHT);
		return space;
	}

	/**
	 * Create grid factory for the given context
	 * @param context
	 * @return
	 */
	private Grid<Object> createGrid(final Context<Object> context) {
		
		final Grid<Object> grid = GridFactoryFinder.createGridFactory(null).createGrid(
										Constants.ID_GRID, context,
										new GridBuilderParameters<Object>(
										new repast.simphony.space.grid.BouncyBorders(),
										new SimpleGridAdder<Object>(), true,
										Constants.GRID_WIDTH, Constants.GRID_HEIGHT));
		return grid;
	}

	/**
	 * Create network for the given context, this networks shows the trust relation from supplier to buyer
	 * @param context
	 * @return
	 */
	private Network<Object> createNetwork(final Context<Object> context) {
		
		NetworkBuilder <Object> netBuilder = new NetworkBuilder <Object> (Constants.ID_SC_NETWORK, context , true );
		netBuilder.buildNetwork();
		@SuppressWarnings("unchecked")
		Network<Object> net = (Network <Object>) context.getProjection(Constants.ID_SC_NETWORK);
		return net;
	}

	/**
	 * Create reversed network for the given context, this network shows the trust relation from buyer to supplier
	 * @param context
	 * @return
	 */
	private Network<Object> createNetworkReversed(final Context<Object> context) {
		
		NetworkBuilder <Object> netBuilder = new NetworkBuilder <Object> (Constants.ID_SC_NETWORK_REVERSED, context , true );
		netBuilder.buildNetwork();
		@SuppressWarnings("unchecked")
		Network<Object> net = (Network <Object>) context.getProjection(Constants.ID_SC_NETWORK_REVERSED);
		return net;
	}
}