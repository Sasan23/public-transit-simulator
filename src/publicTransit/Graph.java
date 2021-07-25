package publicTransit;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Graph<N> extends Serializable { 

	void add(N node);

	void connect(N node1, N node2, String name, int weight);

	void disconnect(N node1, N node2);

	Edge<N> getEdgeBetween(N node1, N node2);

	Collection<Edge<N>> getEdgesFrom(N node);

	Set<N> getNodes();

	List<Edge<N>> getPath(N from, N to);

	boolean pathExists(N from, N to);

	void remove(N node);

	void setConnectionWeight(N node1, N node2, int weight);
}
