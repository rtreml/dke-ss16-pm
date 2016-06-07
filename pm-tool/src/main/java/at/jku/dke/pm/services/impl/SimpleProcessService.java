package at.jku.dke.pm.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.jku.dke.pm.domain.ProcessData;
import at.jku.dke.pm.services.ProcessRepository;
import at.jku.dke.pm.services.ProcessService;

@Service
public class SimpleProcessService implements ProcessService {

	@Autowired
	protected ProcessRepository processRepository;

	@Override
	public List<ProcessData> getAvailableProcesses() {
		return processRepository.findAll();
	}

	@Override
	public ProcessData getProcess(String processId, Map<String, String> filter) {
		return processRepository.findById(processId);
	}

}
