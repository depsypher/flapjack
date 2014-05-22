package flapjack.auth;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flapjack.entity.Session;
import flapjack.manager.PersonManager;

/**
 * Intercepts methods that have @Auth on them and handles authentication
 *
 * @author Ray Vanderborght
 */
@Singleton
public class AuthInterceptor implements MethodInterceptor {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

	@Inject private PersonManager personManager;
	@Context private HttpServletRequest request;
	@Context private HttpServletResponse response;

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Session session = personManager.findSession(request);

		Auth auth = invocation.getMethod().getAnnotation(Auth.class);

		List<Role> requiredRoles = Arrays.asList(auth.requiresRole());
		if (!requiredRoles.isEmpty() &&
				(session == null || !requiredRoles.contains(session.getPerson().getRole()))) {
			response.sendRedirect("/");
			return null;
		}

		if (session != null) {
			request.setAttribute("mvp.person.email", session.getPerson().getEmail());
		}

		return invocation.proceed();
	}
}
