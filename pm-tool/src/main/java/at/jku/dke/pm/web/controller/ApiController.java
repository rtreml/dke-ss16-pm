package at.jku.dke.pm.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import at.jku.dke.pm.domain.ProcessData;
import at.jku.dke.pm.services.ProcessRepository;

@Controller
@RequestMapping(value = "/api")
public class ApiController {

	protected static final Logger logger = LoggerFactory.getLogger(ApiController.class);

	@Autowired
	protected ProcessRepository processRepository;
	
	@RequestMapping(value = "/process", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	public List<ProcessData> processList() {

		return processRepository.findAll();
		
	} 

}
