package flapjack.conf;

import javax.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.jvnet.hk2.guice.bridge.api.HK2IntoGuiceBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Configuration for Jersey 2.x
 *
 * @author ray
 */
public class JerseyConfig extends ResourceConfig {

	private static Logger log = LoggerFactory.getLogger(JerseyConfig.class);

	@Inject
	public JerseyConfig(ServiceLocator serviceLocator) {
		log.info("Registering jersey injectables...");

		property(MvcFeature.TEMPLATE_BASE_PATH, "META-INF/resources/templates/");

		registerProviders();
		createBiDirectionalGuiceBridge(serviceLocator, GuiceConfig.getAllModulesAsArray());
	}

	private void registerProviders() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
		provider.setMapper(mapper);
		register(provider);
	}

	public Injector createBiDirectionalGuiceBridge(ServiceLocator serviceLocator, Module... applicationModules) {
		Module[] allModules = new Module[applicationModules.length + 1];

		allModules[0] = new HK2IntoGuiceBridge(serviceLocator);
		for (int lcv = 0; lcv < applicationModules.length; lcv++) {
			allModules[lcv + 1] = applicationModules[lcv];
		}

		Injector injector = GuiceConfig.getInjectorInstance();
		GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
		GuiceIntoHK2Bridge g2h = serviceLocator.getService(GuiceIntoHK2Bridge.class);
		g2h.bridgeGuiceInjector(injector);

		return injector;
	}
}
