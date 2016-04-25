package at.jku.dke.pm.extract;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.EventAttributes;
import at.jku.dke.pm.domain.Events;

public class ZahlungExtractor implements EventExtractor {

	public final String SOURCE_ID = "ZahlungExtractor";

	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	@Override
	public List<Event> extractEvents(Map<String, Object> log) {

		// Erstellt
		Event e = new Event();
		e.setSource(SOURCE_ID);

		e.setType(Events.ZAHLUNG_DURCHGEFUEHRT);
		e.setEventTs(LocalDateTime.parse((String) log.get("ZAHLTS"), formatter));

		e.addAttribute(EventAttributes.ID_ZAHLUNG, log.get("ID"));
		//TODO: skip falls null ...
		e.addAttribute(EventAttributes.ID_RECHNUNG, log.get("RECHNR"));
		e.addAttribute(EventAttributes.ID_USER, log.get("ZAHLUSR"));
		e.addAttribute(EventAttributes.ID_KREDITOR, log.get("KREDNR"));

		return Arrays.asList(e);
	}

}
