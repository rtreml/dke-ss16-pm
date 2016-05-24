package at.jku.dke.pm.services;

import java.util.List;

import at.jku.dke.pm.domain.Case;

public interface CaseRepository {
	public Case save(Case c);

	public Case findById(int caseId);
	
	public List<Case> findByFootprint(String processId, String footprint);
}
