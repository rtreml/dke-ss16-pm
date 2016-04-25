package at.jku.dke.pm.web.model.graphlib;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @formatter:off { "v": "a", "value": { "label": "node a" } },
 * @formatter:on
 * 
 * @author Tremlro
 * 
 */
public class Node {

	// id
	protected String v;

	// values
	protected Map<String, String> value = new HashMap<String, String>();

	public Node(String v) {
		this.v = v;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public Map<String, String> getValue() {
		return value;
	}

	public void setValue(Map<String, String> value) {
		this.value = value;
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
		return new EqualsBuilder().append(v, rhs.v).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(v).toHashCode();
	}
	//fluent values API
	/*
	 * var NODE_DEFAULT_ATTRS = {
  paddingLeft: 10,
  paddingRight: 10,
  paddingTop: 10,
  paddingBottom: 10,
  rx: 0,
  ry: 0,
  shape: "rect"
};
	 */
	public static final String CSS_CLASS = "class";
	public static final String LABEL = "label";
	public static final String SHAPE = "shape"; //rect, circle, ellipse
	//
	public static final String STYLE = "style";
	public static final String LABEL_STYLE = "labelStyle";
	public static final String LABEL_TYPE = "labelType"; //html
	
	public static Node create(String v) {
		return new Node(v);
	}
	
	public Node cssClass(String cssClass) {
		this.value.put(CSS_CLASS, cssClass);
		return this;
	}

	public Node label(String label) {
		this.value.put(LABEL, label);
		return this;
	}

	public Node shape(String shape) {
		this.value.put(SHAPE, shape);
		return this;
	}

	public Node labelType(String labelType) {
		this.value.put(LABEL_TYPE, labelType);
		return this;
	}

	public Node addValue(String key, String val) {
		this.value.put(key, val);
		return this;
	}

	public Node addValues(Map<String, String> values) {
		this.value.putAll(values);
		return this;
	}
	

}
