package camel.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExampleBean {
	private static final Logger logger = LoggerFactory.getLogger(ExampleBean.class);
	
    public String sayHello(String name) {
    	
    	logger.info("inside examplebean..." + name);
    	
        return "Hello from Example Bean " + name + "!";
    }
    
    public String sayGoodBye(String name) {
    	
    	logger.info("inside examplebean..." + name);
    	
        return "Good Bye from Example Bean " + name + "!";
    }    
    
    
    public String checkRoute(String route) {
    	
    	logger.info("check Route: " + route);
    	
    	return "CHECKED:"+route;
    }
}
