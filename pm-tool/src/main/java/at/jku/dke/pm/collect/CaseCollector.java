package at.jku.dke.pm.collect;

import java.util.List;

import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Event;

public interface CaseCollector {

	public List<Case> identifyCases();

	public List<Event> collectCaseEvents(int caseId);

	public List<Event> collectCaseEvents(Case c);
}
