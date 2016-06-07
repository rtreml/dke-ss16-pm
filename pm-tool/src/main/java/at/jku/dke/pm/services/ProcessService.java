package at.jku.dke.pm.services;

import java.util.List;
import java.util.Map;

import at.jku.dke.pm.domain.ProcessData;

public interface ProcessService {

	public List<ProcessData> getAvailableProcesses();
	
	public ProcessData getProcess(String processId, Map<String, String> filter);
}
