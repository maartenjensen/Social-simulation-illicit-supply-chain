package supplyChainModel.styles;

import java.awt.Color;
import java.awt.Font;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;
import supplyChainModel.agents.CountryAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.enums.SCType;

public class CountryAgentStyle2D extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object o){
		
		if (o instanceof CountryAgent) {
			return ((CountryAgent) o).getColor();
		}
		
		return null;
	}
	
	/**
	   * @return a circle of radius 25.
	   */
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if (spatial == null) {
			if (agent instanceof CountryAgent) {
				if (((CountryAgent) agent).containsSCType(SCType.WHOLESALER))
					spatial = shapeFactory.createRectangle(50, 50);	
				else if (((CountryAgent) agent).containsSCType(SCType.CONSUMER))
					spatial = shapeFactory.createCircle(25, 16);
				else
					spatial = shapeFactory.createRectangle(4, 50);
			}
		}
		return spatial;
	}

	@Override
	public float getScale(Object o) {
		if (o instanceof CountryAgent)
			return (float) ((CountryAgent) o).getRadius();
		
		return 2f;
	}

	@Override
	public String getLabel(Object object) {

		if (object instanceof CountryAgent) {
			final CountryAgent agent = (CountryAgent) object;
			return agent.getLabel();
		}
		
		return "Warning label not found for object";
	}

	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_BIG;
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.NORTH;
	}
	
	public Color getLabelColor(Object object) {
	    return Color.RED;
	}
}