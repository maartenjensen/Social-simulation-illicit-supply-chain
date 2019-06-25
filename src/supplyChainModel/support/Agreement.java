package supplyChainModel.support;

import java.util.HashMap;

import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.SU;

public class Agreement {
	
	// State variables
	private BaseAgent client;
	private BaseAgent supplier;
	private HashMap<Byte, Double> goods;
	private double price;
	private int stepsDue;
	
	public Agreement(BaseAgent client, BaseAgent supplier, HashMap<Byte, Double> goods) {

		SU.getContext().add(this);
		
		this.client = client;
		this.supplier = supplier;
		this.goods = goods;	
	}
	
	public int getIdClient() {
		return client.getId();
	}
	
	public int getIdSupplier() {
		return supplier.getId();
	}
	
	public BaseAgent getClient() {
		return client;
	}
	
	public BaseAgent getSupplier() {
		return supplier;
	}
	
	public double getPrice() {
		return price;
	}
	
	public int getStepsDue() {
		return stepsDue;
	}
}
