package camel.demo;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.processor.idempotent.FileIdempotentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
// import org.springframework.context.annotation.Configuration;


// @Configuration
@Component
public class FileChecking {
	
	private static final Logger logger = LoggerFactory.getLogger(FileChecking.class);

	@Bean
	RoutesBuilder myAnotherRouter() {
		return new RouteBuilder() {
			
			
			//Predicate predicate2 = PredicateBuilder.
			
			private @Value("${camel.setting.helloworld}") String welcomeString;
			
			@Override
			public void configure() throws Exception {
				
				boolean startupRoute = false;
				
				/**
				 * test ftp
				 */
				
				from("ftp://test@localhost?password=test&include=.*.pdf")
					.noAutoStartup()
					//.idempotentConsumer(header("CamelFileName"), FileIdempotentRepository.fileIdempotentRepository(new File("data", "repo.dat")))
					.log("ftp download ${file:name}")
					//.to("file:/C:/temp/camel/download")
					;
												
				/**
				 * test set header
				 */
								
                from("direct:start")
                	.log("start direct")
	                .split().body()
	                .log("${body}")
	                .setHeader("cheese", constant("camembert"))
	                .log("${body} ${headers}")
	                //.aggregate(constant("all"))
	                .to("mock:end");
				
                /**
                 * test timer
                 */
                
//				from("timer://timer1?period=10000")				
//					.log("timer: ${camelId} ${id} ${headers}");
//				
//				from("timer://timer2?period=100000")
//					.log("timer: ${body}")
//					.to("mock:end")
//					;				
					
                
                /**
                 * test ready file
                 */
				// move to staging folder ".staging" first and wait for ReadyFile as indicator and then move to ".completed" folder
				from("file:/C:/temp/camel/in/?include=.*.csv&delay=1000&preMove=.staging&move=.completed&doneFileName=ReadyFile")
					.noAutoStartup()
					.startupOrder(1)
					.to("file:/C:/camel/temp/out/csv");
				
				
				/**
				 * test copy file route
				 */
				final CountDownLatch latch = new CountDownLatch(1);
				from("file:/C:/temp/camel/extfolder/?include=.*.txt&delay=1000&noop=true&idempotentKey=${file:name}-${file:modified}&sendEmptyMessageWhenIdle=true")
				.autoStartup(false)
				.routeId("copyExtFolders")										
					.choice()
						.when(simple("${body} != null")) 
							.setHeader("CamelFileName",simple("${date:now:yyyyMMdd}/${file:name}"))
							.to("file:/C:/temp/camel/backup")
							.to("file:/C:/temp/camel/process")
							.log("copying files ${file:name}")
						.otherwise()
							.log("end????")		
							
							.process(exchange -> {
								Thread stop = null;
		                        if (stop == null) {
		                            stop = new Thread() {
		                                @Override
		                                public void run() {
		                                    try {
		                                        exchange.getContext().stopRoute("copyExtFolders");
		                                    } catch (Exception e) {
		                                        // ignore
		                                    } finally {
		                                        // signal we stopped the route
		                                        latch.countDown();
		                                    }
		                                }
		                            };
		                        }									
		                        
		                        stop.start();
							})							
				;
				
				
				from("direct:testProp")
					.routeId("testProp")
					.setProperty("enabled", constant(true))
					.process(exchange -> {
						
						logger.info("show properties: " + exchange.getProperty("enabled"));
					})
					.log("test properties {{logging.file}}")
					.log("test properties {{logging.file}}")
					.log("test properties ${exchange.properties[enabled]}")	
					.choice()
					.when(simple("${exchange.properties[enabled]} == true"))
						.log("it is enabled")
						.setHeader("message", constant("successful"))
						.to("direct:testProp2")						
					.otherwise()
						.log("it is disabled")	
						.setHeader("message", constant("failure"))
						.to("direct:testProp2")
					;
				
				/**
				 * test call bean
				 * 
				 * write string content to the file
				 * 
				 */
				from("direct:testProp2")
					.routeId("testProp2ID")
					.log("this is test prop 2")
					.log("${headers}")
					.bean(ExampleBean.class, "sayGoodBye")
					.log("${body}")
					.log("${routeId}")
					.bean(ExampleBean.class, "checkRoute(${routeId})")
					.setHeader("checkedRoute", method(ExampleBean.class, "checkRoute(${routeId})"))					
					.setProperty("checkedRoute", method(ExampleBean.class, "checkRoute(${routeId})"))					
					.log("${body}")
					.log("${headers}")
					.log("properties: ${exchange.properties[checkedRoute]}")
					.to("file:/C:/temp/camel/?fileName=trigger.txt")
					;
			
				
				/**
				 * test wait several files concept
				 */
				from("file:/C:/temp/camel/in/?include=.*.log&delay=1000")
					.to("file:/C:/temp/camel/bin")
					.process(
							exchange -> {
								String filename = (String)exchange.getIn().getHeader("CamelFileName");
								
								logger.info("file found: " + filename);
								
								if(filename.contains("a")) AppStatus.isAFileExists = true;
								if(filename.contains("b")) AppStatus.isBFileExists = true;
								if(filename.contains("c")) AppStatus.isCFileExists = true;
								if(filename.contains("d")) AppStatus.isDFileExists = true;
								
								logger.info("isAFileExists: " + AppStatus.isAFileExists);
								logger.info("isBFileExists: " + AppStatus.isBFileExists);
								logger.info("isCFileExists: " + AppStatus.isCFileExists);
								logger.info("isDFileExists: " + AppStatus.isDFileExists);
								
								if(AppStatus.isAFileExists == true
										&& AppStatus.isBFileExists == true
										&& AppStatus.isCFileExists == true
										&& AppStatus.isDFileExists == true
										)				{
									//sendBody("direct:startCheckSystemDate");
									exchange.getContext().createProducerTemplate().sendBody("direct:startCheckSystemDate","start");
									AppStatus.isAFileExists = false;
									AppStatus.isBFileExists = false;
									AppStatus.isCFileExists = false;
									AppStatus.isDFileExists = false;
								}
							}
					);		
				
				
				/**
				 * test check system date
				 * @TODO
				 * 
				 */
				from("direct:startCheckSystemDate")
					.log("all indicator files found");
				
				
				/**
				 * test send email
				 */
				from("file:/C:/temp/camel/in/?include=.*.ok&delay=1000")
					.choice()
						.when(simple("${file:name} ends with 'ok'"))
							.log("end with ok")
							.bean(ExampleBean.class,"sayHello(${file:name})")
							.setHeader("subject", constant("new incident reported"))
							.setHeader("from", constant("CAPOPER@CAPSIL.CHUBB.COM.HK"))
							.convertBodyTo(String.class)
							.process(exchange -> {
								String originalBody = (String)exchange.getIn().getBody();								
								String changedBody = originalBody + " " + " edit !";								
								exchange.getIn().setBody(changedBody);
							})
							.to("smtp://myID@localhost?password=&to=johnson.fu@chubb.com")
							//.to("stream:out")
						.otherwise()
							.log("no end with ok");
				
				
				/**
				 * test route and move files based on report id
				 */
				
				List<String> holdFiles = Arrays.asList(new String[] {"RN540PRT01", "RN540PRT02"});
				
				from("file:/C:/temp/camel/in/?include=.*.txt&delay=1000&delete=true")					
					.log("processing .txt file ${file:name}")
					.process(exchange -> {
						File file = exchange.getIn().getBody(File.class);
						logger.info(file.getName());
						
						String filename = file.getName();
						
						// check filename here... 
						// and apply header...
						
						//exchange.setProperty(name, value);
						
						exchange.getIn().setHeader("hold", Boolean.FALSE);
						
						for(String s : holdFiles) {
							if(filename.contains(s))
								exchange.getIn().setHeader("hold", Boolean.TRUE);
						}		
						
						//exchange.getOut().setHeader("test", "123");
						
						//exchange.getOut().setHeader(name, value);						
					})
					.log("after hold files checking ${headers}")
					.choice()
						.when(header("hold").isEqualTo(true))
							.to("file:/C:/temp/camel/hold")
						.otherwise()
							.to("file:/C:/temp/camel/temp")													
					;
				
				
				// Predicate: conditions and apply to choice
				//Predicate predicate = PredicateBuilder.and(simple("${file:name.ext} == 'txt'"), XPathBuilder.xpath("/author/country = 'us'"));
				Predicate predicate = PredicateBuilder.and(simple("${file:name.ext} == 'txt'"), simple("${file:name} contains 'PPQ_CMPL'"));
				
				from("file:/C:/temp/camel/in/?include=.*.txt&delay=1000")
				.routeId("monitorText")
					.noAutoStartup()
					//.startupOrder(2)
					.choice()
						.when(predicate)
							.process(
									new Processor() {
							            public void process(Exchange msg) {
							                File file = msg.getIn().getBody(File.class);
							                logger.info("Processing file: " + file);							                
							                String text = msg.getIn().getBody(String.class);							                
							                logger.info("Processing text: " + text);
							                
							                System.out.println(msg.getIn().getHeader("CamelFileName"));
							                System.out.println(msg.getIn().getBody());
							                System.out.println(msg.getIn().getBody().getClass());
							            }}
									)
							.to("file:/C:/temp/camel/out?fileName=${file:name}-$simple{date:now:yyyyMMddHHmmss}")
							.log("yeah ! startup = {{camel.setting.startup}}")
							.log(welcomeString)
							//.split(body().tokenize("\n")).streaming().unmarshal()
							.to("direct:trigger")
						.otherwise()
							.to("file:/C:/temp/camel/bin");
				
				
				from("direct:trigger")
					.process(
							new Processor() {
								@Override
								public void process(Exchange msg) throws Exception {
					                String text = msg.getIn().getBody(String.class);					                
					                logger.info("another route processing: " + text);									
								}
								
							}
							);
				
				
				from("direct:hello")
					.log("this is triggered from producer template: ${body}");
				
				
			}
		};
	}
}
