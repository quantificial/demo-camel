package camel.demo;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ApacheCamelDemoApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(ApacheCamelDemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ApacheCamelDemoApplication.class, args);

		try {											
			
			// Sleep for 5 minutes
			TimeUnit.MINUTES.sleep(5);
									
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Autowired
	private FileChecking fileChecking;
	
	@Autowired
	private CamelContext camelContext;
	

	@Autowired
	ProducerTemplate producerTemplate;
	

	
    //access command line arguments
    @Override
    public void run(String... args) throws Exception {
	
        //do something
    	
    	logger.info("start command line...");
    	
    	//CamelContext context = new DefaultCamelContext();    	
    	//camelContext.startRoute("monitorText");
    	
    	logger.info("monitorText status: " + camelContext.getRouteStatus("monitorText"));
    	
//    	camelContext.stopRoute("monitorText");    	
//    	logger.info("monitorText status: " + camelContext.getRouteStatus("monitorText"));
    	
    	producerTemplate.sendBody("direct:hello","calling from command prompt");
    	
    	final List<String> LIST = Arrays.asList(new String[] {"one", "two", "three"});
    	producerTemplate.sendBody("direct:start", LIST);
    	
    	camelContext.startRoute("copyExtFolders");
    	  
    	
    	producerTemplate.sendBody("direct:testProp", "testProp");
    	
    	
    	logger.info("end  command line...");
    	
    	//long fileCount = FtpService.getFtpFileCount();
    	//logger.info("ftp file count: " + fileCount);
    	
    	List<Route> listRoute = camelContext.getRoutes();
    	
    	for(Route r : listRoute) {
    		logger.info(r.getId());
    		
    	}
    	
		
    }	
}
