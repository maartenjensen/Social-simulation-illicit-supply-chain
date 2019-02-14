package supplyChainModel.agents;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Logger;
import supplyChainModel.enums.SCType;

public class AgentProducer extends BaseAgent {

	public AgentProducer(final Context<Object> context, SCType scType, CountryAgent country) {
		super(context, country, scType);
		
		name = "P";
	}
	
	@Override
	public void step_2_receive_shipment() { //Produced by itself
		
		if (stock >= securityStock) {
			return ;
		}
		int stockIncrease = RandomHelper.nextIntFromTo(2, 6);
		stock += stockIncrease;
		totalImport += stockIncrease;
		Logger.logInfoId(id, getNameId() + (stock + stockIncrease) + " - " + stockIncrease + " = " + stock);
	}
	
	@Override
	public void step_3_choose_suppliers_and_buyers() {
		searchBuyers();
	}
	
	@Override
	public void step_4_send_shipment() {
		sendShipment();
	}
	
	@Override
	public void step_5_receive_order() {
		updateOrders();
	}
	
	@Override
	public void step_6_send_order() {
		// Override by subclasses
	}
}