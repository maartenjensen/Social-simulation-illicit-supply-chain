package supplyChainModel.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.context.Context;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

public class Agent2International extends BaseAgent {
	
	public Agent2International(final Context<Object> context, CountryAgent country) {
		super(country, SCType.INTERNATIONAL, Constants.PRICE_BUY_FROM_INTERNATIONAL, Constants.SHIPMENT_MAX_2TO3);
	}
		
	@Override
	public void stepProcessArrivedShipments() {
		for (Shipment shipment : getArrivedShipments()) { //TODO add payment to supplier
			money -= shipment.getPrice();
			shipment.getSupplier().receivePayment(shipment.getPrice());
			addToStock(shipment.getGoods());
			shipment.remove();
		}
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchClients();
		searchSuppliers();
	}
	
	@Override
	public void stepSendShipment() {
		
		updateArrivedOrders();
	}
	
	/**
	 * Send order based on what is required. For each quality the suppliers are checked.
	 * The suppliers with the highest trust for that quality will be asked first to provide
	 * the maximum amount. Orders are created but if an order already exists the required goods
	 * are added to the order
	 */
	@Override
	public void stepSendOrder() {
		
		HashMap<Integer, Order> placedOrders = new HashMap<Integer, Order>();
		Map<Byte, Double> requiredGoods = new HashMap<Byte, Double>();
		requiredGoods.put((byte) 90, 10.0);
		
		for (Byte quality : requiredGoods.keySet()) {
				
			double requiredQuantity = requiredGoods.get(quality);
			ArrayList<TrustCompare> sortedSuppliers = retrieveSortedSuppliers(90);
			for (TrustCompare sortedSupplier : sortedSuppliers) {
				
				BaseAgent supplier = sortedSupplier.getAgent();
				Logger.logInfo("Required:" + requiredQuantity + ", min package size:" + supplier.getMinPackageSize());
				if (requiredQuantity >= supplier.getMinPackageSize()) {
					
					double oldQuantity = relationOther.get(supplier.getId()).getPreviousOrder(quality);
					double chosenQuantity = Constants.SEND_ORDER_LEARN_RATE * requiredQuantity +
											(1 - Constants.SEND_ORDER_LEARN_RATE) * oldQuantity;

					chosenQuantity = Math.min(Math.max(chosenQuantity, supplier.getMinPackageSize()), supplier.getMaxPackageSize());
					requiredQuantity -= chosenQuantity;
					
					//Decide whether the order should be added or create anew
					if (placedOrders.containsKey(supplier.getId())) {
						placedOrders.get(supplier.getId()).addToGoods(quality, chosenQuantity);
					}
					else {
						HashMap<Byte, Double> chosenGoods = new HashMap<Byte, Double>();
						chosenGoods.put(quality, chosenQuantity);
						placedOrders.put(supplier.getId(), new Order(this, supplier, chosenGoods, RepastParam.getShipmentStep()));
					}
				}
			}
		}
		
		addOrdersToRelation(placedOrders);
	}
}