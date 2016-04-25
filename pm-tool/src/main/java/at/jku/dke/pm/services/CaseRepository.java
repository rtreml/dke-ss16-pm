package at.jku.dke.pm.services;

import at.jku.dke.pm.domain.Case;

public interface CaseRepository {
	public Case save(Case c);

	public Case findById(int caseId);
}
