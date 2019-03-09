package supplyChainModel.agents;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.enums.SCType;

public class Agent3Wholesaler extends BaseAgent {
	
	public Agent3Wholesaler(final Context<Object> context, CountryAgent country) {
		super(country, SCType.WHOLESALER, Constants.PRICE_BUY_FROM_WHOLESALER, Constants.SHIPMENT_MAX_3TO4);
	}

	@Override
	public void stepReceiveShipments() {
		
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchClients();
		searchSuppliers();
	}
	
	@Override
	public void stepSendShipment() {
		//sendShipment();
	}
	
	@Override
	public void stepReceiveOrder() {
		//updateOrders();
	}
	
	@Override
	public void stepSendOrder() {
		//sendOrders();
	}
}