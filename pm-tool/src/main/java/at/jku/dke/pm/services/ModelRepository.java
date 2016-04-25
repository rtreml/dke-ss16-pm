package at.jku.dke.pm.services;

import java.util.List;

import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.domain.ProcessInfo;

public interface ModelRepository {

	public ProcessInfo findProcessById(String processId);

	public Model findById(String processId, String id);

	public List<Model> findAll(String processId);

}
