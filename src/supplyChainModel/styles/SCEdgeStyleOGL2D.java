package supplyChainModel.styles;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class SCEdgeStyleOGL2D extends DefaultEdgeStyleOGL2D {

	@Override
	public Color getColor(RepastEdge<?> edge){
		
		/* This is commented because there is not yet a notion of trust from supplier to buyer.
		Object source = edge.getSource();
		Object target = edge.getTarget();
		
		if (source instanceof BaseAgent && target instanceof BaseAgent) {
			double trustLevel = ((BaseAgent) source).getTrustLevel( ((BaseAgent) target).getId() );
			return new Color((float) (1 - trustLevel), (float) trustLevel, 0.0f);
		}*/
		return Color.BLUE;
	}
	
	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		
		return 3;
	}
}