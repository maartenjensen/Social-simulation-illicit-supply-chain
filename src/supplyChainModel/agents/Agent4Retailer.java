package supplyChainModel.agents;

import java.util.ArrayList;
import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

public class Agent4Retailer extends BaseAgent {
	
	public Agent4Retailer(final Context<Object> context, CountryAgent country) {
		super(country, SCType.RETAIL, Constants.PRICE_BUY_FROM_RETAIL, Constants.SHIPMENT_MAX_4TO5);
		
		setStartingStock();
	}
	
	@Override
	public void stepProcessArrivedShipments() {
		
		updateArrivedShipments();
		
		for (Shipment shipment : getArrivedShipments()) { //TODO add payment to supplier
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
		
		ArrayList<TrustCompare> sortedClients = retrieveSortedClients();
		for (TrustCompare client : sortedClients) {
			Logger.logSCAgent(scType, "stepSendShipment(): " + id + " other id: " + client.getAgent().getId() + ", " + client.getTrust());
			Order clientOrder = null;
			for (Order order : getArrivedOrders()) {
				if (order.getClient().getId() == client.getAgent().getId()) {
					clientOrder = order;
				}
			}
			
			if (clientOrder != null && RandomHelper.nextDouble() <= RepastParam.getSendShipmentProbability()) {
				
				HashMap<Byte, Double> goodsToSend = findGoodsInStock(clientOrder.getGoods());
				if (!goodsToSend.isEmpty()) {
					
					double cost = 0;
					for (Byte goodsQuality : goodsToSend.keySet()) {
						cost += goodsToSend.get(goodsQuality) * sellPrice;
					}
					new Shipment(clientOrder.getClient(), this, goodsToSend, cost, RepastParam.getShipmentStep()); //TODO calculate price
					relationsC.get(clientOrder.getClient().getId()).addMyShipment(goodsToSend);
				}
				clientOrder.remove();
			}
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
				//Logger.logInfo("Required:" + requiredQuantity + ", min package size:" + supplier.getMinPackageSize());
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
	
	@Override
	protected void setStartingStock() {
		
		if (RandomHelper.nextDouble() <= 0.5)
			stock.put(Constants.QUALITY_MINIMUM, securityStockMultiplier * minPackageSize);
		else
			stock.put(Constants.QUALITY_MAXIMUM, securityStockMultiplier * minPackageSize);
	}
}