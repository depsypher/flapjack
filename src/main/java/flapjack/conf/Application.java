package flapjack.conf;

import javax.servlet.ServletContextListener;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.google.inject.servlet.GuiceFilter;

@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(Application.class)
				.showBanner(false)
				.run(args);
	}

	@Bean
	public ServletListenerRegistrationBean<ServletContextListener> guiceServlet() {
		return new ServletListenerRegistrationBean<ServletContextListener>(new GuiceConfig());
	}

	@Bean
	public FilterRegistrationBean guiceFilter() {
		return new FilterRegistrationBean(new GuiceFilter());
	}
}
