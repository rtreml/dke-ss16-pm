package at.jku.dke.pm.extract;

import java.util.List;
import java.util.Map;

import at.jku.dke.pm.domain.Event;

public interface EventExtractor {

	public List<Event> extractEvents(Map<String, Object> log);
}
