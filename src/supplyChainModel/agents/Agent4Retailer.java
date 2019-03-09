package supplyChainModel.agents;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.enums.SCType;

public class Agent4Retailer extends BaseAgent {
	
	public Agent4Retailer(final Context<Object> context, CountryAgent country) {
		super(country, SCType.RETAIL, Constants.PRICE_BUY_FROM_RETAIL, Constants.SHIPMENT_MAX_4TO5);
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