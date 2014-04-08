package mas.agentsUnderdogs.util;
import java.util.ArrayList;
import java.util.Collections;


public class PriorityQueue 
{

	public ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	
	public int Length() 
	{
		 return this.vertices.size();
	}
	
	public boolean Contains(Vertex vertex)
	{
		for (int i = 0; i < this.vertices.size(); i++) {
			if( vertex.name.equals( this.vertices.get(i).name )){
				return true;
			}	
		}
		return false;
	}
	
	public Vertex First() 
	{
		if(this.vertices.size() > 0)
		{
			return (Vertex)this.vertices.remove(0);
		}
		return null;
	}
	
	public Vertex Last() 
	{
		if(this.vertices.size() > 0)
		{
			return (Vertex)this.vertices.remove(this.vertices.size()-1);
		}
		return null;
	}
	
	public void Push(Vertex vertex) 
	{
		this.vertices.add(vertex);

		Collections.sort(this.vertices);
	}
	
	public void Remove(Vertex vertex)
	{
		for (int i = 0; i < this.vertices.size(); i++) {
			if( vertex.name.equals( this.vertices.get(i).name )){
				this.vertices.remove(i);
				break;
			}	
		}
		
		Collections.sort(this.vertices);
	}
	
	public Vertex getVertex(Vertex vertex)
	{
		for (int i = 0; i < this.vertices.size(); i++) {
			if( vertex.name.equals( this.vertices.get(i).name )){
				return this.vertices.get(i);
			}	
		}
		return null;

	}
}
