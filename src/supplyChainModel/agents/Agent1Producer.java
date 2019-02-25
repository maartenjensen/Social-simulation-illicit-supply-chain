package supplyChainModel.agents;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.enums.SCType;

public class Agent1Producer extends BaseAgent {

	public Agent1Producer(final Context<Object> context, CountryAgent country) {
		super(context, country, SCType.PRODUCER, Constants.PRICE_BUY_FROM_PRODUCER, Constants.SHIPMENT_MAX_1TO2);
	}
	
	@Override
	public void step_2_receive_shipment() { //Produced by itself
		
		if (stock >= securityStock) {
			return;
		}
		int stockIncrease = RandomHelper.nextIntFromTo(2, 6);
		money -= stockIncrease * Constants.PRICE_PRODUCTION;
		stock += stockIncrease;
		outputTotalImport += stockIncrease;
		outputCurrentImport += stockIncrease;
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