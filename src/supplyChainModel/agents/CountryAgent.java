package supplyChainModel.agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;

/**
 * Class is used to spawn nodes at the correct country,
 * country objects contain their position, possible spawn locations (countryPoints)
 * and quality. This class can be outfitted with police interventions.
 * @author Maarten Jensen
 */
public class CountryAgent {

	private String name = "none";
	private ArrayList<SCType> scTypes = new ArrayList<SCType>();
	
	private double x;
	private double y;
	private double radius;
	private double lowQualityProb;
	
	private ArrayList<Integer> countryPoints;
	private ArrayList<NdPoint> countryPointsInner;
	private ArrayList<NdPoint> countryPointsOuter;
	private HashMap<String, Integer> countryBorders = new HashMap<String, Integer>();
	
	private Color vsl_color;
	
	public CountryAgent(final Context<Object> context, String countryName, ArrayList<SCType> scTypes, HashMap<String, Integer> countryBorders, double x, double y, double radius, double lowQualityProb) {
		
		context.add(this);
		name = countryName; 
		this.scTypes = scTypes;
		this.countryBorders = countryBorders;
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.lowQualityProb = lowQualityProb;
		createCountryPoints();
		move(this.x, this.y);
		
		vsl_color = new Color(230 + RandomHelper.nextIntFromTo(0, 20), 230 + RandomHelper.nextIntFromTo(0, 20), 230 + RandomHelper.nextIntFromTo(0, 20));
	}

	public BaseAgent spawnAgent(SCType scType) {
		
		if (containsSCType(SCType.PRODUCER) && scType == SCType.PRODUCER && SU.getObjectsCount(Agent1Producer.class) < RepastParam.getProducerNumberCap()) {
			if (RandomHelper.nextDouble() <= 0.5)
				return new Agent1Producer(SU.getContext(), this, Constants.QUALITY_MINIMUM);
			else
				return new Agent1Producer(SU.getContext(), this, Constants.QUALITY_MAXIMUM);
		}
		else if (containsSCType(SCType.INTERNATIONAL) && scType == SCType.INTERNATIONAL) {
			return new Agent2International(SU.getContext(), this);
		}
		else if (containsSCType(SCType.WHOLESALER) && scType == SCType.WHOLESALER) {
			return new Agent3Wholesaler(SU.getContext(), this);
		}
		else if (containsSCType(SCType.RETAIL) && scType == SCType.RETAIL) {
			return new Agent4Retailer(SU.getContext(), this);
		}
		else if (containsSCType(SCType.CONSUMER) && scType == SCType.CONSUMER) {
			if (RandomHelper.nextDouble() <= lowQualityProb)
				return new Agent5Consumer(SU.getContext(), this, Constants.QUALITY_MINIMUM, RandomHelper.nextDoubleFromTo(Constants.PRICE_CONSUMER_BUY_MIN, Constants.PRICE_CONSUMER_BUY_MAX));
			else
				return new Agent5Consumer(SU.getContext(), this, Constants.QUALITY_MAXIMUM, RandomHelper.nextDoubleFromTo(Constants.PRICE_CONSUMER_BUY_MIN, Constants.PRICE_CONSUMER_BUY_MAX));
		}
		return null;
	}

	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	public void move(double x, double y) {

		NdPoint newPos = new NdPoint(x, y);
		Logger.logInfo(name + " new pos:[" + newPos.getX() + ", " + newPos.getY() + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, (int) newPos.getX(), (int) newPos.getY());
	}
	
	public String getLabel()  {
		return name;
	}
	
