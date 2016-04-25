package at.jku.dke.pm.extract;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.EventAttributes;
import at.jku.dke.pm.domain.Events;

public class KreditorExtractor implements EventExtractor {
	
	public final String SOURCE_ID = "KreditorExtractor"; 
	
	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	@Override
	public List<Event> extractEvents(Map<String, Object> log) {

		Event e = new Event();
		e.setSource(SOURCE_ID);
		
		e.setType(Events.KREDITOR_ERSTELLT);
		e.setEventTs(LocalDateTime.parse((String)log.get("ERSTELLTS"), formatter));
		
		e.addAttribute(EventAttributes.ID_KREDITOR, log.get("KREDNR"));
		e.addAttribute(EventAttributes.ID_USER, log.get("ERSTELLUSR"));
		
		return Arrays.asList(e);
	}

}
