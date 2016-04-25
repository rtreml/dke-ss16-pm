package at.jku.dke.pm.domain;

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

	private Events() {
	}

}
