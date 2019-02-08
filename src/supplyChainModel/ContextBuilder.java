package supplyChainModel;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import supplyChainModel.agents.AgentConsumer;
import supplyChainModel.agents.AgentDistributor;
import supplyChainModel.agents.AgentProducer;
import supplyChainModel.agents.CountryAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.ContextDataLoader;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;

public class ContextBuilder implements repast.simphony.dataLoader.ContextBuilder<Object> {
	
	/**
	 * This function builds the simulation, loading text files should be done in here.
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		
		createContinuousSpace(context);
		createGrid(context);
		createNetwork(context);
		createNetworkReversed(context);
		
		SU.setContext(context);
		SU.resetId();
		
		RepastParam.setRepastParameters();
		
		// Create the supply chain
		ContextDataLoader countryLoader = new ContextDataLoader();
		countryLoader.readFullFile(context,"./input","contextBuildInformation");
		supplyChainCreation(context);
		
		// If running in batch mode, tell the scheduler when to end each run.
		if (RunEnvironment.getInstance().isBatch()){
			
			double endAt = RepastParam.getRunLength();
			RunEnvironment.getInstance().endAt(endAt);
		}
		
		return context;
	}
	
	/*====================================
	 * Main steps
	 *====================================*/
	
	private void supplyChainCreation(final Context<Object> context) {
		
		if (SCType.getScLayers() <= 2)
			Logger.logError("To few supply chain layers, minimum of 3 required:" + SCType.getScLayers());
		
		ArrayList<CountryAgent> countryAgents = SU.getObjectsAll(CountryAgent.class);
		
		for (CountryAgent country : countryAgents) {
			
			if (country.containsSCType(SCType.PRODUCER)) {
				for (int i = 0; i < 8; i++) {
					new AgentProducer(context, SCType.PRODUCER, country);
				}
			}
			
			if (country.containsSCType(SCType.INTERNATIONAL)) {
				for (int i = 0; i < 4; i++) {
					 new AgentDistributor(context, "I", SCType.INTERNATIONAL, country);
				}
			}
			
			if (country.containsSCType(SCType.WHOLESALER)) {
				for (int i = 0; i < 2; i++) {
					new AgentDistributor(context, "W", SCType.WHOLESALER, country);
				}
			}
			
			if (country.containsSCType(SCType.RETAIL)) {
				for (int i = 0; i < 1; i++) {
					new AgentDistributor(context, "R", SCType.RETAIL, country);
				}
			}
			
			if (country.containsSCType(SCType.CONSUMER)) {
				for (int i = 0; i < 2; i++) {
					new AgentConsumer(context, SCType.CONSUMER, country);
				}
			}
		}
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