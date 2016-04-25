package at.jku.dke.pm.extract;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.EventAttributes;
import at.jku.dke.pm.domain.Events;

public class BestellungExtractor implements EventExtractor {

	public final String SOURCE_ID = "BestellungExtractor";

	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	@Override
	public List<Event> extractEvents(Map<String, Object> log) {

		List<Event> events = new ArrayList<>();

		// Erstellt
		Event e = new Event();
		e.setSource(SOURCE_ID);

		e.setType(Events.BESTELLUNG_ERSTELLT);
		e.setEventTs(LocalDateTime.parse((String) log.get("ERSTELLTS"), formatter));

		e.addAttribute(EventAttributes.ID_BESTELLUNG, log.get("BESTELLNR"));
		e.addAttribute(EventAttributes.ID_KREDITOR, log.get("KREDNR"));
		e.addAttribute(EventAttributes.ID_USER, log.get("ERSTELLUSR"));

		events.add(e);

		// Freigegeben
		//TODO: optional falls kein FreigabeTS ?
		if (log.getOrDefault("FREIGABETS", null) != null) {
			e = new Event();
			e.setSource(SOURCE_ID);

			e.setType(Events.BESTELLUNG_FREIGEGEBEN);
			e.setEventTs(LocalDateTime.parse((String) log.get("FREIGABETS"), formatter));

			e.addAttribute(EventAttributes.ID_BESTELLUNG, log.get("BESTELLNR"));
			e.addAttribute(EventAttributes.ID_KREDITOR, log.get("KREDNR"));
			e.addAttribute(EventAttributes.ID_USER, log.get("FREIGABEUSR"));

			events.add(e);
		}

		return events;
	}

}
