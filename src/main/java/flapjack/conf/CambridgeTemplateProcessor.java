package flapjack.conf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;
import org.joda.time.DateTime;
import org.jvnet.hk2.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cambridge.ClassPathTemplateLoader;
import cambridge.ExpressionLanguage;
import cambridge.Template;
import cambridge.TemplateFactory;
import cambridge.TemplateLoader;
import cambridge.jexl.JexlExpressionLanguage;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;

import flapjack.entity.Person;
import flapjack.entity.Session;
import flapjack.exception.SessionTimeoutException;
import flapjack.manager.PersonManager;
import flapjack.utils.AppUtils;

/**
 * A Jersey 2.x Template Processor for integrating with Cambridge Templates
 *
 * @author Ray Vanderborght
 */
@javax.ws.rs.ext.Provider
public class CambridgeTemplateProcessor extends AbstractTemplateProcessor<Template> {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(CambridgeTemplateProcessor.class);

	/** Produces TemplateFactories */
	private TemplateLoader loader;

	/** Cache these, they are basically the compiled version of a template */
	private Map<String, TemplateFactory> factories = new ConcurrentHashMap<String, TemplateFactory>();

	@Inject
	private Provider<PersonManager> personManagerProvider;

	@Inject
	private Provider<HttpServletRequest> requestProvider;

	/**
	 * Create an instance of this processor with injected {@link javax.ws.rs.core.Configuration config} and
	 * (optional) {@link javax.servlet.ServletContext servlet context}.
	 *
	 * @param config config to configure this processor from.
	 * @param serviceLocator service locator to initialize template object factory if needed.
	 * @param servletContext (optional) servlet context to obtain template resources from.
	 */
	@Inject
	public CambridgeTemplateProcessor(final javax.ws.rs.core.Configuration config,
			final ServiceLocator serviceLocator, @Optional final ServletContext servletContext) {

		super(config, servletContext, "cambridge", "html");
		this.loader = new ClassPathTemplateLoader(getClass().getClassLoader());
	}

	@Override
	protected Template resolve(final String templateReference, final Reader reader) throws Exception {
		TemplateFactory fact = null;
		if (isProd()) {
			fact = factories.get(templateReference);
			if (fact == null) {
				fact = loader.newTemplateFactory(templateReference, getExpressionLanguage());
				factories.put(templateReference, fact);
			}
		} else {
			fact = loader.newTemplateFactory(templateReference, getExpressionLanguage());
		}

		Template template = fact.createTemplate();
		setCustomTemplateProperties(template);

		return template;
	}

	@Override
	public void writeTo(final Template template, final Viewable viewable, final MediaType mediaType,
						final MultivaluedMap<String, Object> httpHeaders, final OutputStream out) throws IOException {
		try {
			Object model = viewable.getModel();
			if (!(model instanceof Map)) {
				model = ImmutableMap.of("model", viewable.getModel());
			}

			Charset encoding = setContentType(mediaType, httpHeaders);
			Writer w = new OutputStreamWriter(out, encoding);

			template.setProperty("it", model);
			template.printTo(w);

			w.flush();

		} catch (Exception e) {
			throw new ContainerException(e);
		}
	}

	/**
	 * Configure the expression language to use in the templates
	 */
	protected ExpressionLanguage getExpressionLanguage() {
		JexlExpressionLanguage jexl = new JexlExpressionLanguage();
//		jexl.getEngine().setSilent(false);
//		jexl.getEngine().setLenient(false);

		// Add a custom 'tool' binding with our TemplateTool class
		// http://commons.apache.org/proper/commons-jexl/apidocs/org/apache/commons/jexl2/package-summary.html#configuration
		Map<String, Object> funcs = new HashMap<String, Object>();
		funcs.put("utils", AppUtils.class);
		jexl.getEngine().setFunctions(funcs);

		return jexl;
	}

	/**
	 * Add any custom template properties here
	 */
	protected void setCustomTemplateProperties(Template template) {
		DateTime now = new DateTime();
		PersonManager personManager = personManagerProvider.get();
		HttpServletRequest request = requestProvider.get();

		template.setProperty("app.name", "Flapjack");
		template.setProperty("app.root", request.getContextPath());
		template.setProperty("app.time", now);

		// find person's session and attach them if found
		Person person = null;
		try {
			Session session = personManager.getSession(request);
			if (session != null) {
				person = session.getPerson();
			}
			template.setProperty("app.sessionTimeout", false);
		} catch (SessionTimeoutException e) {
			template.setProperty("app.sessionTimeout", true);
		}

		template.setProperty("user", new User(person));
	}

	protected boolean isProd() {
		return false;
	}

	protected int checkInterval() {
		return isProd() ? -1 : 0;
	}

	/**
	 * A collection of user related things for the frontend
	 *
	 * @author Ray Vanderborght
	 */
	public static class User {
		private Person person;

		public User(Person person) {
			this.person = person;
		}

		public boolean isLoggedIn() {
			return person != null;
		}
		public boolean accountRequiresSetup() {
			return isLoggedIn() ? person.getName() == null : false;
		}

		public Person getPerson() {
			return person;
		}
	}
}
