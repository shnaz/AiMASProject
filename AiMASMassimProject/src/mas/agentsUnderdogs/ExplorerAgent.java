package mas.agentsUnderdogs;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import mas.agentsUnderdogs.util.*;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;
import eis.iilang.Action;
import eis.iilang.Percept;

public class ExplorerAgent extends Agent {

	/*
	 * // Shared graph between all agents private static int agentCount = 1;
	 * private static Graph graph = new Graph(1200); private static
	 * HashSet<String> unExplored = new HashSet<String>(800); private static
	 * boolean done=true; //Debug private static int occupiedZoneVertices=0;
	 * //Debug
	 */
	// Not-shared variables
	private String step;
	private int numberOfEdges;

	private ArrayList<Vertex> myPathArray = new ArrayList<Vertex>();
	private String myPosition;
	private int myEnergy;
	private int myAgentID;
	private String myPost = null;
	private HashSet<String> importantVertices = new HashSet<String>(100);
	private static boolean PHASE2 = true;

	private String lastAction;
	private String lastActionResult;
	private String lastActionParam;
	private Action lastActionObj;
	private boolean lastActionFailed = false;

	public ExplorerAgent(String name, String team) {
		super(name, team);
		myAgentID = agentCount++;
		// printStringToFile("#Initialized agent nr: " + myAgentID,
		// "ExplorerAgent.txt");
		// resetLogFiles();
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

		// Recharge when flat
		if (myEnergy < 10) {
			return MarsUtil.rechargeAction();
		}

		if (graph.edges.size() == numberOfEdges && PHASE == 1) {
			PHASE = 2;
			graph.chooseTheMostValuableZone();
			try {
				Helper.playTadaSound();
			} catch (IOException e) {
			}
		}

		if (!graph.zoneBorderVertices.isEmpty() && myPost == null) {
			myPost = graph.popFromZoneBorderVertices();
			if (myPost != null) {
				findPathToVertex(myPost);
			}
		}

		graph.unExplored.remove(myPosition);
		graph.unProbed.remove(myPosition);
		importantVertices.remove(myPosition);

		if (PHASE == 1) {

			// Probe vertex if not probed
			if (!graph.isVertexProbed(myPosition)) {
				graph.unProbed.remove(myPosition);
				graph.setVertexAsProbed(myPosition);
				return MarsUtil.probeAction();
			} else {
				// Vertex epiCenter = graph.getVertex(myPosition);
				int weight = graph.getVertexWeight(myPosition); // epiCenter.cost;
				if (weight == 10) {
					for (Vertex neigh : graph.getNeighbours(myPosition)) {
						if (!graph.isVertexProbed(neigh.name)) {
							importantVertices.add(neigh.name);
						}
						for (Vertex neighsNeigh : graph
								.getNeighbours(neigh.name)) {
							if (!graph.isVertexProbed(neighsNeigh.name)) {
								importantVertices.add(neighsNeigh.name);
							}

							for (Vertex neighsNeighsNeigh : graph
									.getNeighbours(neighsNeigh.name)) {
								if (!graph
										.isVertexProbed(neighsNeighsNeigh.name)) {
									importantVertices
											.add(neighsNeighsNeigh.name);
								}
							}
						}

					}
					graph.expandZoneWithCenter(myPosition);

				} else if (weight == 1) {
					for (Vertex neigh : graph.getNeighbours(myPosition)) {
						graph.unProbed.remove(neigh.name);
						importantVertices.remove(neigh.name);
						graph.setVertexAsProbed(neigh.name);
						graph.setVertexWeight(neigh.name, 0);
					}

				}
			}

		} else {
			
			if (!graph.isVertexProbed(myPosition)) {
				graph.unProbed.remove(myPosition);
				graph.setVertexAsProbed(myPosition);
				return MarsUtil.probeAction();
			}
			
		}
		// Move from one vertex to another - MAYBE move this before PHASE if statements
		if (!myPathArray.isEmpty()) {
			String nextVertex = myPathArray.remove(0).name;
			return MarsUtil.gotoAction(nextVertex);
		}

		// Find path to nearest important vertex
		if (!importantVertices.isEmpty() && PHASE == 1) {
			findPathToTheNearestImportantVertex();
			String nextVertex = myPathArray.remove(0).name;
			return MarsUtil.gotoAction(nextVertex);
		}

		// Find path to nearest unexplored vertex
		if (!graph.unProbed.isEmpty()) {
			findPathToTheNearestUnexploredVertex();
			String nextVertex = myPathArray.remove(0).name;
			return MarsUtil.gotoAction(nextVertex);
		}

		// Go back to post if not there
		if (!myPosition.equals(myPost)) {
			findPathToVertex(myPost);

		}

		return MarsUtil.rechargeAction();
	}

	private void findPathToTheNearestUnexploredVertex() {
		Vertex start = graph.getVertex(myPosition);
		myPathArray = PathFinder.FindPath(start, graph.unProbed, graph);
		String target = myPathArray.get(myPathArray.size() - 1).name;
		graph.unProbed.remove(target);
	}

	private void findPathToTheNearestImportantVertex() {
		Vertex start = graph.getVertex(myPosition);
		myPathArray = PathFinder.FindPath(start, importantVertices, graph);
		String target = myPathArray.get(myPathArray.size() - 1).name;
		importantVertices.remove(target);
	}

