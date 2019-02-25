package supplyChainModel.agents;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.enums.SCType;

public class Agent3Wholesaler extends BaseAgent {
	
	public Agent3Wholesaler(final Context<Object> context, CountryAgent country) {
		super(context, country, SCType.WHOLESALER, Constants.PRICE_BUY_FROM_WHOLESALER, Constants.SHIPMENT_MAX_3TO4);
	}

	@Override
	public void step_2_receive_shipment() {
		//This is done by the shipments
	}
	
	@Override
	public void step_3_choose_suppliers_and_buyers() {
		searchBuyers();
		searchSuppliers();
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
		sendOrders();
	}
}