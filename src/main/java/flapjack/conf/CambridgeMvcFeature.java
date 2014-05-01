package flapjack.conf;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.server.mvc.MvcFeature;

/**
 * Configuration for the Cambridge template processor
 *
 * @see CambridgeTemplateProvider
 * @author ray
 */
@ConstrainedTo(RuntimeType.SERVER)
public class CambridgeMvcFeature implements Feature {

	private final static String SUFFIX = ".html";

	public static final String TEMPLATES_BASE_PATH = MvcFeature.TEMPLATE_BASE_PATH + SUFFIX;
	public static final String CACHE_TEMPLATES = MvcFeature.CACHE_TEMPLATES + SUFFIX;
	public static final String TEMPLATE_OBJECT_FACTORY = MvcFeature.TEMPLATE_OBJECT_FACTORY + SUFFIX;
	public static final String ENCODING = MvcFeature.ENCODING + SUFFIX;

	@Override
	public boolean configure(final FeatureContext context) {
		final Configuration config = context.getConfiguration();

		if (!config.isRegistered(CambridgeTemplateProvider.class)) {
			context.register(CambridgeTemplateProvider.class);

			if (!config.isRegistered(MvcFeature.class)) {
				context.register(MvcFeature.class);
			}
			return true;
		}
		return false;
	}
}