	private void findPathToVertex(String goalVertex) {
		Vertex start = graph.getVertex(myPosition);
		HashSet<String> goal = new HashSet<String>();
		goal.add(goalVertex);

		myPathArray = PathFinder.FindPath(start, goal, graph);
		String target = myPathArray.get(myPathArray.size() - 1).name;
		graph.unProbed.remove(target);

	}

	@SuppressWarnings({ "deprecation" })
	private void handleAllPercept() {

		Collection<Percept> percepts = getAllPercepts();

		for (Percept p1 : percepts) {
			if (p1.getName().equals("step")) {
				step = p1.getParameters().get(0).toString();

			} else if (p1.getName().equals("lastAction")) {
				lastAction = p1.getParameters().get(0).toString();

			} else if (p1.getName().equals("lastActionParam")) {
				lastActionParam = p1.getParameters().get(0).toString();

			} else if (p1.getName().equals("lastActionResult")) {
				lastActionResult = p1.getParameters().get(0).toString();

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
			// Force close
			// return;
		}

		for (Percept p1 : percepts) {

			if (p1.getName().equals("position")) {
				myPosition = p1.getParameters().get(0).toString();
				if (!graph.hasVertex(myPosition)) {
					graph.addVertex(myPosition);
				}
			} else if (p1.getName().equals("lastActionResult")) {
				String lastActionRes = p1.getParameters().get(0).toString();

				if (lastActionRes.substring(0, 6).equals("failed")) {

					printStringToFile("Step:" + step + "\tAgentID:" + myAgentID
							+ " Position:" + myPosition + "\tLastAction: "
							+ lastAction + lastActionParam
							+ "\t\t lastActionResult:" + lastActionRes,
							"ExplorerAgent_FailedActions.txt");
				}
			} else if (p1.getName().equals("edges")) {
				numberOfEdges = Integer.parseInt(p1.getParameters().get(0)
						.toString());
			} else if (p1.getName().equals("energy")) {
				myEnergy = Integer.parseInt(p1.getParameters().get(0)
						.toString());
			} else if (p1.getName().equals("visibleEdge")) {
				String vertexA = p1.getParameters().get(0).toString();
				String vertexB = p1.getParameters().get(1).toString();
				int edgeCost = 1;

				graph.addEdge(vertexA, edgeCost, vertexB);

				printStringToFile("Step: " + step + "\tExplore edge1: "
						+ vertexA + "\t edge2: " + vertexB + "\t edgeCost: "
						+ edgeCost, "ExplorerAgent_NoSurveyAction.txt");
			}

			else if (p1.getName().equals("surveyedEdge")) {
				String vertexA = p1.getParameters().get(0).toString();
				String vertexB = p1.getParameters().get(1).toString();
				int edgeCost = Integer.parseInt(p1.getParameters().get(2)
						.toString());

				graph.addEdge(vertexA, edgeCost, vertexB);

				printStringToFile("Step: " + step + "\tSurvey edge1: "
						+ vertexA + "\tSurvey edge2: " + vertexB
						+ "\tSurvey edgeCost: " + edgeCost,
						"ExplorerAgent_SurveyAction.txt");
			} else if (p1.getName().equals("probedVertex")) {
				String vertex = p1.getParameters().get(0).toString();
				int vertexWeight = Integer.parseInt(p1.getParameters().get(1)
						.toString());
				graph.setVertexWeight(vertex, vertexWeight);
				printStringToFile("Step: " + step + "\tAgentID: " + myAgentID
						+ "\tProbed vertex: " + vertex + "\tVertex Weight: "
						+ vertexWeight, "ExplorerAgent_ProbeAction.txt");
			}

		}
	}

	// For testing purposes
	private void printStringToFile(String text, String filename) {
		// String path = "logfiles/"+filename;
		//
		// try (PrintWriter out = new PrintWriter(new BufferedWriter(
		// new FileWriter(path, true)))) {
		// out.println(text);
		// } catch (IOException e) {
		// }

	}

	// For testing purposes
	public void resetLogFiles() {
		// if(myAgentID==1)
		// try {
		// new FileOutputStream("logfiles/ExplorerAgent_PathLog.txt").close();
		// new
		// FileOutputStream("logfiles/ExplorerAgent_ProbeAction.txt").close();
		// new FileOutputStream("logfiles/ExplorerAgent_Status.txt").close();
		// new
		// FileOutputStream("logfiles/ExplorerAgent_SurveyAction.txt").close();
		// new
		// FileOutputStream("logfiles/ExplorerAgent_RandomFail.txt").close();
		// new FileOutputStream("logfiles/ExplorerAgent.txt").close();
		// new
		// FileOutputStream("logfiles/ExplorerAgent_FailedActions.txt").close();
		// new FileOutputStream("logfiles/ExplorerAgent_Done.txt").close();
		// new
		// FileOutputStream("logfiles/ExplorerAgent_SortedProbedVertices.txt").close();
		// new FileOutputStream("logfiles/ExplorerAgent_Zones.txt").close();
		// } catch (Exception e) {}
	}

	// For testing purposes
	public void printLogFile() {
		// String status = "Step:"+step+"\tExplored vertices: " +
		// graph.graph.size() + "\t"
		// + "Explored edges: " + graph.edges.size() + "/" + numberOfEdges
		// + "\t Unexplored vertices: " + graph.unExplored.size() +
		// "\tProbed Vertices: "+graph.probedVertices.size();
		// printStringToFile(status, "ExplorerAgent_Status.txt");
		//
	}

}
