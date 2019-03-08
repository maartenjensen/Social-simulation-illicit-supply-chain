package supplyChainModel.styles;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;
import supplyChainModel.agents.BaseAgent;

public class SCReversedEdgeStyleOGL2D2 extends DefaultEdgeStyleOGL2D {
	
	@Override
	public Color getColor(RepastEdge<?> edge){
		
		Object source = edge.getSource();
		Object target = edge.getTarget();
		
		if (source instanceof BaseAgent && target instanceof BaseAgent) {
			double trustLevel = ((BaseAgent) source).retrieveTrustLevel( ((BaseAgent) target).getId() );
			return new Color((float) (1 - trustLevel), (float) trustLevel, 0.0f);
		}
		return Color.BLUE;
	}
	
	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		
		return 1;
	}
}
