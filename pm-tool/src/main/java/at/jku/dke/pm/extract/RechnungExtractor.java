package at.jku.dke.pm.extract;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.EventAttributes;
import at.jku.dke.pm.domain.Events;

public class RechnungExtractor implements EventExtractor {

	public final String SOURCE_ID = "RechnungExtractor";

	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	protected final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	@Override
	public List<Event> extractEvents(Map<String, Object> log) {

		List<Event> events = new ArrayList<>();

		// Eingegangen
		Event e = new Event();
		e.setSource(SOURCE_ID);

		e.setType(Events.RECHNUNG_EINGEGANGEN);
		e.setEventTs(LocalDateTime.parse((String) log.get("EINGANSDAT"), formatter));

		e.addAttribute(EventAttributes.ID_RECHNUNG, log.get("RECHNR"));
		e.addAttribute(EventAttributes.ID_BESTELL_POS, log.get("POSNR"));
		e.addAttribute(EventAttributes.ID_BESTELLUNG, log.get("BESTELLNR"));
		e.addAttribute(EventAttributes.ID_KREDITOR, log.get("KREDNR"));

		events.add(e);

		// Gestellt
		e = new Event();
		e.setSource(SOURCE_ID);

		e.setType(Events.RECHNUNG_GESTELLT);
		e.setEventTs(LocalDate.parse((String) log.get("RECHNUNGSDATUM"), dateFormatter).atStartOfDay());

		e.addAttribute(EventAttributes.ID_RECHNUNG, log.get("RECHNR"));
		e.addAttribute(EventAttributes.ID_BESTELL_POS, log.get("POSNR"));
		e.addAttribute(EventAttributes.ID_BESTELLUNG, log.get("BESTELLNR"));
		e.addAttribute(EventAttributes.ID_KREDITOR, log.get("KREDNR"));

		events.add(e);

		return events;
	}

}
