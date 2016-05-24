package at.jku.dke.pm.domain;

import java.time.Duration;
import java.time.LocalDateTime;
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

	protected LocalDateTime startTs;
	protected LocalDateTime endTs;
	protected Duration duration;

	protected String footprint;

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

	public LocalDateTime getStartTs() {
		return startTs;
	}

	public void setStartTs(LocalDateTime startTs) {
		this.startTs = startTs;
	}

	public LocalDateTime getEndTs() {
		return endTs;
	}

	public void setEndTs(LocalDateTime endTs) {
		this.endTs = endTs;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public String getFootprint() {
		return footprint;
	}

	public void setFootprint(String footprint) {
		this.footprint = footprint;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
