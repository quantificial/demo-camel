package camel.demo;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
// import org.springframework.context.annotation.Configuration;


// @Configuration
@Component
public class MyCamelConfiguration {

	@Bean
	RoutesBuilder myRouter() {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				from("file:/C:/temp/in/?include=.*.wav")
					.to("file:/C:/temp/out/wave/");
			}
		};
	}
}
