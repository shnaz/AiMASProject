package mas.agentsUnderdogs.util;
import java.util.ArrayList;
import java.util.Collections;


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
	
	public static ArrayList<Vertex> FindPath(Vertex start, Vertex goal,Graph graph) 
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
			if(currentVertex.name.equals(goal.name))
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
		
		if(currentVertex.name != goal.name) 
		{
			System.err.println("Goal Not Found");
			return null;
		}
		
		return CalculatePath(currentVertex);
	}

}
