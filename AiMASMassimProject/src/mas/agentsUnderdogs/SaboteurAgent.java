package mas.agentsUnderdogs;

import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public class SaboteurAgent extends Agent {

	public SaboteurAgent(String name, String team) {
		super(name, team);
		// TODO do something if necessary
	}

	@Override
	public void handlePercept(Percept p) {
		
		// TODO handle percepts if necessary
	}

	@Override
	public Action step() {
		return MarsUtil.skipAction();
	}

}
