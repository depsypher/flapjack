package flapjack.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.guice.InstrumentationModule;
import com.yammer.metrics.reporting.HealthCheckServlet;
import com.yammer.metrics.reporting.MetricsServlet;
import com.yammer.metrics.reporting.PingServlet;
import com.yammer.metrics.reporting.ThreadDumpServlet;
import com.yammer.metrics.util.DeadlockHealthCheck;

import flapjack.controller.AccountController;
import flapjack.controller.AuthController;
import flapjack.controller.HomeController;
import flapjack.manager.PersonManager;
import flapjack.manager.jpa.PersonManagerJpa;

/**
 * Configuration for guice injection
 *
 * @author Ray Vanderborght
 */
public class GuiceConfig extends GuiceServletContextListener {

	private static final Logger log = LoggerFactory.getLogger(GuiceConfig.class);

	private static Injector injector;
	private static List<Module> modules = new ArrayList<>();

	/**
	 * Logs the time required to initialize Guice
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		log.info("Creating Guice injector...");
		Stopwatch stopwatch = new Stopwatch();

		stopwatch.start();
		modules.add(new AppModule());
		modules.add(new JpaPersistModule("flapjackPU"));
		modules.add(new InstrumentationModule());
		modules.add(new MetricsModule());

		injector = Guice.createInjector(modules);

		super.contextInitialized(servletContextEvent);
		stopwatch.stop();

		log.info("Guice initialization took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

	@Override
	protected Injector getInjector() {
		injector.getInstance(ApplicationInitializer.class);

		return injector;
	}

	public static Injector getInjectorInstance() {
		return injector;
	}

	public static Module[] getAllModulesAsArray() {
		return (Module[]) modules.toArray(new Module[modules.size()]);
	}

	public static class ApplicationInitializer {
		@Inject
		ApplicationInitializer(PersistService service) {
			service.start();
		}
	}

	static class AppModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(PersonManager.class).to(PersonManagerJpa.class).in(Singleton.class);
			bind(AccountController.class);
			bind(AuthController.class);
			bind(HomeController.class);
		}
	}

	public class MetricsModule extends AbstractModule {
		@Override
		protected void configure() {
			install(new ServletModule() {
				@Override
				protected void configureServlets() {
					Clock clock = Clock.defaultClock();
					bind(Clock.class).toInstance(clock);

					VirtualMachineMetrics vmMetrics = VirtualMachineMetrics.getInstance();
					bind(VirtualMachineMetrics.class).toInstance(vmMetrics);

					JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
					bind(JsonFactory.class).toInstance(jsonFactory);

					HealthCheckRegistry healthCheckRegistry = HealthChecks.defaultRegistry();
					bind(HealthCheckRegistry.class).toInstance(healthCheckRegistry);

					healthCheckRegistry.register(new DeadlockHealthCheck(vmMetrics));

					MetricsRegistry metricsRegistry = Metrics.defaultRegistry();
					bind(MetricsRegistry.class).toInstance(metricsRegistry);

					serve("/admin/ping").with(new PingServlet());
					serve("/admin/threads").with(new ThreadDumpServlet(vmMetrics));
					serve("/admin/healthcheck").with(new HealthCheckServlet(healthCheckRegistry));
					serve("/admin/metrics").with(new MetricsServlet(
						clock,
						vmMetrics,
						metricsRegistry,
						jsonFactory,
						true
					));

					filter("/admin/*").through(new AppMetricsFilter());
				}
			});
		}
	}
}
