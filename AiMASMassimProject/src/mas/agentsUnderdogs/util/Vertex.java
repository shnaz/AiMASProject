package mas.agentsUnderdogs.util;

import java.util.ArrayList;


public class Vertex implements Comparable<Vertex>{
	
	public String name;
	public ArrayList<Vertex> adj;
	public int cost;
	public Vertex parent;
		
	public Vertex() {
		this.adj = new ArrayList<Vertex>();
		this.cost = 0;
		this.parent = null;
		this.name = "";
	}
	
	public Vertex(String name) {
		this.adj = new ArrayList<Vertex>();
		this.cost = 0;
		this.parent = null;
		this.name = name;
	}
	
	public void addNeighbour(Vertex v){
		if(!hasNeighbour(v.name)){
			this.adj.add(v);	
		}
	}
	
	public void addNeighbour(String name, int cost ){
		if(!hasNeighbour(name)){
			Vertex newNeigh = new Vertex(name);
			newNeigh.cost = cost;
		
			this.adj.add(newNeigh);	
		}
	}
	
	public void removeNeighbourIfExists(String name){
		for (int i = 0; i < adj.size(); i++) {
			if(name.equals(this.adj.get(i).name)){
				adj.remove(i);
				return;
			}
		}
		
	}
	
	public boolean hasNeighbour(String v){
		boolean edgeExists = false;
		
		for (int i = 0; i < adj.size(); i++) {
			if(v.equals(this.adj.get(i).name)){
				edgeExists = true;
			}
				
		}
		return edgeExists;
	}
	
	public int getCost(){
		return this.cost;
	}
	
	public int compareTo(Vertex compareVertex) {
		 
		int compareCost = ((Vertex) compareVertex).getCost(); 
		
		if(this.cost < compareCost){
			return -1;
		}
		
		if(this.cost > compareCost){
			return 1;
		}
		
		return 0;
		
		//ascending order
		//return this.cost - compareCost;
 
		//descending order
		//return compareQuantity - this.quantity;
 
	}


}
