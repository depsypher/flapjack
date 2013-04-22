package flapjack.conf;

import java.io.IOException;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.common.collect.ImmutableSet;
import com.yammer.metrics.web.DefaultWebappMetricsFilter;

/**
 * Only allows traffic served from a local host. Used to prevent normal users
 * from being able to see the metrics.
 *
 * @author Ray Vanderborght
 */
public class AppMetricsFilter extends DefaultWebappMetricsFilter {

	private static final Set<String> allowedHosts = ImmutableSet.of("localhost", "127.0.0.1");

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!allowedHosts.contains(request.getServerName())) {
			throw new ServletException("Not allowed");
		}
		super.doFilter(request, response, chain);
	}
}
