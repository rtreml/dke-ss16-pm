package at.jku.dke.pm.domain;

import java.util.HashMap;
import java.util.Map;

public final class Events {

	//Bestellposition	
	public static final String BESTELLPOSITION_ERSTELLT = "PE";
	public static final String BESTELLPOSITION_STORNIERT = "PS";
	public static final String BESTELLMENGE_GEAENDERT = "PCM"; //HISTORIE
	public static final String PREIS_GEAENDERT = "PCP"; //HISTORIE

	//Bestellung
	public static final String BESTELLUNG_ERSTELLT = "BE";
	public static final String BESTELLUNG_FREIGEGEBEN = "BF";
	public static final String BESTELLUNG_STORNIERT = "BS";

	//Kreditor
	public static final String KREDITOR_ERSTELLT = "KA";
	public static final String KREDITOR_GESPERRT = "KG"; //HISTORIE
	public static final String KREDITOR_ENTSPERRT = "KE"; //HISTORIE
	
	//Rechnung
	public static final String RECHNUNG_EINGEGANGEN = "RE";
	public static final String RECHNUNG_GESTELLT = "RG";

	//Wareneingang
	public static final String WARE_EINGEGANGEN = "WE";

	//Zahlung
	public static final String ZAHLUNG_DURCHGEFUEHRT = "ZD";


	/**
	 * Masterdata: Events
	 */
	@SuppressWarnings("serial")
	public static final Map<String, String> MD_EVENTS = new HashMap<String, String>() {
		{
			put(BESTELLPOSITION_ERSTELLT, "Bestellposition erstellt");
			put(BESTELLPOSITION_STORNIERT, "Bestellposition storniert");
			put(BESTELLMENGE_GEAENDERT, "Bestellmenge geändert");
			put(PREIS_GEAENDERT, "Preis geändert");
			put(BESTELLUNG_ERSTELLT, "Bestellung erstellt");
			put(BESTELLUNG_FREIGEGEBEN, "Bestellung freigegeben");
			put(BESTELLUNG_STORNIERT, "Bestellung Storniert");
			put(KREDITOR_ERSTELLT, "Kreditor erstellt");
			put(KREDITOR_GESPERRT, "Kreditor gesperrt");
			put(KREDITOR_ENTSPERRT, "Kreditor entsperrt");
			put(RECHNUNG_EINGEGANGEN, "Rechnung eingegangen");
			put(RECHNUNG_GESTELLT, "Rechnung gestellt");
			put(WARE_EINGEGANGEN, "Ware eingegangen");
			put(ZAHLUNG_DURCHGEFUEHRT, "Zahlung durchgeführt");
		}
	};

	private Events() {
	}

}