	private void createCountryPoints() {

		// Create straight down country points
		countryPoints = new ArrayList<Integer>();
		for (int y = (int) (this.y + radius); y > this.y - radius; y --) {
			countryPoints.add(y);
		}
		
		// Create inner country points, for retailer level
		countryPointsInner = new ArrayList<NdPoint>();
		double distance = 0.4 * radius;
		double degreesDif = (360.0/Constants.VSL_RAD_CONVERT)/(Constants.VSL_N_AGENT_RADIUS + 1);
		for (int i = 0; i < Constants.VSL_N_AGENT_RADIUS; i ++) {
			NdPoint countryPoint = new NdPoint(x + Math.cos(i * degreesDif) * distance, y - Math.sin(i * degreesDif) * distance);
			countryPointsInner.add(countryPoint);
		}
		
		// Create outer country points, for consumer level
		countryPointsOuter = new ArrayList<NdPoint>();
		distance = 0.8 * radius;
		for (int i = 0; i < Constants.VSL_N_AGENT_RADIUS; i ++) {
			NdPoint countryPoint = new NdPoint(x + Math.cos(i * degreesDif) * distance, y - Math.sin(i * degreesDif) * distance);
			countryPointsOuter.add(countryPoint);
		}
	}

	public NdPoint getFreePosition(Object toExclude, SCType scType) {
		
		switch(scType) {
		case PRODUCER:
			return getFreePositionStandard(toExclude, scType);
		case INTERNATIONAL:
			return getFreePositionStandard(toExclude, scType);
		case WHOLESALER:
			return getFreePositionStandard(toExclude, scType);
		case RETAIL:
			return getFreePositionInner(toExclude, scType);
		case CONSUMER:
			return getFreePositionOuter(toExclude, scType);
		default:
			return new NdPoint(this.x, this.y - radius/2.0);
		}
	}

	public NdPoint getFreePositionStandard(Object toExclude, SCType scType) {
		
		double xAdjust = 0;
		if (scType == SCType.WHOLESALER)
			xAdjust = radius;
			
		Collections.shuffle(countryPoints);
		for (Integer y : countryPoints) {
		
				if (pointFree(new NdPoint(this.x - xAdjust, y), toExclude)) {
					return new NdPoint(this.x - xAdjust, y);
				}
		}
		Logger.logInfo("No free point in " + name);
		return new NdPoint(this.x - xAdjust, this.y - radius/2);
	}
	
	public NdPoint getFreePositionInner(Object toExclude, SCType scType) {

		Collections.shuffle(countryPointsInner);
		for (NdPoint point : countryPointsInner) {
		
				if (pointFree(new NdPoint(point.getX(), point.getY()), toExclude)) {
					return new NdPoint(point.getX(), point.getY());
				}
		}
		return countryPointsInner.get(0);
	}
	
	public NdPoint getFreePositionOuter(Object toExclude, SCType scType) {

		Collections.shuffle(countryPointsOuter);
		for (NdPoint point : countryPointsOuter) {
			
			if (pointFree(new NdPoint(point.getX(), point.getY()), toExclude)) {
				return new NdPoint(point.getX(), point.getY());
			}
		}
		return countryPointsOuter.get(0);
	}
	
	/**
	 * Check if the point is free of BaseAgents, return true when it is free
	 * @param point
	 * @param toExclude
	 * @return
	 */
	public boolean pointFree(NdPoint point, Object toExclude) {
		
		for (Object o: SU.getContinuousSpace().getObjectsAt(point.getX(), point.getY())) {
			//Logger.logInfo("Found object of class: " + o.getClass());
			if (o != toExclude && o instanceof BaseAgent)
				return false;
		}
		return true;
	}
	
	/*================================
	 * Getters and setters
	 *===============================*/
	public boolean containsSCType(SCType scType) {

		if (scTypes.contains(scType))
			return true;
		else
			return false;
	}
	
	public int retrieveBordersN(String otherCountry) {
		if (countryBorders.containsKey(otherCountry)) {
			return countryBorders.get(otherCountry);
		}
		else {
			Logger.logError("CountryAgent.retrieveBordersN(): countryBorders for country:" + name + " does not contain country:" + otherCountry);
			return -1;
		}
	}
	
	public HashMap<String, Integer> getCountryBorders() {
		return countryBorders;
	}
	
	public String getCountryBordersStr() {
		return countryBorders.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public double getQuality() {
		return lowQualityProb;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public Color getColor() {
		return vsl_color;
	}
}