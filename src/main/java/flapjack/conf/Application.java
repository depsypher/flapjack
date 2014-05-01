package flapjack.conf;

import javax.servlet.ServletContextListener;

import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.google.inject.servlet.GuiceFilter;

/**
 * Main entry point into the application.
 * Runs an embedded tomcat instance as currently configured.
 *
 * @author ray
 */
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(Application.class)
				.showBanner(false)
				.run(args);
	}

	@Bean
	public FilterRegistrationBean guiceFilter() {
		FilterRegistrationBean filter = new FilterRegistrationBean(new GuiceFilter());
		filter.setOrder(1);
		return filter;
	}

	@Bean
	public FilterRegistrationBean jerseyFilter() {
		FilterRegistrationBean filter = new FilterRegistrationBean(new ServletContainer());
		filter.addInitParameter("jersey.config.server.provider.packages", "flapjack.controller");
		filter.addInitParameter("jersey.config.servlet.filter.forwardOn404", "true");
		filter.addInitParameter("jersey.config.server.provider.classnames", "flapjack.conf.CambridgeMvcFeature");
		filter.addInitParameter("javax.ws.rs.Application", "flapjack.conf.JerseyConfig");
		filter.setOrder(2);
		return filter;
	}

	@Bean
	public ServletListenerRegistrationBean<ServletContextListener> guiceServlet() {
		return new ServletListenerRegistrationBean<ServletContextListener>(new GuiceConfig());
	}
}
