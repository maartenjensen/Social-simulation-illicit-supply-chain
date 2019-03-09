package supplyChainModel.agents;

import java.util.HashMap;
import java.util.Map;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.common.RepastParam;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

public class Agent1Producer extends BaseAgent {

	public Agent1Producer(final Context<Object> context, CountryAgent country) {
		super(country, SCType.PRODUCER, Constants.PRICE_BUY_FROM_PRODUCER, Constants.SHIPMENT_MAX_1TO2);
	}
	/*
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
	}*/
	
	/**
	 * The producer receives shipments it has made itself,
	 * the money is the trade in for raw materials and production costs
	 */
	@Override
	public void stepReceiveShipments() {
		for (Shipment shipment : getArrivedShipments()) {
			money -= shipment.getPrice();
			addToStock(shipment.getGoods());
			shipment.remove();
		}
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchClients();
	}
	
	@Override
	public void stepSendShipment() {
		
		//TODO order the orders based on most important clients
		//sendShipment();
		for (Order order : getArrivedOrders()) {
			Map<Byte, Double> goodsToSend = findGoodsInStock(order.getGoods());
			if (!goodsToSend.isEmpty()) {
				new Shipment(order.getClient(), this, goodsToSend, 10, RepastParam.getShipmentStep()); //TODO calculate price
			}
			order.remove();
		}
	}
	
	@Override
	public void stepReceiveOrder() {
		//updateOrders();
	}
	
	/**
	 * The producer directly creates shipments
	 * and can freely decide the quantity
	 */
	@Override
	public void stepSendOrder() {
		
		//TODO make this part of decision with correct values
		Map<Byte, Double> producedGoods = new HashMap<Byte, Double>();
		producedGoods.put((byte) 90, 20.0);
		double productionCost = 90 * 20.0;
		new Shipment(this, null, producedGoods, productionCost, RepastParam.getShipmentStep());
	}
}