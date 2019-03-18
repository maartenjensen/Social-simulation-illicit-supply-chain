package supplyChainModel.agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

public class Agent5Consumer extends BaseAgent {

	// State variables
	private byte quality;
	public double baseConsumption;
	public boolean satisfied;
	public int ticksUntilRemoved;
	public int ticksWithoutSatisfaction;
	
	public Agent5Consumer(final Context<Object> context, CountryAgent country, byte quality) {
		super(country, SCType.CONSUMER, 0, 0);
		
		this.quality = quality;
		baseConsumption = RandomHelper.nextDoubleFromTo(RepastParam.getConsumptionMin(), RepastParam.getConsumptionMax());
		satisfied = false;
		ticksUntilRemoved = Constants.CONSUMER_REMOVE_TICKS;
		ticksWithoutSatisfaction = 0;
		
		stock.put(quality, 0.0);
	}
	
	/*====================================
	 * The main steps of the agents
	 *====================================*/
	
	/**
	 * Adjusted to change removal based on life and rehab instead of from bankruptcy
	 * Can't die when initializing
	 */
	@Override
	public void stepCheckRemoval() {
		
		ticksUntilRemoved -= 1;
		
		if (!SU.getIsInitializing() && (ticksUntilRemoved == 0 || ticksWithoutSatisfaction >= Constants.CONSUMER_LIMIT_WITHOUT_SATISFACTION)) {
			remove();
		}
	}
	
	/**
	 * Gain money
	 */
	public void stepReceiveIncome() {
		
		money += Constants.PRICE_CONSUMER_INCOME;
	}
	
	@Override
	public void stepProcessArrivedShipments() {
		
		updateArrivedShipments();
		
		for (Shipment shipment : getArrivedShipments()) { //TODO add payment to supplier
			money -= shipment.getPrice();
			shipment.getSupplier().receivePayment(shipment.getPrice());
			addToStock(shipment.getGoods());
			shipment.remove();
			ticksWithoutSatisfaction = 0;
			// Add import etc for DataCollector.
		}
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchSuppliers();
	}
	
	@Override
	public void stepSendShipment() {
		
		
		if (stock.get(quality) >= baseConsumption) {
			Logger.logInfoId(id, getNameId() + ":" + stock.get(quality) + " - " + baseConsumption + " = " + (stock.get(quality) - baseConsumption));
			stock.put(quality, stock.get(quality) - baseConsumption);
			ticksWithoutSatisfaction = 0;
			satisfied = true;
		}
		else {
			stock.put(quality, 0.0);
			ticksWithoutSatisfaction ++;
			satisfied = false;
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

		double requiredQuantity = securityStockMultiplier * getMinPackageSizeBoth() + baseConsumption;
		requiredQuantity -= stock.get(quality);
		requiredGoods.put(quality, requiredQuantity);
		
		return requiredGoods;
	}

	/*================================
	 * Getters and setters
	 *===============================*/	
	
	/*public String getLabel() {
		return id + String.format("  $:%.0f", money);
	}*/
	
	public Color getColor() {
		if (satisfied)
			return Color.GREEN;
		else
			return Color.RED;
	}
	
	public boolean getSatisfied() {
		return satisfied;
	}
	
	public byte getQuality() {
		return quality;
	}
}