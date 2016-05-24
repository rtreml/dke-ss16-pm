package at.jku.dke.pm.web.model.graphlib;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @formatter:off { "v": "a", "w": "b", "value": { "label": "edge a->b" } }
 * @formatter:on
 * 
 * @author Tremlro
 * 
 */
public class Edge {

	// source
	protected String v;

	//target
	protected String w;

	// values
	protected Map<String, String> value = new HashMap<>();

	//metainformation
	protected Map<String, Object> meta = new HashMap<>();
	
	@JsonCreator
	public Edge(@JsonProperty("v") String v, @JsonProperty("w") String w) {
		this.v = v;
		this.w = w;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public String getW() {
		return w;
	}

	public void setW(String w) {
		this.w = w;
	}

	public Map<String, String> getValue() {
		return value;
	}

	public void setValue(Map<String, String> value) {
		this.value = value;
	}

	public String getValue(String key) {
		return value.get(key);
	}
	
	public Map<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public Object getMeta(String key) {
		return meta.get(key);
	}


	//fluent values API
	//default
	public static final String ARROWHEAD = "arrowhead"; //normal, vee
	public static final String LINE_INTERPOLATE = "lineInterpolate"; //linear,
	//
	public static final String CSS_CLASS = "class";
	public static final String LABEL = "label";
	//
	public static final String STYLE = "style";
	public static final String LABEL_STYLE = "labelStyle";
	public static final String ARROWHEAD_STYLE = "arrowheadStyle";

	public static Edge create(String v, String w) {
		return new Edge(v, w);
	}

	public Edge arrowhead(String arrowhead) {
		this.value.put(ARROWHEAD, arrowhead);
		return this;
	}

	public Edge cssClass(String cssClass) {
		this.value.put(CSS_CLASS, cssClass);
		return this;
	}

	public Edge label(String label) {
		this.value.put(LABEL, label);
		return this;
	}

	public Edge lineInterpolate(String lineInterpolate) {
		this.value.put(LINE_INTERPOLATE, lineInterpolate);
		return this;
	}

	public Edge addValue(String key, String val) {
		this.value.put(key, val);
		return this;
	}

	public Edge addValues(Map<String, String> values) {
		this.value.putAll(values);
		return this;
	}

	//meta information
	public Edge addMeta(String key, Object val) {
		this.meta.put(key, val);
		return this;
	}

	public Edge addMetas(Map<String, Object> metas) {
		this.meta.putAll(metas);
		return this;
	}

}
