package at.jku.dke.pm.services;

import java.util.List;

import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.domain.ProcessData;

public interface ProcessRepository {
	
	public List<ProcessData> findAll();

	public ProcessData findById(String processId);

	public Model findModelById(String processId, String id);

	public List<Model> findAllModels(String processId);

}
