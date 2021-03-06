package at.jku.dke.pm.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import at.jku.dke.pm.domain.Case;
import at.jku.dke.pm.domain.Model;
import at.jku.dke.pm.domain.ProcessData;
import at.jku.dke.pm.services.CaseRepository;
import at.jku.dke.pm.services.ProcessRepository;
import at.jku.dke.pm.services.ProcessService;

@Controller
@RequestMapping(value = "/api/{processId}")
public class ProcessApiController {

	protected static final Logger logger = LoggerFactory.getLogger(ProcessApiController.class);

	@Autowired
	protected CaseRepository caseRepository;
	
	@Autowired
	@Deprecated
	protected ProcessRepository processRepository;

	@Autowired
	protected ProcessService processService;

	@RequestMapping(method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	public ResponseEntity<ProcessData> processInfo(@PathVariable("processId") String processId) {

		logger.debug("processId = {}", processId);
		
		ProcessData p = processService.getProcess(processId, null);
		
		return p != null ? ResponseEntity.ok(p) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	} 

	@Deprecated
	@RequestMapping(value = "/models", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	public ResponseEntity<List<Model>> listModels(@PathVariable("processId") String processId) {

		logger.debug("processId = {}", processId);
		
		List<Model> models = processRepository.findAllModels(processId);
		return models != null ? ResponseEntity.ok(models) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	} 

	@RequestMapping(value = "/model/{modelId}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	public ResponseEntity<Model> getModel(@PathVariable("processId") String processId, @PathVariable("modelId") String modelId) {

		logger.debug("processId = {} modelId = {}", processId, modelId);
		
		Model m = processRepository.findModelById(processId, modelId);
		
		return m != null ? ResponseEntity.ok(m) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	} 

	@RequestMapping(value = "/case/{caseId}", method = RequestMethod.GET, produces = { "application/json" } )
	public ResponseEntity<Case> getCase(@PathVariable("processId") String processId, @PathVariable("caseId") int caseId) {

		logger.debug("processId = {} modelId = {}", processId, caseId);
	
		Case c = caseRepository.findById(caseId);
		
		return c != null ? ResponseEntity.ok(c) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	} 

}
