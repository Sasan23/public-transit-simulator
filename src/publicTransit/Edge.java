package publicTransit;

import java.io.Serializable;

public class Edge<T> implements Serializable { 

	private static final long serialVersionUID = -5958964710958118594L; // Generate to prevent the warning. 
	private T destination; 
    private String name;
    private int weight;
    private T source;  

    public Edge(T destination, String name, int weight) {
        this.destination = destination;
        this.name = name;
        this.weight = weight;
    }
    
    public Edge(T destination, String name, int weight, T source) {
        this.destination = destination;
        this.name = name;
        this.weight = weight;
        this.source = source;
    }

    public T getDestination(){
        return destination;
    }

    public String getName(){
        return name;
    }

    public int getWeight(){
        return weight;
    }
    
    public T getSource() {
		return source;
	}

    public void setWeight(int weight){
    	if (weight < 0) {
    		throw new IllegalArgumentException("The weight cannot be negative!");
    	}
        this.weight = weight;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

	public void setSource(T source) {
		this.source = source;
	}

	public String toString(){
        return "to " + destination + " with " + name + " takes " + weight + " minutes";
    }
}