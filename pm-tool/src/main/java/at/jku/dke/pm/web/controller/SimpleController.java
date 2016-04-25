package at.jku.dke.pm.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SimpleController {

	protected static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

	@RequestMapping("/")
	public String showIndex(Model model) {

		logger.debug("index !!");
		return "PmTool";
	} 

}
