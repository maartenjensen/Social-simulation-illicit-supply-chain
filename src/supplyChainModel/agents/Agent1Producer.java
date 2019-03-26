package supplyChainModel.agents;

import java.util.ArrayList;
import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

public class Agent1Producer extends BaseAgent {

	// State variables
	private byte quality;
	private Double previousOrder;
	
	public Agent1Producer(final Context<Object> context, CountryAgent country, byte quality) {
		super(country, SCType.PRODUCER, Constants.PRICE_BUY_FROM_PRODUCER, Constants.SHIPMENT_MAX_1TO2);
		
		this.quality = quality;
		this.previousOrder = 0.0;
		
		setStartingStock();
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
		
		ArrayList<TrustCompare> sortedClients = retrieveSortedClients();
		for (TrustCompare client : sortedClients) {
			//Logger.logSCAgent(scType, "stepSendShipment(): " + id + " other id: " + client.getAgent().getId() + ", " + client.getTrust());
			Order clientOrder = null;
			for (Order order : getArrivedOrders()) {
				if (order.getClient().getId() == client.getAgent().getId()) {
					clientOrder = order;
				}
			}
			
			if (clientOrder != null) {
				
				if (RandomHelper.nextDouble() <= RepastParam.getSendShipmentProbability()) {
				
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
				else {
					
					clientOrder.setSavedOrder();
				}
			}
		}
	}

	/**
	 * The producer directly creates shipments
	 * and can freely decide the quantity,
	 * the max creation size is defined by the RepastParam
	 */
	@Override
	public void stepSendOrder() {
		
		double requiredQuantity = securityStockMultiplier * minPackageSize;
		for (Integer id : relationsC.keySet()) {
			requiredQuantity += relationsC.get(id).getPreviousOtherOrder(quality);
		}
		
		if (stock.containsKey(quality))
			requiredQuantity -= stock.get(quality);
		
		double chosenQuantity = Constants.SEND_ORDER_LEARN_RATE * requiredQuantity +
								(1 - Constants.SEND_ORDER_LEARN_RATE) * previousOrder;
		chosenQuantity = Math.max(0, Math.min(RepastParam.getProductionMax(), chosenQuantity));
		
		if (chosenQuantity == 0.0) {
			previousOrder = 0.0;
			return ;
		}
		
		HashMap<Byte, Double> producedGoods = new HashMap<Byte, Double>();
		producedGoods.put(quality, chosenQuantity);
		double productionCost = chosenQuantity * Constants.PRICE_PRODUCTION;
		new Shipment(this, null, producedGoods, productionCost, RepastParam.getShipmentStep());
		
		SU.getDataCollector().addProducedStock(producedGoods);
		
		previousOrder = chosenQuantity;
	}
	
	/*================================
	 * Getters and setters
	 *===============================*/	
	
	@Override
	protected void setStartingStock() {
		stock.put(quality, securityStockMultiplier * minPackageSize);
	}
	
	public byte getQuality() {
		return quality;
	}
}