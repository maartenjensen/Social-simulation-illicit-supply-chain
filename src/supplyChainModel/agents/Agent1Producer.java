package supplyChainModel.agents;

import java.util.HashMap;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.common.RepastParam;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

public class Agent1Producer extends BaseAgent {

	// State variables
	private byte quality;
	
	public Agent1Producer(final Context<Object> context, CountryAgent country, byte quality) {
		super(country, SCType.PRODUCER, Constants.PRICE_BUY_FROM_PRODUCER, Constants.SHIPMENT_MAX_1TO2);
		
		this.quality = quality;
	}
	
	/**
	 * The producer receives shipments it has made itself,
	 * the money is the trade in for raw materials and production costs
	 */
	@Override
	public void stepProcessArrivedShipments() {
		
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
		
		updateArrivedOrders();
		
		//TODO order the orders based on most important clients
		for (Order order : getArrivedOrders()) {
			HashMap<Byte, Double> goodsToSend = findGoodsInStock(order.getGoods());
			if (!goodsToSend.isEmpty()) {
				new Shipment(order.getClient(), this, goodsToSend, 2500, RepastParam.getShipmentStep()); //TODO calculate price
				relationsC.get(order.getClient().getId()).addMyShipment(goodsToSend);
			}
			order.remove();
		}
	}
	
	/**
	 * The producer directly creates shipments
	 * and can freely decide the quantity
	 */
	@Override
	public void stepSendOrder() {
		
		//TODO make this part of decision with correct values
		HashMap<Byte, Double> producedGoods = new HashMap<Byte, Double>();
		producedGoods.put(quality, 20.0);
		double productionCost = quality * 20.0;
		new Shipment(this, null, producedGoods, productionCost, RepastParam.getShipmentStep());
	}
}