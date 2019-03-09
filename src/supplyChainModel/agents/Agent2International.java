package supplyChainModel.agents;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.enums.SCType;

public class Agent2International extends BaseAgent {
	
	public Agent2International(final Context<Object> context, CountryAgent country) {
		super(country, SCType.INTERNATIONAL, Constants.PRICE_BUY_FROM_INTERNATIONAL, Constants.SHIPMENT_MAX_2TO3);
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