package at.jku.dke.pm.extract;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.EventAttributes;
import at.jku.dke.pm.domain.Events;

//TODO: auf bekannte Felder einschränken
public class HistorieExtractor implements EventExtractor {

	public static final String SOURCE_ID = "HistorieExtractor";

	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	@Override
	public List<Event> extractEvents(Map<String, Object> log) {

		Event e = null;

		String tabelle = (String) log.get("TABELLE");
		String feld = (String) log.get("FELD");

		// TODO: Felder überprüfen
		switch (tabelle) {
		case "Bestellposition": {
			e = new Event();

			e.setSource(SOURCE_ID);
			if ("Menge".equalsIgnoreCase(feld)) {
				e.setType(Events.BESTELLMENGE_GEAENDERT);
			} else if ("Preis".equalsIgnoreCase(feld)) {
				e.setType(Events.PREIS_GEAENDERT);
			} else if ("StornoKZ".equalsIgnoreCase(feld)) {
				e.setType(Events.BESTELLPOSITION_STORNIERT);
			}
			e.setEventTs(LocalDateTime.parse((String) log.get("AENDERTS"), formatter));

			e.addAttribute(EventAttributes.ID_USER, log.get("AENDERUSR"));
			String id = String.valueOf(log.get("ID")); // <Pos><Bestellung:6>
			e.addAttribute(EventAttributes.ID_BESTELLUNG, StringUtils.right(id, 6));
			e.addAttribute(EventAttributes.ID_BESTELL_POS, StringUtils.substring(id, 0, -6));

			break;
		}
		case "Bestellung": {
			e = new Event();

			e.setSource(SOURCE_ID);
			e.setType(Events.BESTELLUNG_STORNIERT);
			e.setEventTs(LocalDateTime.parse((String) log.get("AENDERTS"), formatter));

			e.addAttribute(EventAttributes.ID_USER, log.get("AENDERUSR"));
			e.addAttribute(EventAttributes.ID_BESTELLUNG, log.get("ID"));

			break;
		}
		case "Kreditor": {
			e = new Event();

			e.setSource(SOURCE_ID);
			e.setType("X".equalsIgnoreCase((String) log.get("WERT_NEU")) ? Events.KREDITOR_GESPERRT
					: Events.KREDITOR_ENTSPERRT);
			// e.setEventId(Events.KREDITOR_GESPERRT);
			e.setEventTs(LocalDateTime.parse((String) log.get("AENDERTS"), formatter));

			e.addAttribute(EventAttributes.ID_USER, log.get("AENDERUSR"));
			e.addAttribute(EventAttributes.ID_KREDITOR, log.get("ID"));

			break;
		}
		}

		return e != null ? Arrays.asList(e) : Collections.emptyList();
	}

}
