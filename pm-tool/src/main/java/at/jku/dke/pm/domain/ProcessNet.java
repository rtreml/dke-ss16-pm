package at.jku.dke.pm.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ProcessNet {
	protected String id;

	protected Map<String, Object> data = new HashMap<>();

	protected Set<Node> nodes = new HashSet<>();

	protected List<Edge> edges = new ArrayList<>();

	public ProcessNet() {
	}

	public ProcessNet(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	public void addNode(Node n) {
		nodes.add(n);
	}

	public Node addNode(String node) {
		Node n = new Node(node);
		nodes.add(n);
		return n;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public void addEdge(Edge e) {
		edges.add(e);
	}

	public Edge addEdge(String source, String target) {
		Edge e = new Edge(source, target);
		edges.add(e);
		return e;
	}

	public static class Node {
		protected String id;

		protected Map<String, Object> data = new HashMap<>();

		public Node() {
		}

		public Node(String id) {
			this.id = id;
		}

		public static Node create(String id) {
			return new Node(id);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public Object getData(String key) {
			return data.get(key);
		}

		public Node addData(String key, Object value) {
			data.put(key, value);
			return this;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj.getClass() != getClass()) {
				return false;
			}
			Node rhs = (Node) obj;
			return new EqualsBuilder().append(id, rhs.id).isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(id).toHashCode();
		}
	}

	public static class Edge {
		protected String source;
		protected String target;

		protected Map<String, Object> data = new HashMap<>();

		public Edge() {
		}

		public Edge(String source, String target) {
			this.source = source;
			this.target = target;
		}

		public static Edge create(String source, String target) {
			return new Edge(source, target);
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public Object getData(String key) {
			return data.get(key);
		}

		public Edge addData(String key, Object value) {
			data.put(key, value);
			return this;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj.getClass() != getClass()) {
				return false;
			}
			Edge rhs = (Edge) obj;
			return new EqualsBuilder().append(source, rhs.source).append(target, rhs.target).isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(source).append(target).toHashCode();
		}

	}
}
