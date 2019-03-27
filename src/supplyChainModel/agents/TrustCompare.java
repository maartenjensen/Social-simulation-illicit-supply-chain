package supplyChainModel.agents;

public class TrustCompare implements Comparable<TrustCompare> {

	BaseAgent agent;
	double trust;
	
	/**
	 * Compare class to easily compare agents based on trust
	 */
	public TrustCompare(BaseAgent agent, double trust) {
		
		this.agent = agent;
		this.trust = trust;
	}

	@Override
	public int compareTo(TrustCompare o) {
		
		if (this.trust < o.trust) return -1;
		if (this.trust > o.trust) return 1;
		else return 0;
	}
	
	public BaseAgent getAgent() {
		return agent;
	}
	
	public double getTrust() {
		return trust;
	}
	
	public String toString() {
		return agent.getId() + ":" + trust;
	}
}
