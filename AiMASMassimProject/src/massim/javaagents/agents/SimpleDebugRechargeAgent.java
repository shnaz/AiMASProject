package massim.javaagents.agents;

import java.util.Collection;

import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;

public class SimpleDebugRechargeAgent extends Agent{

	public SimpleDebugRechargeAgent(String name, String team) {
		super(name, team);
	}

	@Override
	public Action step() {
		
		Collection<Percept> percepts = getAllPercepts();
		for(Percept p : percepts){
			
			println("Percept received");
			String type = p.getName();
			
			if (type.equalsIgnoreCase("simStart")){
				println("simStart");
			}
			else if(type.equalsIgnoreCase("simEnd")){
				println("simEnd");
				clearBeliefs();
			}
			
			
		}
		
		return MarsUtil.rechargeAction();
	}

	@Override
	public void handlePercept(Percept p) {}

}
