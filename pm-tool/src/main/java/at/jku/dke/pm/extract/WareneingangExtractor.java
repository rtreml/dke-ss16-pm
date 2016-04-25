package at.jku.dke.pm.extract;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.EventAttributes;
import at.jku.dke.pm.domain.Events;

public class WareneingangExtractor implements EventExtractor {

	public final String SOURCE_ID = "WareneingangExtractor";

	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	@Override
	public List<Event> extractEvents(Map<String, Object> log) {

		// Erstellt
		Event e = new Event();
		e.setSource(SOURCE_ID);

		e.setType(Events.WARE_EINGEGANGEN);
		e.setEventTs(LocalDateTime.parse((String) log.get("EINGANGSTS"), formatter));

		e.addAttribute(EventAttributes.ID_WARENEINGANG, log.get("ID"));
		e.addAttribute(EventAttributes.ID_BESTELL_POS, log.get("POSNR"));
		e.addAttribute(EventAttributes.ID_BESTELLUNG, log.get("BESTELLNR"));
		e.addAttribute(EventAttributes.ID_USER, log.get("EINGANGSUSR"));
		e.addAttribute(EventAttributes.ID_KREDITOR, log.get("KREDNR"));

		return Arrays.asList(e);
	}

}
