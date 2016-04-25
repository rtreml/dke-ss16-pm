package at.jku.dke.pm.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Case {
	
	protected Integer id = null;
	protected String processId;
	protected String name;

	protected int eventId;
	protected Map<String, Object> identifier = new HashMap<>();

	protected List<Event> events;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public Map<String, Object> getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Map<String, Object> identifier) {
		this.identifier = identifier;
	}
	
	public void addIdentifier(String key, Object value) {
		this.identifier.put(key, value);
	}

	public void addIdentifiers(Map<String, Object> identifiers) {
		this.identifier.putAll(identifiers);
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
