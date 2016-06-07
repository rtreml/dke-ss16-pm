package at.jku.dke.pm.analyze;

import java.util.List;

import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Model;

public interface ModelAnalyzer {
	
	public void analyze(Model model, List<Case> cases);

}
