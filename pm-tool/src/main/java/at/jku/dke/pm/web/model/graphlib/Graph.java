package at.jku.dke.pm.web.model.graphlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @formatter:off { "options": { "directed": true, "multigraph": false,
 *                "compound": false }, "nodes": [ { "v": "a", "value": {
 *                "label": "node a" } }, { "v": "b", "value": { "label":
 *                "node b" } } ], "edges": [ { "v": "a", "w": "b", "value": {
 *                "label": "edge a->b" } } ] }
 * @formatter:on
 * @author Tremlro
 * 
 */
public class Graph {

	protected Options options;

	protected List<Node> nodes = new ArrayList<Node>();

	protected List<Edge> edges = new ArrayList<Edge>();

	protected Map<String, String> value = new HashMap<String, String>();

	public Graph() {
		this.options = new Options();
	}

	public Graph(boolean directed, boolean multigraph, boolean compound) {
		this.options = new Options(directed, multigraph, compound);
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public void addNode(Node node) {
		if (!nodes.contains(node))
			this.nodes.add(node);
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

	public Map<String, String> getValue() {
		return value;
	}

	public void setValue(Map<String, String> value) {
		this.value = value;
	}

	public void addValue(String key, String val) {
		this.value.put(key, val);
	}

	public void addValues(Map<String, String> values) {
		this.value.putAll(values);
	}
	
	public static class Options {
		protected boolean directed = true;
		protected boolean multigraph = false;
		protected boolean compound = false;

		public Options() {
		}

		public Options(boolean directed, boolean multigraph, boolean compound) {
			this.directed = directed;
			this.multigraph = multigraph;
			this.compound = compound;
		}

		public boolean isDirected() {
			return directed;
		}

		public void setDirected(boolean directed) {
			this.directed = directed;
		}

		public boolean isMultigraph() {
			return multigraph;
		}

		public void setMultigraph(boolean multigraph) {
			this.multigraph = multigraph;
		}

		public boolean isCompound() {
			return compound;
		}

		public void setCompound(boolean compound) {
			this.compound = compound;
		}

	}

}
