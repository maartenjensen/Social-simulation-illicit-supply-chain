package supplyChainModel.agents;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;

public class Agent5Consumer extends BaseAgent {

	public double baseConsumption;
	public boolean satisfied;
	public int ticksUntilRemoved;
	public int ticksWithoutSatisfaction;
	
	public Agent5Consumer(final Context<Object> context, CountryAgent country) {
		super(context, country, SCType.CONSUMER, 0, 0);
		
		baseConsumption = RandomHelper.nextDoubleFromTo(RepastParam.getConsumptionMin(), RepastParam.getConsumptionMax());
		satisfied = false;
		ticksUntilRemoved = Constants.CONSUMER_REMOVE_TICKS;
		ticksWithoutSatisfaction = 0;
	}
	
	/**
	 * Adjusted to change removal based on life and rehab instead of from bankruptcy
	 * Can't die when initializing
	 */
	@Override
	public void stepRemoval() {
		
		ticksUntilRemoved -= 1;
		
		if (!SU.getIsInitializing() && (ticksUntilRemoved == 0 || ticksWithoutSatisfaction >= Constants.CONSUMER_LIMIT_WITHOUT_SATISFACTION)) {
			removeNode();
		}
	}
	
	/**
	 * Consume drugs
	 */
	@Override
	public void step_2_receive_shipment() {
		
		money += Constants.PRICE_CONSUMER_INCOME;
	}

	@Override
	public void step_3_choose_suppliers_and_buyers() {
		searchSuppliers();
	}
	
	/**
	 * Consume instead of sending shipment
	 */
	@Override
	public void step_4_send_shipment() {
		
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
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = -5, shuffle=true)
	public void step_5_receive_order() {
		supplyNeeded = Math.max((securityStock - stock) + baseConsumption, 0);
	}
	
	@Override
	public void step_6_send_order() {
		sendOrders();
	}

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