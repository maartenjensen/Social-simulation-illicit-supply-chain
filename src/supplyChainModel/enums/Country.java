package supplyChainModel.enums;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;

public enum Country {

	NONE(0,0,0,0),
	
	SOUTH_AMERICA(1,0,3,Constants.GRID_HEIGHT, SCType.PRODUCER),
	INTERNATIONAL(5,0,3,Constants.GRID_HEIGHT, SCType.INTERNATIONAL),
	
	SPAIN(9,Constants.GRID_HEIGHT/2 ,4,Constants.GRID_HEIGHT/2, SCType.WHOLESALER, SCType.RETAIL, SCType.CONSUMER),
	THE_NETHERLANDS(9,0, 4,Constants.GRID_HEIGHT/2, SCType.WHOLESALER, SCType.RETAIL, SCType.CONSUMER),
	
	UNITED_KINGDOM(14,0,5,5, SCType.RETAIL, SCType.CONSUMER),
	//GERMANY(14,5,5,5, SCType.RETAIL, SCType.CONSUMER),
	GERMANY(14,2,6,16, SCType.RETAIL, SCType.CONSUMER),
	FRANCE(14,10,5,5, SCType.RETAIL, SCType.CONSUMER),
	ITALY(14,15,5,5, SCType.RETAIL, SCType.CONSUMER);
	
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	
	private ArrayList<Point> countryPoints;
	
	private Country(int x, int y, int width, int height, SCType... scType) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		countryPoints = createCountryPoints();
		
	}
	
	public Point getFreePosition(Object toExclude) {
		
		boolean free = false;
		Collections.shuffle(countryPoints);
		for (Point p : countryPoints) {
		
				free = true;
				for (Object o: SU.getGrid().getObjectsAt(p.x, p.y)) {
					if (o != toExclude)
						free = false;
				}
				
				if (free) {
					return new Point(p.x,p.y);
				}
		}
		Logger.logInfo("No free point in " + name());
		return new Point(x + width/2, y + height/2);
	}
	
	private ArrayList<Point> createCountryPoints() {
		
		ArrayList<Point> emptyCountryPoints = new ArrayList<Point>();
		for (int x = this.x; x < this.x + width; x ++) {
			for (int y = this.y; y < this.y + height; y ++) {
				emptyCountryPoints.add(new Point(x,y));
			}
		}
		return emptyCountryPoints;
	}
}
