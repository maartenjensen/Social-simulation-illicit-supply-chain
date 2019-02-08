package supplyChainModel.common;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.SimUtilities;

// Simulation Utils class
public class SU {

	private static Context<Object> masterContext = null; // MasterContext is saved because the SU class cannot call the context
	private static int agentId = -1;
	/**
	 * Set masterContext
	 */
	public static void setContext(Context<Object> masterContext) {
		SU.masterContext = masterContext;
	}
	
	public static void resetId() {
		agentId = 0;
	}
	
	public static int getNewId() {
		if (agentId >= 0)
			return agentId ++;
		else {
			Logger.logError("SU.getNewId(): agentId == -1, resetId() not called");
			return -1;
		}
	}
	
	/**
	 * Gets master context since I don't use the sub-contexts
	 * @return Context the master context
	 */
	public static Context<Object> getContext() {

		if (masterContext == null)  {
			Logger.logError("SimUtils.getContext(): context returned null");
		}
		return masterContext;
	}
	
	/**
	 * Retrieves all the objects within the master context based on the given class.
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAll(Class<T> clazz) {
		
		@SuppressWarnings("unchecked")
		final Iterable<T> objects = (Iterable<T>) getContext().getObjects(clazz);
		final ArrayList<T> objectList = new ArrayList<T>();
		for (final T object : objects) {
			objectList.add(object);
		}
		return objectList;
	}
	
	/**
	 * Same as getObjectsAll but uses SimUtilities.shuffle to randomize
	 * the ArrayList of objects
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAllRandom(Class<T> clazz) {
		
		ArrayList<T> objectList = getObjectsAll(clazz);
		SimUtilities.shuffle(objectList, RandomHelper.getUniform());
		return objectList;
	}
	
	/**
	 * Returns the angle in degrees
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double point_direction(double x1, double y1, double x2, double y2) {
		return Math.atan2(y2 - y1, x2 - x1);
	}
	
	public static double point_distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt( Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) );
	}
	
	
	
	public static Grid<Object> getGrid() {
		@SuppressWarnings("unchecked")
		Grid<Object> grid = (Grid<Object>) getContext().getProjection(Constants.ID_GRID);
		if (grid == null)  {
			Logger.logError("SU.getGrid(): grid returned null (ID:" + Constants.ID_GRID + ")");
		}
		return grid;
	}
	
	public static ContinuousSpace<Object> getContinuousSpace() {
		@SuppressWarnings("unchecked")
		ContinuousSpace<Object> continuousSpace = (ContinuousSpace<Object>) getContext().getProjection(Constants.ID_CONTINUOUS_SPACE);
		if (continuousSpace == null)  {
			Logger.logError("SU.getContinuousSpace(): continouse returned null (ID:" + Constants.ID_CONTINUOUS_SPACE + ")");
		}
		return continuousSpace;
	}
	
	public static Network<Object> getNetworkSC() {
		@SuppressWarnings("unchecked")
		Network<Object> network = (Network<Object>) SU.getContext().getProjection(Constants.ID_SC_NETWORK);
		if (network == null)  {
			Logger.logError("SU.getNetworkSC(): network returned null (ID:" + Constants.ID_SC_NETWORK + ")");
		}
		return network;
	}
	
	public static Network<Object> getNetworkSCReversed() {
		@SuppressWarnings("unchecked")
		Network<Object> network = (Network<Object>) SU.getContext().getProjection(Constants.ID_SC_NETWORK_REVERSED);
		if (network == null)  {
			Logger.logError("SU.getNetworkSCReversed(): network returned null (ID:" + Constants.ID_SC_NETWORK_REVERSED + ")");
		}
		return network;
	}
}
