package at.jku.dke.pm.services;

import java.util.List;

import at.jku.dke.pm.domain.Event;

public interface EventRepository {
	public List<Event> saveAll(List<Event> events);
}
