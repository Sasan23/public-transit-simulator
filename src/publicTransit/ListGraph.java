package publicTransit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ListGraph<T> implements Graph<T>, Serializable {

	private static final long serialVersionUID = -7223573296806723525L; // Generated because of the warning. 
	private Map<T, Set<Edge<T>>> nodes = new HashMap<>();
	// The below fields are for the Dijkstra implementation. 
	private Set<T> settledNodes;
	private Set<T> unSettledNodes;
	private Map<T, T> predecessors; 
	private Map<T, Integer> distance; // The integer is the shortest distance. 
	
	@Override // Adds a new node in our Map. 
	public void add(T node) {
		nodes.putIfAbsent(node, new HashSet<>());
	}

	@Override // Connects two nodes by creating two edges that point to the respective node, and adds them to the set of edges. 
	public void connect(T node1, T node2, String name, int weight) {
		if(!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
			throw new NoSuchElementException("connect: Both of these nodes are not in the Graph!");
		} 
		if (weight < 0) {
			throw new IllegalArgumentException("connect: The weight is negative!");
		}
		if (!(getEdgeBetween(node1, node2) == null)) {
			throw new IllegalStateException("connect: These nodes are already connected!");
		} 
		Set<Edge<T>> se1 = nodes.get(node1);
		Edge<T> e1 = new Edge<T>(node2, name, weight, node1); 
		se1.add(e1);
		Set<Edge<T>> se2 = nodes.get(node2);
		Edge<T> e2 = new Edge<T>(node1, name, weight, node2); 
		se2.add(e2);
	}

	@Override // Does the opposite of the above. Removes a connection between two nodes. 
	public void disconnect(T node1, T node2) {
		if(!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
			throw new NoSuchElementException("disconnect: Both of these nodes are not in the Graph!");
		} 
		if (getEdgeBetween(node1, node2) == null) {
			throw new IllegalStateException("disconnect: These nodes are not connected!");
		} 
		oneWayDisconnect(node1, node2);
		oneWayDisconnect(node2, node1);
	}
	
	// Help method to the above. Removes all edges from node1 that connect to node2. 
	private void oneWayDisconnect (T node1, T node2) {
		Set<Edge<T>> edgeSetCopy = new HashSet<>();
		for (Edge<T> edge: nodes.get(node1)) {
			if (edge.getDestination().equals(node2)) {
				edgeSetCopy.add(edge);
			}
		}  
		// All of this is to avoid a ConcurrentModificationException. An iterator could have been an alternative. 
		for (Edge<T> edge: edgeSetCopy) {
			if (edge.getDestination().equals(node2)) {
				nodes.get(node1).remove(edge);
			}
		}
	}

	@Override // This returns the edge that points from node1 to node2. 
	public Edge<T> getEdgeBetween(T from, T to) { 
		if(!nodes.containsKey(from) || !nodes.containsKey(to)) {
			throw new NoSuchElementException("getEdgeBetween: Both of these nodes are not in the Graph!");
		} 
		for (Edge<T> edge: nodes.get(from)) {
			if (edge.getDestination().equals(to)) {
				return edge;
			}
		}
		return null;
	}

	@Override // Returns the set of edges from a node. 
	public Collection<Edge<T>> getEdgesFrom(T node) {
		if(!nodes.containsKey(node)) {
			throw new NoSuchElementException("getEdgesFrom: This node is not in the Graph!");
		} 
		return nodes.get(node);
	}

	@Override // Getter for all our nodes. 
	public Set<T> getNodes() {
		Set<T> copy = new HashSet<>();
		copy.addAll(nodes.keySet());
		return copy;
	}

	@Override // Returns the shortest path (using Breath First Search) between two nodes by calling gatherPath.
	public List<Edge<T>> getPath(T from, T to) {
		Set<T> visited = new HashSet<>();
		LinkedList<T> queue = new LinkedList<>();
		Map<T, T> via = new HashMap<>(); // A Map of connections between two nodes. 
		visited.add(from);
		queue.add(from);
		while(!queue.isEmpty()) {
			T node = queue.pollFirst();
			for(Edge<T> e: nodes.get(node)) { // For each Edge within this one node. 
				T toNode = e.getDestination();
				if(!visited.contains(toNode)) { // Here we "branch out", checking off each node we have yet to visit. 
					queue.add(toNode);
					visited.add(toNode);
					via.put(toNode, node);
				}
			}
		}
		if (!visited.contains(to)) { 
			return null;
		}
		return gatherPath(from, to, via);
	}
	
	// Help method to the above, which takes two Nodes and a Map of paths, and returns the linear path between the Nodes. 
	private List<Edge<T>> gatherPath(T from, T to, Map<T, T> via) {
		List<Edge<T>> path= new ArrayList<>();
		T where = to;
		while(!where.equals(from)) { // Here we trace back from our "to" back to our "from". 
			T node = via.get(where);
			Edge<T> e = getEdgeBetween(node, where);
			path.add(e);
			where = node;
		}
		Collections.reverse(path);
		return path;
	}	
	
	// The below source was used to implement Dijkstra's algorithm below. 
	// https://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
	// Generates the shortest path in List for between the source and target, using Dijkstra's algorithm. 
    public List<Edge<T>> getDijkstraPath(T target, T source) {
    	execute(source);
        LinkedList<T> path = new LinkedList<T>();
        T step = target;
        // Check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return gatherDijkstraPath(path);
    }
    
	private List<Edge<T>> gatherDijkstraPath(LinkedList<T> list) {
		List<Edge<T>> edges = new ArrayList<>();
		for (int i=0; i<list.size()-1; i++) {
			edges.add(getEdgeBetween(list.get(i), list.get(i+1)));
		}
		return edges;
	}

	// Generates the minimal distance by calling findMinimalDistances to add to predecessors. 
    private void execute(T source) {
    	
    	settledNodes = new HashSet<T>(); // These fields are at the top of this class. 
    	unSettledNodes = new HashSet<T>();
    	predecessors = new HashMap<T, T>();
    	distance = new HashMap<T, Integer>();
    	
        distance.put(source, 0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            T node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }
    
	// Finds the minimal distance for the node to each neighboring node. Adds it to the distance Map. 
    private void findMinimalDistances(T node) {
        List<T> adjacentNodes = getNeighbors(node); // Gets all neighbors of our node. 
        for (T target: adjacentNodes) { // Loop through all neighbors. 
            if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
            	// The put method replaces the previous target if it was there. 
                distance.put(target, getShortestDistance(node) + getDistance(node, target)); // Replaces with a shorter distance here. 
                predecessors.put(target, node); // And the node here as the value. 
                unSettledNodes.add(target);
            }
        }
    }
    
	// Returns the "distance" (or weight) between the two passed nodes. 
    private int getDistance(T node, T target) { 
    	for (Edge<T> edge: nodes.get(node)) {
    		if (edge.getDestination().equals(target)) {
    			return edge.getWeight();
    		}
    	}
    	throw new RuntimeException("Should not happen");
    }
	
	// Returns all neighboring nodes to our node. 
    private List<T> getNeighbors(T node) {
    	List<T> neighbors = new ArrayList<T>();
    	for (Edge<T> edge: nodes.get(node)) {
    		if (!settledNodes.contains(edge.getDestination())) {
    			neighbors.add(edge.getDestination());
    		}
    	}
    	return neighbors; 
    }
    
	// Returns the node with the shortest distance of the ones passed. 
    private T getMinimum(Set<T> nodes) {
        T minimum = null;
        for (T node: nodes) {
            if (minimum == null) {
                minimum = node;
            } else {
                if (getShortestDistance(node) < getShortestDistance(minimum)) {
                    minimum = node;
                }
            }
        }
        return minimum;
    }
    
	// Checks if the node has a distance, if not return MAX. If so, return the distance. 
    private int getShortestDistance(T destination) { 
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }
    
    

	@Override // Calls depthFirstSearch to see if the visited places included our "to" destination. 
	public boolean pathExists(T from, T to) {
		if(!nodes.containsKey(from) || !nodes.containsKey(to)) {
			return false; // We return false if neither node exists in our Map. 
		} 
		Set<T> visited = new HashSet<>();
		depthFirstSearch(from, visited);
		return visited.contains(to);
	}
	
	// DFS. This is for pathExists to call. The visited Set will have all visited Nodes. 
	private void depthFirstSearch(T where, Set<T>visited) {
		visited.add(where);
		for(Edge<T> e : nodes.get(where)) {
			if(!visited.contains(e.getDestination())) {
				depthFirstSearch(e.getDestination(), visited);
			}
		}
	}
	
	@Override // Removes a node and all its edges, as well as all the edges in other nodes that point to this node. 
	public void remove(T node) {
		if (nodes.containsKey(node)) {
			for (Set<Edge<T>> edgeSet: nodes.values()) { // For all the sets of edges for all our nodes. 
				Set<Edge<T>> edgesToRemove = new HashSet<>();
				for (Edge<T> edge: edgeSet) { // For all the edges in each of those sets. 
					if (edge.getDestination().equals(node)) { // We use getDestination to check equality. 
						edgesToRemove.add(edge);
					}
				} 
				for (Edge<T> edge: edgesToRemove) { // We have it here, and not above, to avoid ConcurrentModificationException. 
					edgeSet.remove(edge);
				}
				// Note that we need getDestination because two Edges for opposite nodes will NOT be equal.
			} // That is because their destinations obviously differ (it is the opposite node). 
			nodes.remove(node); // Finally remove the node fully from our nodes set. 
		} else {
			throw new NoSuchElementException("remove: This node is not in our Graph!");
		} 
	}

	@Override // Updates the weight of two nodes that are connection, by updating the two edges. 
	public void setConnectionWeight(T node1, T node2, int weight) {
		if(!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
			throw new NoSuchElementException("setConnectionWeight: Both of these nodes are not in the Graph!");
		} 
		if (getEdgeBetween(node1, node2) == null) {
			throw new IllegalStateException("setConnectionWeight: These nodes are not connected!");
		} 
		if (weight < 0) {
			throw new IllegalArgumentException("setConnectionWeight: The weight is negative!");
		}
		Edge<T> edgeFrom1to2 = getEdgeBetween(node1, node2);
		Edge<T> edgeFrom2to1 = getEdgeBetween(node2, node1);
		edgeFrom1to2.setWeight(weight);
		edgeFrom2to1.setWeight(weight);
	}
	
	public void changeConnection(T node1, T node2, int weight, String name) {
		setConnectionWeight(node1, node2, weight);
		Edge<T> edgeFrom1to2 = getEdgeBetween(node1, node2);
		Edge<T> edgeFrom2to1 = getEdgeBetween(node2, node1);
		edgeFrom1to2.setName(name);;
		edgeFrom2to1.setName(name);
	}
	
	@Override
	public String toString() {
		String string= "";
		for(T node : nodes.keySet()) { 
			string += node + ": ";
			for (Edge<T> e : nodes.get(node)) {
				string += "\n";
				string += "    " + e;
			}
			string += "\n";
		}
		return string;
	}
}
