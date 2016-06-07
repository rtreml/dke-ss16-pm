package at.jku.dke.pm.collect.footprint;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.dke.pm.domain.Event;

public class SimpleFootprintGenerator implements FootprintGenerator {

	@Override
	public String generate(List<Event> events) {

		return String.format(
				"%02d|%s",
				events.size(),
				events.stream().sorted(Comparator.comparing(e -> e.getEventTs(), (t1, t2) -> t1.compareTo(t2)))
						.map(e -> e.getType()).collect(Collectors.joining("|")));
	}

}
