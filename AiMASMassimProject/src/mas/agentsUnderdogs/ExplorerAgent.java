package mas.agentsUnderdogs;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import mas.agentsUnderdogs.util.*;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;
import eis.iilang.Action;
import eis.iilang.Percept;

public class ExplorerAgent extends Agent {
	
	// Shared graph between all agents
	private static Graph graph = new Graph(1200);
	private static int agentCount = 1;
	private static ArrayList<String> vipVertices = new ArrayList<String>();
	
	private static boolean done=true; //Debug
	private static int occupiedZoneVertices=0; //Debug
	
	// Not-shared variables
	private ArrayList<String> unVisited = new ArrayList<String>();
	private ArrayList<Vertex> pathArray = new ArrayList<Vertex>();
	private boolean isSurveyed = false;
	private boolean lastActionFailed=false;
	private String myPosition;
	private String step;
	private String lastAction;
	private String lastActionResult;
	private String lastActionParam;
	private Action lastActionObj;
	private int numberOfEdges;
	private int energy;
	private int agentID;

	public ExplorerAgent(String name, String team) {
		super(name, team);
		agentID = agentCount++;
		printStringToFile("#Initialized agent nr: " + agentID,
				"ExplorerAgent.txt");
		resetLogFiles();
	}

	@Override
	public void handlePercept(Percept p) {

	}

	@Override
	public Action step() {
		
		// Get all precept and process
		handleAllPercept();
		
		// Repeat last action if random failed
		if(lastActionFailed){
			lastActionFailed = false;
			return lastActionObj;
		}

		// When all vertex weights are known - TODO maybe delete?
		if (graph.probedVertices.Length() == 400) {
			if(done){
				try {
					done = false;
					unVisited.clear();
					
					Helper.playTadaSound();
					graph.printProbedToTxtFile();//Debugging purposes
					graph.printZonesToTxtFile();
				} catch (Exception e) {} 
			}
		}
		
		if (graph.zoneBorderVertices.size() > 0 && occupiedZoneVertices < 8) {
			String vertexToBeOccupied = graph.popFromZoneBorderVertices();
			if(vertexToBeOccupied != null){
				unVisited.add(vertexToBeOccupied);
				occupiedZoneVertices++;
			}
		}
		
		printLogFile(); // For debuging purposes
		
		// Recharge when flat
		if (energy < 10) {
			return MarsUtil.rechargeAction();
		}
		
		// Survey vertex if not surveyed
		if (!isSurveyed && pathArray.isEmpty()) {
			isSurveyed = false;
			return MarsUtil.surveyAction();
		}
		
		// Probe vertex if not probed
		if(!graph.isVertexProbed(myPosition)){
			return MarsUtil.probeAction();
		}
		
		
		// Mark as "visited" when standing on a vertex
		if (unVisited.contains(myPosition)) {
			unVisited.remove(myPosition);
			return MarsUtil.surveyAction();
		}
		
		// Move from one vertex to another
		if (!pathArray.isEmpty()) {
			String nextVertex= pathArray.remove(0).name;
			return MarsUtil.gotoAction(nextVertex); 
		}
		

		if (!unVisited.isEmpty()) {
			int size = unVisited.size();
			String nextVertex = unVisited.get(size - 1);
			findPathToVertex(nextVertex);			
			return MarsUtil.skipAction();
		}
		

		return MarsUtil.rechargeAction();
	}
	
	
	private void findPathToVertex(String vertex){
		Vertex start = graph.getVertex(myPosition);
		Vertex goal = graph.getVertex(vertex);
		pathArray = PathFinder.FindPath(start, goal, graph);
	}

