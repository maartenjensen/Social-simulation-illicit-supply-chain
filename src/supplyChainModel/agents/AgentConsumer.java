package supplyChainModel.agents;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.enums.SCType;

public class AgentConsumer extends BaseAgent {

	public double baseConsumption = RandomHelper.nextDoubleFromTo(RepastParam.getConsumptionMin(), RepastParam.getConsumptionMax());
	public boolean satisfied = false;
	
	public AgentConsumer(final Context<Object> context, SCType scType, CountryAgent country) {
		super(context, country, scType);
		
		name = "C";
	}
	
	@Override
	public void step_2_receive_shipment() {
		
		if (stock >= baseConsumption) {
			Logger.logInfoId(id, getNameId() + ":" + stock + " - " + baseConsumption + " = " + (stock - baseConsumption));
			stock -= baseConsumption;
			satisfied = true;
			
		}
		else {
			stock = 0;
			satisfied = false;
		}
	}

	@Override
	public void step_3_choose_suppliers_and_buyers() {
		searchSuppliers();
	}
	
	@Override
	public void step_4_send_shipment() {
		
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
		return id + String.format(", $:%.1f, *:%.1f, #:%.1f", money, stock, baseConsumption);
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