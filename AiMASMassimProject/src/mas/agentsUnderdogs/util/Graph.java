package mas.agentsUnderdogs.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Graph {

	public Hashtable<String, Vertex> graph;
	public Set<String> edges;
	public ArrayList<Vertex> probedVertices = new ArrayList<Vertex>();
	public int numberOfVertices;

	public HashSet<String> unExplored = new HashSet<String>(800);
	public HashSet<String> unProbed = new HashSet<String>(800);

	public PriorityQueue valuableZones = new PriorityQueue();
	public ArrayList<Vertex> valuableVertices = new ArrayList<Vertex>();
	public ArrayList<String> zoneBorderVertices = new ArrayList<String>();
	public Hashtable<String, ArrayList<String>> zoneVertices = new Hashtable<>(100);
	

	public Graph() {
		this.graph = new Hashtable<>();
		this.numberOfVertices = 0;
		this.edges = new HashSet<String>();

	}

	public Graph(int size) {
		this.graph = new Hashtable<>(size);
		this.numberOfVertices = 0;
		this.edges = new HashSet<String>();
	}

	public void addVertex(String vertexName) {
		if (!hasVertex(vertexName)) {
			Vertex newVertex = new Vertex(vertexName);
			this.graph.put(vertexName, newVertex);
			this.numberOfVertices++;

			this.unExplored.add(newVertex.name);
			this.unProbed.add(newVertex.name);
		}
	}

	public void addVertex(Vertex vertex) {
		String key = vertex.name;
		if (!hasVertex(key)) {
			this.graph.put(key, vertex);
			this.numberOfVertices++;
		}
	}

	public Vertex removeVertex(String vertexName) {
		Vertex vertex = this.graph.remove(vertexName);
		numberOfVertices--;

		return vertex;
	}

	public Vertex getVertex(String vertexName) {
		Vertex vertex = this.graph.get(vertexName);

		return vertex;
	}

	public boolean hasVertex(String vertexName) {
		return this.graph.containsKey(vertexName);
	}

	public void addNeighbourToVertex(String vertexName, int edgeCost,
			String neighbourName) {

		if (!vertexHasNeighbour(vertexName, neighbourName)) {
			Vertex newNeighbour = new Vertex(neighbourName);
			newNeighbour.cost = edgeCost;

			Vertex temp = this.graph.remove(vertexName);
			temp.adj.add(newNeighbour);
			addVertex(temp);
		}

		addToEgdes(vertexName, neighbourName);
	}

	public void addEdge(String vertexName, int edgeCost, String neighbourName) {
		this.addVertex(vertexName);
		this.addVertex(neighbourName);

		addNeighbourToVertex(vertexName, edgeCost, neighbourName);
		addNeighbourToVertex(neighbourName, edgeCost, vertexName);

	}

	public void addToEgdes(String v1, String v2) {
		int t1 = Integer.parseInt(v1.substring(1));
		int t2 = Integer.parseInt(v2.substring(1));
		int[] t = { t1, t2 };
		Arrays.sort(t);
		String edge = "";
		for (int i = 0; i < t.length; i++) {
			edge += "v" + t[i];
		}
		edges.add(edge);
	}

	public void setVertexWeight(String vertexName, int weight) {
		Vertex vertex = removeVertex(vertexName);
		vertex.cost = weight;
		addVertex(vertex);

		if (!probedVertices.contains(vertex)) {
			probedVertices.add(vertex);

			if (weight == 10) {
				valuableVertices.add(vertex);
			}

//			if (probedVertices.size() == 400) {
//				findValuabeZones();
//			}
		}

	}

	public int getVertexWeight(String vertexName) {
		return getVertex(vertexName).cost;
	}

	public boolean isVertexProbed(String vertexName) {
		return getVertex(vertexName).isProbed;
	}

	public boolean isVertexSurveyed(String vertexName) {
		return getVertex(vertexName).isSurveyed;
	}

	public void setVertexAsSurveyed(String vertexName) {
		Vertex v = this.graph.remove(vertexName);
		v.isSurveyed = true;
		addVertex(v);
	}

	public void setVertexAsProbed(String vertexName) {
		Vertex v = this.graph.remove(vertexName);
		v.isProbed = true;
		addVertex(v);
	}

	public void expandZoneWithCenter(String centerVertex) {
		ArrayList<String> visited = new ArrayList<>();
		Vertex epiCenter = this.getVertex(centerVertex);

		Vertex zone = new Vertex(epiCenter.name);
		zone.cost += getVertex(epiCenter.name).cost; // this.getVertexWeight(epiCenter.name);
		visited.add(zone.name);

		for (Vertex neighbour : epiCenter.adj) {
			Vertex neigh = this.getVertex(neighbour.name);
			if (!visited.contains(neighbour.name)) {
				zone.cost += neigh.cost;
				visited.add(neigh.name);
			}
			zone.removeNeighbourIfExists(neigh.name); // remove first degree
														// neighbours
			
			for (Vertex neighboursNeighbour : neigh.adj) {
				Vertex neighsNeigh = this.getVertex(neighboursNeighbour.name);
				if (!visited.contains(neighboursNeighbour.name)) {
					zone.cost += neighsNeigh.cost;
					// zone.adj.add(neighsNeigh);
					visited.add(neighsNeigh.name);
				}
				zone.removeNeighbourIfExists(neighsNeigh.name); // remove second degree neighbours

				for (Vertex neighboursNeighboursNeigh : neighsNeigh.adj) {
					Vertex neighsNeighsNeigh = this
							.getVertex(neighboursNeighboursNeigh.name);
					if (!visited.contains(neighsNeighsNeigh.name)) {
						zone.cost += neighsNeighsNeigh.cost;
						zone.adj.add(neighsNeighsNeigh);
						visited.add(neighsNeighsNeigh.name);
					}
				}
			}
		}
		valuableZones.Push(zone);
		zoneVertices.put(epiCenter.name, visited);
	}

	public void findValuabeZones() {

		ArrayList<String> visited = new ArrayList<>();
		for (Vertex root : valuableVertices) {
			Vertex zone = new Vertex(root.name);
			zone.cost += getVertex(root.name).cost;
			visited.add(zone.name);

			for (Vertex neighbour : root.adj) {
				Vertex neigh = this.getVertex(neighbour.name);
				if (!visited.contains(neighbour.name)) {
					zone.cost += neigh.cost;
					visited.add(neigh.name);
				}
				zone.removeNeighbourIfExists(neigh.name); // remove first degree
															// neighbours

				for (Vertex neighboursNeighbour : neigh.adj) {
					Vertex neighsNeigh = this
							.getVertex(neighboursNeighbour.name);
					if (!visited.contains(neighboursNeighbour.name)) {
						zone.cost += neighsNeigh.cost;
						// zone.adj.add(neighsNeigh);
						visited.add(neighsNeigh.name);
					}
					zone.removeNeighbourIfExists(neighsNeigh.name); // remove
																	// second
																	// degree
																	// neighbours

					for (Vertex neighboursNeighboursNeigh : neighsNeigh.adj) {
						Vertex neighsNeighsNeigh = this
								.getVertex(neighboursNeighboursNeigh.name);
						if (!visited.contains(neighsNeighsNeigh.name)) {
							zone.cost += neighsNeighsNeigh.cost;
							zone.adj.add(neighsNeighsNeigh);
							visited.add(neighsNeighsNeigh.name);
						}
					}
				}
			}
			valuableZones.Push(zone);
			visited.clear();
		}

		chooseTheMostValuableZone();
	}

	public Vertex getMostValuableZone() {
		Vertex zoneBorderPosts = this.valuableZones.vertices.get(this.valuableZones.vertices
				.size() - 1);
		this.unProbed.clear();
		ArrayList<String> zoneVertices1 = this.zoneVertices.get(zoneBorderPosts.name);
		for (String v : zoneVertices1) {
			if(this.graph.get(v).cost == 0){
				Vertex vert = this.getVertex(v);
				vert.isProbed = false;
				unProbed.add(v);
				this.graph.put(vert.name, vert);

			}
			
		}

		return zoneBorderPosts;
	}
	
	public void chooseTheMostValuableZone() {
		
		Vertex zone = this.getMostValuableZone();
		for (Vertex v : zone.adj) {
			zoneBorderVertices.add(v.name);
		}
		
		for (String v : this.zoneVertices.get(zone.name) ) {
			if( !zoneBorderVertices.contains(v) )
				zoneBorderVertices.add(v);
		}

		for (Vertex b : zone.adj) {
			zoneBorderVertices.add(b.name);
		}

		// ArrayList<String> duplicate = new ArrayList<String>();
		// duplicate.addAll(zoneBorderVertices);
		// Collections.shuffle(duplicate);
		// zoneBorderVertices.addAll(duplicate);

		// String zoneCenter = getMostValuableZone().name;
		// for (int i = 0; i < 28; i++) {
		// zoneBorderVertices.add(zoneCenter);
		// }

	}

	public String popFromZoneBorderVertices() {

		if (!zoneBorderVertices.isEmpty()) {
			String borderVertex = zoneBorderVertices.remove(0);
			// trimBorder(borderVertex);
			return borderVertex;

		} else {

			return getMostValuableZone().name;
		}

	}

	// public String popFromZoneBorderVertices() {
	//
	// if(!zoneBorderVertices.isEmpty()){
	// String borderVertex = zoneBorderVertices.remove(0);
	// //trimBorder(borderVertex);
	// return borderVertex;
	//
	// } else {
	//
	// return getMostValuableZone().name;
	// }
	//
	// }

	// private void trimBorder(String vertex){
	// ArrayList<String> temp = new ArrayList<String>(zoneBorderVertices);
	// zoneBorderVertices.clear();
	// Vertex takenVertex = this.graph.get(vertex);
	//
	// for (String borderVertex : temp) {
	// if(!takenVertex.hasNeighbour(borderVertex)){
	// zoneBorderVertices.add(borderVertex);
	// }
	//
	// }
	//
	// }

	public boolean vertexHasNeighbour(String vertexName, String neighbourName) {
		return this.graph.get(vertexName).hasNeighbour(neighbourName);
	}

	public ArrayList<Vertex> getNeighbours(String vertexName) {

		ArrayList<Vertex> neighbours = new ArrayList<Vertex>();

		for (int i = 0; i < this.graph.get(vertexName).adj.size(); i++) {

			Vertex temp = new Vertex();
			temp.name = this.graph.get(vertexName).adj.get(i).name;
			temp.cost = this.graph.get(vertexName).adj.get(i).cost;
			neighbours.add(temp);

		}
		// ArrayList<Vertex> neighbours = new
		// ArrayList<Vertex>(this.graph.get(vertexName).adj);

		return neighbours;
	}

	public void printToString() {

		Enumeration<String> items = graph.keys();

		while (items.hasMoreElements()) {

			String vertex = items.nextElement();

			System.out.print(vertex + "\t: ");
			ArrayList<Vertex> neighbours = this.graph.get(vertex).adj;
			for (int i = 0; i < neighbours.size(); i++) {
				System.out.print("(" + neighbours.get(i).cost + ","
						+ neighbours.get(i).name + ")  ");

			}
			System.out.println("");

		}

	}

	public void printToTxtFile() throws FileNotFoundException,
			UnsupportedEncodingException {

		PrintWriter writer = new PrintWriter("logfiles/Graph.txt", "UTF-8");
		writer.println("Graph (Vertices=" + this.graph.size() + ", Edges="
				+ this.edges.size() + ")");

		Enumeration<String> items = graph.keys();

		while (items.hasMoreElements()) {

			String vertex = items.nextElement();
			writer.print(vertex + "\t: ");
			ArrayList<Vertex> neighbours = this.graph.get(vertex).adj;
			for (int i = 0; i < neighbours.size(); i++) {
				writer.print("(" + neighbours.get(i).cost + ","
						+ neighbours.get(i).name + ")  ");

			}
			writer.println("");

		}
		writer.println("-------End of file-------");
		writer.close();

	}

	public void printProbedToTxtFile() throws FileNotFoundException,
			UnsupportedEncodingException {

		PrintWriter writer = new PrintWriter(
				"logfiles/ExplorerAgent_SortedProbedVertices.txt", "UTF-8");
		writer.println("Probed vertices sorted, ascending");
		Collections.sort(this.probedVertices);
		for (int i = 0; i < probedVertices.size(); i++) {
			writer.print(probedVertices.get(i).name + "\t");
			writer.println(probedVertices.get(i).cost);
		}

		writer.close();

	}

	public void printZonesToTxtFile() throws FileNotFoundException,
			UnsupportedEncodingException {

		PrintWriter writer = new PrintWriter(
				"logfiles/ExplorerAgent_Zones.txt", "UTF-8");
		writer.println("Zones:");

		for (Vertex zone : valuableZones.vertices) {
			writer.print(zone.name + "\t" + zone.cost + "\t(");
			for (Vertex neigh : zone.adj) {
				writer.print(neigh.name + ", ");
			}
			writer.println(")");
		}
		writer.close();

	}

}
