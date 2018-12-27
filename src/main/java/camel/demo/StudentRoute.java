package camel.demo;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

// import org.springframework.context.annotation.Configuration;

// @Configuration
@Component
public class StudentRoute extends RouteBuilder {
	
//	@Bean
//	ServletRegistrationBean servletRegistrationBean() {
//		final ServletRegistrationBean servlet = new ServletRegistrationBean(
//				//new CamelHttpTransportServlet(), "/javaoutofbounds/*");
//				new CamelHttpTransportServlet(), "/camel/*");
//			servlet.setName("CamelServlet");
//		return servlet;
//	}

	@Override
	public void configure() throws Exception {
		
		System.out.println("trigger");

		restConfiguration().component("servlet").bindingMode(RestBindingMode.json);

		rest("/student")
			.produces("application/json")
				.get("/hello/{name}")
					.route().transform()
					.simple("Hello ${header.name}, Welcome to JavaOutOfBounds.com").endRest();
		rest("/student")
			.produces("application/json")
				.get("/records/{name}")								
				.to("direct:records");

		from("direct:records").tracing().log("process").process(new Processor() {

			final AtomicLong counter = new AtomicLong();

			@Override
			public void process(Exchange exchange) throws Exception {

				final String name = exchange.getIn().getHeader("name", String.class);
				//exchange.getIn().setBody(new Student(counter.incrementAndGet(), name, "Camel + SpringBoot"));
				exchange.getIn().setBody(name + ":" + counter.incrementAndGet());
			}
		});

	}
}
