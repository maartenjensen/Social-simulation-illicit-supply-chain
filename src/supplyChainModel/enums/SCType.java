package supplyChainModel.enums;

import java.awt.Color;

import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;

/**
 * Enum for the supply chain type.
 * @author Maarten
 *
 */
public enum SCType {

	PRODUCER(0, Color.BLACK), INTERNATIONAL(1, Color.BLACK), WHOLESALER(2, Color.MAGENTA), RETAIL(3, Color.BLACK), CONSUMER(4, Color.RED);
	
	static int scLayers = 5;
	
	private int scLayer;
	private Color color;
	
	private SCType(int scLayer, Color color) {
		
		this.scLayer = scLayer;
		this.color = color;
	}

	public int getX() {

		int x = Constants.VSL_COUNTRY_X + scLayer * Constants.VSL_COUNTRY_WIDTH + 1;
		if (x > Constants.GRID_WIDTH)
			Logger.logError("SCType.getScLayerX(): x > Constants.GRID_WIDTH . " + x + " > " + Constants.GRID_WIDTH);
		return x;
	}
	
	public int getScLayer() {
		return scLayer;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getScCharacter() {
		if (name().length() > 0) {
			return name().substring(0, 1);
		}
		else {
			Logger.logError("SCType.getScCharacter() ");
			return "X";
		}
	}
	
	// Static functions
	public static int getScLayers() {
		return scLayers;
	}
}