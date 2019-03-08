package supplyChainModel.styles;

import java.awt.Color;
import java.awt.Font;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.Constants;

public class AgentStyle2D extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object o){
		
		if (o instanceof BaseAgent) {
			BaseAgent scNode = (BaseAgent)o;
			return scNode.getColor();	
		}

		return null;
	}
	
	@Override
	public float getScale(Object o) {
		if (o instanceof BaseAgent)
			return 1f;

		return 2f;
	}
	
	@Override
	public String getLabel(Object object) {

		if (object instanceof BaseAgent) {
			final BaseAgent agent = (BaseAgent) object;
			return agent.getLabel();
		}
		
		return "Warning label not found for object";
	}

	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_LABEL;
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.SOUTH_EAST;
	}
}