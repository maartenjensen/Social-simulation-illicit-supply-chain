package supplyChainModel.agents;

import java.util.ArrayList;
import java.util.HashMap;

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
		
		updateArrivedShipments();
		
		for (Shipment shipment : getArrivedShipments()) {
			money -= shipment.getPrice();
			shipment.getSupplier().receivePayment(shipment.getPrice());
			addToStock(shipment.getGoods());
			shipment.remove();
			// Add import etc for DataCollector.
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
		
		//TODO order the orders based on most important clients
		for (Order order : getArrivedOrders()) {
			HashMap<Byte, Double> goodsToSend = findGoodsInStock(order.getGoods());
			if (!goodsToSend.isEmpty()) {
				
				double cost = 0;
				for (Byte goodsQuality : goodsToSend.keySet()) {
					cost += goodsToSend.get(goodsQuality) * Constants.PRICE_BUY_FROM_PRODUCER;
				}
				new Shipment(order.getClient(), this, goodsToSend, cost, RepastParam.getShipmentStep()); //TODO calculate price
				relationsC.get(order.getClient().getId()).addMyShipment(goodsToSend);
			}
			order.remove();
		}
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
		HashMap<Byte, Double> requiredGoods = getRequiredGoods();
		
		for (Byte quality : requiredGoods.keySet()) {
				
			double requiredQuantity = requiredGoods.get(quality);
			ArrayList<TrustCompare> sortedSuppliers = retrieveSortedSuppliers(quality);
			for (TrustCompare sortedSupplier : sortedSuppliers) {
				
				BaseAgent supplier = sortedSupplier.getAgent();
				Logger.logInfo("Required:" + requiredQuantity + ", min package size:" + supplier.getMinPackageSize());
				if (requiredQuantity >= supplier.getMinPackageSize()) {
					
					double oldQuantity = relationsS.get(supplier.getId()).getPreviousMyOrder(quality);
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
	
	public HashMap<Byte, Double> getRequiredGoods() {
		
		HashMap<Byte, Double> requiredGoods = new HashMap<Byte, Double>();
		
		for (Byte quality : stock.keySet()) {
			
			double requiredQuantity = securityStockMultiplier * getMinPackageSizeBoth();
			for (Integer id : relationsC.keySet()) {
				requiredQuantity += relationsC.get(id).getPreviousOtherOrder(quality);
			}
			
			requiredQuantity -= stock.get(quality);
			
			requiredGoods.put(quality, requiredQuantity);
		}
		
		return requiredGoods;
	}
}