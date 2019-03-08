package supplyChainModel.support;

/**
 * The goods that are produced by producers, they pass through the
 * supply chain to be finally consumed by the consumers.
 * @author Maarten
 */
public class Goods {
	
	private boolean active = false;
	private double size = 0;
	private int quality = 0;
	
	public Goods(boolean active, double size, int quality) {
		
		this.active = active;
		this.size = size;
		this.quality = quality;
	}

	public boolean getActive() {
		return active;
	}

	public double getSize() {
		return size;
	}
	
	public int getQuality() {
		return quality;
	}
}