package flapjack.conf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;

import cambridge.ClassPathTemplateLoader;
import cambridge.Expressions;
import cambridge.Template;
import cambridge.TemplateFactory;
import cambridge.TemplateLoader;
import cambridge.jexl.JexlExpressionLanguage;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

import flapjack.entity.Person;
import flapjack.entity.Session;
import flapjack.exception.SessionTimeoutException;
import flapjack.manager.PersonManager;
import flapjack.utils.AppUtils;

/**
 * A Jersey View Processor for integrating with Cambridge Templates
 *
 * @author Ray Vanderborght
 */
@javax.ws.rs.ext.Provider
public class CambridgeTemplateProvider implements ViewProcessor<String> {

	private static final String UTF_8 = "UTF-8";
	private static final String TEMPLATE_DIR = "static/templates";

	/** Produces TemplateFactories */
	private TemplateLoader loader;

	/** Cache these, they are basically the compiled version of a template */
	private Map<String, TemplateFactory> factories = new ConcurrentHashMap<String, TemplateFactory>();

	@Inject
	private Provider<PersonManager> personManagerProvider;

	@Inject
	private Provider<HttpServletRequest> requestProvider;

	@Inject
	public CambridgeTemplateProvider(ServletContext context) throws Exception {
		setExpressionLanguage();

		this.loader = new ClassPathTemplateLoader(getClass().getClassLoader());
	}

	@Override
	public String resolve(String path) {
		return path;
	}

	@Override
	public void writeTo(String t, Viewable viewable, OutputStream out) throws IOException {
		// commit the status and headers to the HttpServletResponse
		out.flush();

		String templ = TEMPLATE_DIR + t;
		TemplateFactory fact = null;
		if (isProd()) {
			fact = factories.get(templ);
			if (fact == null) {
				fact = loader.newTemplateFactory(templ);
				factories.put(templ, fact);
			}
		} else {
			fact = loader.newTemplateFactory(templ);
		}

		Template template = fact.createTemplate();
		template.setProperty("it", viewable.getModel());

		setCustomTemplateProperties(template);

		Writer w = new OutputStreamWriter(out, UTF_8);
		template.printTo(w);
		w.flush();
	}

	/**
	 * Configure the expression language to use in the templates
	 */
	protected void setExpressionLanguage() {
		JexlExpressionLanguage jexl = new JexlExpressionLanguage();
		//jexl.getEngine().setSilent(false);
		//jexl.getEngine().setLenient(false);
		Expressions.setDefaultExpressionLanguage("jexl", jexl);

		// Add a custom 'tool' binding with our TemplateTool class
		// http://commons.apache.org/proper/commons-jexl/apidocs/org/apache/commons/jexl2/package-summary.html#configuration
		Map<String, Object> funcs = new HashMap<String, Object>();
		funcs.put("utils", AppUtils.class);
		jexl.getEngine().setFunctions(funcs);
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
