package at.jku.dke.pm.analyze;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;
import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.domain.ProcessNet;

public class BasicModelAnalyzer implements ModelAnalyzer {

	protected static final Logger logger = LoggerFactory.getLogger(BasicModelAnalyzer.class);

	private static final String META_LABEL = "label";
	private static final String META_SHAPE = "shape";
	private static final String META_CSS = "css";

	private static final String META_COUNT = "count";
	private static final String META_PASS_COUNT = "countPass";
	private static final String META_DURATION = "duration";

	@SuppressWarnings("unchecked")
	@Override
	public void analyze(Model model, List<Case> cases) {
		// Nodes/Edges für den Graph
		Map<String, ProcessNet.Node> nodes = new HashMap<>();
		Map<String, ProcessNet.Edge> edges = new HashMap<>();

		model.setNoCases(cases.size());

		// Nodes vorbereiten
		nodes.put("start", ProcessNet.Node.create("start").addData(META_LABEL, "Start").addData(META_SHAPE, "circle")
				.addData(META_CSS, "pm_start"));
		nodes.put("end", ProcessNet.Node.create("end").addData(META_LABEL, "End").addData(META_SHAPE, "circle")
				.addData(META_CSS, "pm_end"));

		// Cases analysieren
		List<Duration> durations = new ArrayList<>();
		boolean firstPass = true;
		for (Case c : cases) {
			// 1) sort events
			c.getEvents().sort((e1, e2) -> e1.getEventTs().compareTo(e2.getEventTs()));

			// 2) Total Duration
			Duration d = Duration.between(c.getEvents().get(0).getEventTs(), c.getEvents()
					.get(c.getEvents().size() - 1).getEventTs());
			logger.debug("{} Duration {}", c.getId(), d);
			durations.add(d);

			// 3)graph aufbauen
			Event last = null;
			for (Event e : c.getEvents()) {
				ProcessNet.Node node = nodes.computeIfAbsent(e.getType(),
						k -> ProcessNet.Node.create(k).addData(META_LABEL, k));
				((AtomicInteger) node.getData().computeIfAbsent(META_COUNT, k -> new AtomicInteger(0)))
						.incrementAndGet();
				if (firstPass) {
					((AtomicInteger) node.getData().computeIfAbsent(META_PASS_COUNT, k -> new AtomicInteger(0)))
							.incrementAndGet();
				}
				// ((AtomicInteger) nodes.computeIfAbsent(e.getType(), k ->
				// ProcessNet.Node.create(k).addData(META_LABEL, k)).getData()
				// .computeIfAbsent(META_COUNT, k -> new AtomicInteger(0))).incrementAndGet();

				if (last != null) {
					String l = last.getType();
					ProcessNet.Edge edge = edges.computeIfAbsent(String.format("%s-%s", last.getType(), e.getType()),
							k -> ProcessNet.Edge.create(l, e.getType()).addData(META_LABEL, k));
					((AtomicInteger) edge.getData().computeIfAbsent(META_COUNT, k -> new AtomicInteger(0)))
							.incrementAndGet();
					((List<Duration>) edge.getData().computeIfAbsent(META_DURATION, k -> new ArrayList<Duration>()))
							.add(Duration.between(last.getEventTs(), e.getEventTs()));
					if (firstPass) {
						((AtomicInteger) edge.getData().computeIfAbsent(META_PASS_COUNT, k -> new AtomicInteger(0)))
								.incrementAndGet();
					}
					// edge.setValue(value);
				} else {
					ProcessNet.Edge edge = edges.computeIfAbsent(String.format("start-%s", e.getType()),
							k -> ProcessNet.Edge.create("start", e.getType()).addData(META_LABEL, k));
					((AtomicInteger) edge.getData().computeIfAbsent(META_COUNT, k -> new AtomicInteger(0)))
							.incrementAndGet();

				}

				last = e;
			}
			String l = last.getType();
			((AtomicInteger) edges
					.computeIfAbsent(String.format("%s-end", last.getType()),
							k -> ProcessNet.Edge.create(l, "end").addData(META_LABEL, k)).getData()
					.computeIfAbsent(META_COUNT, k -> new AtomicInteger(0))).incrementAndGet();

			firstPass = false;
		}

		// durchschnittszeit
		edges.values()
				.stream()
				.filter(e -> e.getData().containsKey(META_DURATION))
				.forEach(
						e -> {
							logger.debug("e: X({}) {} {}", e, e.getData(META_LABEL), e.getData());
							e.getData().put(
									META_DURATION,
									Duration.ofSeconds((long) ((List<Duration>) e.getData(META_DURATION)).stream()
											.mapToLong(Duration::getSeconds).average().orElse(0)));
						});

		// N/E logging
		logger.debug("nodes: {}", nodes);
		nodes.entrySet().forEach(e -> logger.debug("     N: {}: {}", e.getKey(), e.getValue().getData()));
		logger.debug("edges: {}", edges);
		edges.entrySet().forEach(e -> logger.debug("     E: {}: {}", e.getKey(), e.getValue().getData()));

		// graph initialisieren
		ProcessNet net = new ProcessNet(model.getFootprint());

		nodes.values().forEach(n -> net.addNode(n));
		edges.values().forEach(e -> net.addEdge(e));

		model.setProcessNet(net);

		// durations
		model.setMinDuration(durations.stream().min(Comparator.comparing(Duration::getSeconds)).get());
		model.setMaxDuration(durations.stream().max(Comparator.comparing(Duration::getSeconds)).get());
		model.setAvgDuration(Duration.ofSeconds((long) durations.stream().mapToLong(Duration::getSeconds).average()
				.orElse(0)));

		logger.debug("Durations min:{} max:{} avg:{}", model.getMinDuration(), model.getMaxDuration(),
				model.getAvgDuration());
	}

}
