package supplyChainModel.agents;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.enums.SCType;

public class Agent1Producer extends BaseAgent {

	public Agent1Producer(final Context<Object> context, CountryAgent country) {
		super(context, country, SCType.PRODUCER, Constants.PRICE_BUY_FROM_PRODUCER, Constants.SHIPMENT_MAX_1TO2);
	}
	
	public void stepProduce() { //Produced by itself
		
		if (stock >= securityStock) {
			return;
		}
		int stockIncrease = Constants.PRODUCER_PRODUCE_AMOUNT;
		money -= stockIncrease * Constants.PRICE_PRODUCTION;
		stock += stockIncrease;
		out_totalImport += stockIncrease;
		out_currentImport += stockIncrease;
		Logger.logInfoId(id, getNameId() + " " + (stock + stockIncrease) + " - " + stockIncrease + " = " + stock);
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchClients();
	}
	
	@Override
	public void stepSendShipment() {
		sendShipment();
	}
	
	@Override
	public void stepReceiveOrder() {
		updateOrders();
	}
	
	@Override
	public void stepSendOrder() {
		// Override by subclasses
	}
}