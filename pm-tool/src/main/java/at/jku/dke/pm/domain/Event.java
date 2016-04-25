package at.jku.dke.pm.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Event {
	protected Integer id = null;

	protected String source;
	protected String legacyId;

	protected String type;
	
	protected LocalDateTime eventTs;

	protected Map<String, Object> attributes = new HashMap<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLegacyId() {
		return legacyId;
	}

	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDateTime getEventTs() {
		return eventTs;
	}

	public void setEventTs(LocalDateTime eventTs) {
		this.eventTs = eventTs;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", source=" + source + ", type=" + type
				+ ", eventTs=" + eventTs + ", attributes=" + attributes + "]";
	}
	
	
}
