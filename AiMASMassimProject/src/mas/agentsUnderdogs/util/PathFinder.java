package mas.agentsUnderdogs.util;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;


public class PathFinder {
	
	public static PriorityQueue closedList, openList;
	
	// Find the path
	private static ArrayList<Vertex> CalculatePath (Vertex vertex)
	{
		ArrayList<Vertex> path = new ArrayList<Vertex>();
		while (vertex != null) 
		{
			path.add(vertex);
			vertex = vertex.parent;
		}
		Collections.reverse(path);
		
		path.remove(0);
		return path;
	}
	
	public static ArrayList<Vertex> FindPath(Vertex start, HashSet<String> goal, Graph graph) 
	{
		closedList = new PriorityQueue();
		openList = new PriorityQueue();
		start.cost = 0;
		openList.Push(start);
		
		Vertex currentVertex = null;
		
		while(openList.Length() != 0)
		{
			currentVertex = openList.First();
			closedList.Push(currentVertex);
			// Check if the current node is the goal node
			if(goal.contains(currentVertex.name))
			{
				return CalculatePath(currentVertex);
			}
			// Create an ArrayList to store the neighbor nodes
			ArrayList<Vertex> neighbours = new ArrayList<Vertex>();
			neighbours = graph.getNeighbours(currentVertex.name);

			for (int i = 0; i < neighbours.size(); i++) 
			{
				Vertex neighbourVertex = (Vertex) neighbours.get(i);
				
				int cost = currentVertex.cost + neighbourVertex.cost;
				
				if(openList.Contains(neighbourVertex) && cost < openList.getVertex(neighbourVertex).cost){
					openList.Remove(neighbourVertex);
				}
				
				if(closedList.Contains(neighbourVertex) && cost < closedList.getVertex(neighbourVertex).cost){
					closedList.Remove(neighbourVertex);
				}
				
				
				if( !openList.Contains(neighbourVertex) && !closedList.Contains(neighbourVertex))
				{
					neighbourVertex.cost = cost;
					neighbourVertex.parent = currentVertex;
					
					openList.Push(neighbourVertex);
				}
			}	
		}
		
		if(!goal.contains(currentVertex.name)) 
		{
			
			try {
				//graph.printToTxtFile();
				//printSetToTxtFile(goal, start.name);
				//printArrayToTxtFile(closedList.vertices);
			} catch (Exception e) {} 
			
			System.err.println("Goal Not Found");
			return null;
		}

		
		return CalculatePath(currentVertex);
	}
	
	public static void printSetToTxtFile(HashSet<String> set, String start) throws FileNotFoundException, UnsupportedEncodingException{
		
		PrintWriter writer = new PrintWriter("logfiles/Unexplored_set.txt", "UTF-8");
		writer.println("HashSet (Vertices="+set.size()+")");
		writer.print("Start: "+start +"\n ");
		Iterator<String> items = set.iterator();

		while(items.hasNext()){
			
			String vertex = items.next();
			writer.print(vertex +"\n ");
			
		}
		writer.println("-------End of file-------");
		writer.close();

	}
	
	public static void printArrayToTxtFile(ArrayList<Vertex> list) throws FileNotFoundException, UnsupportedEncodingException{
		
		PrintWriter writer = new PrintWriter("logfiles/ClosedList.txt", "UTF-8");
		writer.println("Closed List: (Vertices="+list.size()+")");
		
		for (Vertex vertex : list) {
			writer.print(vertex.name +"\n ");
		}
		writer.println("-------End of file-------");
		writer.close();
	}
	
	

}
