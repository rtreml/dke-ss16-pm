package at.jku.dke.pm.collect.footprint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.dke.pm.domain.Event;

public class SumUpFootprintGenerator implements FootprintGenerator {

	@Override
	public String generate(List<Event> events) {

		List<String> ids = events.stream()
				.sorted(Comparator.comparing(e -> e.getEventTs(), (t1, t2) -> t1.compareTo(t2))).map(e -> e.getType())
				.collect(Collectors.toList());

		List<String> ret = new ArrayList<>();
		ret.add(String.format("%02d", events.size()));
		
		int cnt = 1;
		String last = "";
		for (String token : ids) {
			if (last.equals(token)) {
				cnt++;
			} else {
				if (cnt > 1) {
					ret.add(String.format("%d*%s", cnt, last));
					cnt =1;
				} else {
					ret.add(String.format("%s", last));
					
				}

			}
			last = token;
		}


		if (cnt > 1) {
			ret.add(String.format("%d*%s", cnt, last));
		} else {
			ret.add(String.format("%s", last));
			
		}

		
		return ret.stream().filter(e -> !e.isEmpty()).collect(Collectors.joining("|"));
	}

}
