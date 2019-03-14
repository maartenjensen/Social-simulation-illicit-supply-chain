package supplyChainModel.agents;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;

public class Agent5Consumer extends BaseAgent {

	public double baseConsumption;
	public boolean satisfied;
	public int ticksUntilRemoved;
	public int ticksWithoutSatisfaction;
	
	public Agent5Consumer(final Context<Object> context, CountryAgent country) {
		super(country, SCType.CONSUMER, 0, 0);
		
		baseConsumption = RandomHelper.nextDoubleFromTo(RepastParam.getConsumptionMin(), RepastParam.getConsumptionMax());
		satisfied = false;
		ticksUntilRemoved = Constants.CONSUMER_REMOVE_TICKS;
		ticksWithoutSatisfaction = 0;
	}
	
	/*====================================
	 * The main steps of the agents
	 *====================================*/
	
	/**
	 * Adjusted to change removal based on life and rehab instead of from bankruptcy
	 * Can't die when initializing
	 */
	@Override
	public void stepCheckRemoval() {
		
		ticksUntilRemoved -= 1;
		
		if (!SU.getIsInitializing() && (ticksUntilRemoved == 0 || ticksWithoutSatisfaction >= Constants.CONSUMER_LIMIT_WITHOUT_SATISFACTION)) {
			remove();
		}
	}
	
	/**
	 * Gain money
	 */
	public void stepReceiveIncome() {
		
		money += Constants.PRICE_CONSUMER_INCOME;
	}
	
	@Override
	public void stepProcessArrivedShipments() {
		
	}

	@Override
	public void stepChooseSuppliersAndClients() {
		searchSuppliers();
	}
	
	/**
	 * Consume instead of sending shipment
	 */
	@Override
	public void stepSendShipment() {
		/*
		if (stock >= baseConsumption) {
			Logger.logInfoId(id, getNameId() + ":" + stock + " - " + baseConsumption + " = " + (stock - baseConsumption));
			stock -= baseConsumption;
			ticksWithoutSatisfaction = 0;
			satisfied = true;
		}
		else {
			stock = 0;
			ticksWithoutSatisfaction ++;
			satisfied = false;
		}*/
	}
	
	@Override
	public void stepSendOrder() {
		//sendOrders();
	}
	
	
	/**
	 * This function is 
	 * @param size
	 */
	/*@Override
	public void receivePackage(BaseAgent supplier, double size, double price) {
		// Update trust relation
		if (trustOther.containsKey(supplier.getId()))
			trustOther.get(supplier.getId()).addShipmentReceived(size);
		else
			Logger.logError("BaseAgent.receivePackage():supplier " + supplier.getId() + " not found in key");
		// Make payment
		money -= price;
		supplier.receivePayment(price);
		// Change stock
		stock += size;
		out_totalImport += size;
		out_currentImport += size;
		ticksWithoutSatisfaction = 0;
	}*/

	/*================================
	 * Getters and setters
	 *===============================*/	
	
	public String getLabel() {
		return id + String.format("  $:%.0f  *:%.1f  #:%.1f", money, stock, baseConsumption);
	}
	
	public Color getColor() {
		if (satisfied)
			return Color.GREEN;
		else
			return Color.RED;
	}
	
	public boolean getSatisfied() {
		return satisfied;
	}

}