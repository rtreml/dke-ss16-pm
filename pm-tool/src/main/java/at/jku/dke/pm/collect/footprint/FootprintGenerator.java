package at.jku.dke.pm.collect.footprint;

import java.util.List;

import at.jku.dke.pm.domain.Event;

public interface FootprintGenerator {

	public String generate(List<Event> events);
	
}
