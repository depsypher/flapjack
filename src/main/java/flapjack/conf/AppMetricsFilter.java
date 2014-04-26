package flapjack.conf;

import java.io.IOException;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.yammer.metrics.web.DefaultWebappMetricsFilter;

import flapjack.entity.Person;
import flapjack.entity.Session;
import flapjack.manager.PersonManager;

/**
 * Only allows admin access or traffic served from a local host. Used to prevent normal users
 * from being able to see the metrics.
 *
 * @author Ray Vanderborght
 */
public class AppMetricsFilter extends DefaultWebappMetricsFilter {

	private static final Set<String> allowedHosts = ImmutableSet.of("localhost", "127.0.0.1");

	@Inject
	private PersonManager personManager;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		Session session = personManager.getSession((HttpServletRequest) request);
		if (session == null) {
			if (!allowedHosts.contains(request.getServerName())) {
				throw new ServletException("Not allowed");
			}
		} else {
			Person person = session.getPerson();
			if (person == null || !person.isAdmin()) {
				throw new ServletException("Not allowed");
			}
		}
		super.doFilter(request, response, chain);
	}
}
