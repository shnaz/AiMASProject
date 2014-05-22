package mas.agentsUnderdogs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import eis.iilang.Action;
import eis.iilang.Percept;
import mas.agentsUnderdogs.util.PathFinder;
import mas.agentsUnderdogs.util.Vertex;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public class SaboteurAgent extends Agent {

	// Not-shared variables
	private String step;
	private ArrayList<Vertex> myPathArray = new ArrayList<Vertex>();
	private int myAgentID;
	private int myEnergy;
	private String myPosition;
	private String myPost = null;

	private String lastAction;
	private String lastActionResult;
	private String lastActionParam;
	private Action lastActionObj;
	private boolean lastActionFailed = false;

	public SaboteurAgent(String name, String team) {
		super(name, team);
		myAgentID = agentCount++;
	}

	@Override
	public void handlePercept(Percept p) {
	}

	@Override
	public Action step() {

		// Get all precept and process
		handleAllPercept();

		// Repeat last action if random failed
		if (lastActionFailed) {
			lastActionFailed = false;
			return lastActionObj;
		}

		if (!graph.zoneBorderVertices.isEmpty() && myPost == null ) {
			myPost = graph.popFromZoneBorderVertices();
			if(myPost != null){
				findPathToVertex(myPost);
				occupiedZoneVertices++;
			}
		}

		// Set this vertex as explored
		graph.unExplored.remove(myPosition);

		// Recharge when flat
		if (myEnergy < 10) {
			return MarsUtil.rechargeAction();
		}

		// Move from one vertex to another
		if (!myPathArray.isEmpty()) {
			String nextVertex = myPathArray.remove(0).name;
			return MarsUtil.gotoAction(nextVertex);
		}

		// Find path to nearest unexplored vertex
		if (!graph.unExplored.isEmpty()) {
			findPathToTheNearestVertex();
			String nextVertex = myPathArray.remove(0).name;
			return MarsUtil.gotoAction(nextVertex);
		}

		return MarsUtil.rechargeAction();
	}

	private void findPathToTheNearestVertex() {
		Vertex start = graph.getVertex(myPosition);
		myPathArray = PathFinder.FindPath(start, graph.unExplored, graph);
		String target = myPathArray.get(myPathArray.size() - 1).name;
		graph.unExplored.remove(target);
	}

	private void findPathToVertex(String goalVertex) {
		Vertex start = graph.getVertex(myPosition);
		HashSet<String> goal = new HashSet<String>();
		goal.add(goalVertex);

		myPathArray = PathFinder.FindPath(start, goal, graph);
		String target = myPathArray.get(myPathArray.size() - 1).name;
		graph.unExplored.remove(target);

	}

	@SuppressWarnings({ "deprecation" })
	private void handleAllPercept() {

		Collection<Percept> percepts = getAllPercepts();
		for (Percept p1 : percepts) {

			if (p1.getName().equals("position")) {
				myPosition = p1.getParameters().get(0).toString();
				graph.addVertex(myPosition);
				
			} else if (p1.getName().equals("energy")) {
				myEnergy = Integer.parseInt(p1.getParameters().get(0).toString());
				
			} else if (p1.getName().equals("visibleEdge")) {
				String vertexA = p1.getParameters().get(0).toString();
				String vertexB = p1.getParameters().get(1).toString();
				int edgeCost = 1;

				graph.addEdge(vertexA, edgeCost, vertexB);

				printStringToFile("Step: " + step + "\tExplore edge1: "
						+ vertexA + "\t edge2: " + vertexB + "\t edgeCost: "
						+ edgeCost, "ExplorerAgent_NoSurveyAction.txt");

			} else if (p1.getName().equals("step")) {
				step = p1.getParameters().get(0).toString();

			} else if (p1.getName().equals("lastAction")) {
				lastAction = p1.getParameters().get(0).toString();

			} else if (p1.getName().equals("lastActionParam")) {
				lastActionParam = p1.getParameters().get(0).toString();

			} else if (p1.getName().equals("lastActionResult")) {
				lastActionResult = p1.getParameters().get(0).toString();
				if (lastActionResult.substring(0, 6).equals("failed")) {

					printStringToFile("Step:" + step + "\tAgentID:" + myAgentID
							+ " Position:" + myPosition + "\tLastAction: "
							+ lastAction + lastActionParam
							+ "\tlastActionResult:" + lastActionResult,
							"ExplorerAgent_FailedActions.txt");
				}
			}
		}

		// In case of random fail, repeat last action
		if (lastActionResult.equals("failed_random")) {
			lastActionFailed = true;
			if (lastAction.equals("goto")) {
				lastActionObj = MarsUtil.gotoAction(lastActionParam);
			} else if (lastAction.equals("survey")) {
				lastActionObj = MarsUtil.surveyAction();
			} else if (lastAction.equals("probe")) {
				lastActionObj = MarsUtil.probeAction();
			} else {
				lastActionObj = MarsUtil.skipAction();
			}
			printStringToFile("Step:" + step + "AgentID:" + myAgentID + " - "
					+ lastAction + "(" + lastActionParam + ")",
					"ExplorerAgent_RandomFail.txt");
		}
	}

	// For testing purposes
	private void printStringToFile(String text, String filename) {
		String path = "logfiles/" + filename;

		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(path, true)))) {
			out.println(text);
		} catch (IOException e) {
		}
	}
}