	@SuppressWarnings({ "deprecation" })
	private void handleAllPercept() {
		Collection<Percept> percepts = getAllPercepts();

		for (Percept p1 : percepts) {
			if (p1.getName().equals("step")) {
				step = p1.getParameters().get(0).toString();
				
			}else if (p1.getName().equals("lastAction")) {
				lastAction = p1.getParameters().get(0).toString();
				
			}else if (p1.getName().equals("lastActionParam")) {
				lastActionParam = p1.getParameters().get(0).toString();
				
			}else if (p1.getName().equals("lastActionResult")) {
				lastActionResult = p1.getParameters().get(0).toString();
				
			}
		}
		//In case of random fail, repeat last action
		if(lastActionResult.equals("failed_random")){
			lastActionFailed = true;
			if(lastAction.equals("goto")){
				lastActionObj = MarsUtil.gotoAction(lastActionParam);
			}else if(lastAction.equals("survey")){
				lastActionObj = MarsUtil.surveyAction();
			}else if(lastAction.equals("probe")){
				lastActionObj = MarsUtil.probeAction();
			}else{
				lastActionObj = MarsUtil.skipAction();
			}
			printStringToFile("AgentID:"+agentID+" - "+lastAction+"("+lastActionParam+")","ExplorerAgent_RandomFail.txt");
			// Force close method
			return;
		}
		
		
		for (Percept p1 : percepts) {

			if (p1.getName().equals("position")) {
				myPosition = p1.getParameters().get(0).toString();
				if (!graph.hasVertex(myPosition)) {
					graph.addVertex(myPosition);
				}
			}
			else if (p1.getName().equals("lastActionResult")) {
				String lastActionRes = p1.getParameters().get(0).toString();

				if (lastActionRes.substring(0, 6).equals("failed")) {

					printStringToFile("Step:"+step+"\tAgentID:"+agentID+" Position:" + myPosition
							+ "\tLastAction: " + lastAction+lastActionParam
							+ "\tlastActionResult:" + lastActionRes,
							"ExplorerAgent_FailedActions.txt");
				}
			}
			else if (p1.getName().equals("edges")) {
				numberOfEdges = Integer.parseInt(p1.getParameters().get(0)
						.toString());
			}
			else if (p1.getName().equals("energy")) {
				energy = Integer.parseInt(p1.getParameters().get(0).toString());
			}

			else if (p1.getName().equals("surveyedEdge")) {
				String vertexA = p1.getParameters().get(0).toString();
				String vertexB = p1.getParameters().get(1).toString();
				int edgeCost = Integer.parseInt(p1.getParameters().get(2)
						.toString());
				isSurveyed = true;

				if (!graph.hasVertex(vertexA)) {
					unVisited.add(vertexA);
				}
				if (!graph.hasVertex(vertexB)) {
					unVisited.add(vertexB);
				}
				graph.addEdge(vertexA, edgeCost, vertexB);

				printStringToFile("Step: " + step + "\tSurvey edge1: "
						+ vertexA + "\tSurvey edge2: " + vertexB
						+ "\tSurvey edgeCost: " + edgeCost, "ExplorerAgent_SurveyAction.txt");
			}
			
			else if (p1.getName().equals("probedVertex")) {
				String vertex = p1.getParameters().get(0).toString();
				int vertexWeight = Integer.parseInt(p1.getParameters().get(1).toString());
				graph.setVertexWeight(vertex, vertexWeight);
				printStringToFile("Step: " + step + "\tAgentID: "+agentID+"\tProbed vertex: " + vertex 
						+ "\tVertex Weight: " + vertexWeight, "ExplorerAgent_ProbeAction.txt");
			}

		}
	}

	// For testing purposes
	private void printStringToFile(String text, String filename) {
		String path = "logfiles/"+filename;
		
//		try (PrintWriter out = new PrintWriter(new BufferedWriter(
//				new FileWriter(path, true)))) {
//			out.println(text);
//		} catch (IOException e) {
//		}

	}
	// For testing purposes
	public void resetLogFiles(){
		if(agentID==1)
			vipVertices.add("v33");
			vipVertices.add("v135");
			vipVertices.add("v214");
			vipVertices.add("v100");
			vipVertices.add("v387");
			vipVertices.add("v182");
			vipVertices.add("v41");
			vipVertices.add("v314");

			try {
				new FileOutputStream("logfiles/ExplorerAgent_PathLog.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_ProbeAction.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_Status.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_SurveyAction.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_RandomFail.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_FailedActions.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_Done.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_SortedProbedVertices.txt").close();
				new FileOutputStream("logfiles/ExplorerAgent_Zones.txt").close();
			} catch (Exception e) {}
	}
	// For testing purposes
	public void printLogFile() {
		String status = "Step:"+step+"\tExplored vertices: " + graph.graph.size() + "\t"
				+ "Explored edges: " + graph.edges.size() + "/" + numberOfEdges
				+ "\t " + "Unvisited vertices: " + unVisited.size() + "\tProbed Vertices: "+graph.probedVertices.Length();
		printStringToFile(status, "ExplorerAgent_Status.txt");
		
	}

}
