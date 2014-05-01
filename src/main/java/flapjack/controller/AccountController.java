package flapjack.controller;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableMap;

import org.glassfish.jersey.server.mvc.Viewable;

import com.yammer.metrics.annotation.Timed;

import flapjack.entity.Person;
import flapjack.entity.Session;
import flapjack.manager.PersonManager;
import flapjack.utils.AppUtils;

/**
 * Controller for the account page
 *
 * @author Ray Vanderborght
 */
@Path("/account")
public class AccountController {

	@Inject
	private PersonManager personManager;

	/**
	 * Get the account page
	 */
	@GET
	@Produces(MediaType.TEXT_HTML) @Timed
	public Viewable account() {
		Map<String, String> model = new HashMap<String, String>();
		return new Viewable("/account.html", model);
	}

	/**
	 * Handle account setup form submission
	 */
	@POST @Path("/setup")
	@Produces(MediaType.APPLICATION_JSON) @Timed
	public Map<String, String> setup(@Context HttpServletRequest request, @FormParam("name") String name) {
		if (!AppUtils.isValid(name, 3, 32)) {
			return ImmutableMap.of("errorMessage", "Must supply a name between 3 and 32 characters long");
		}

		Session session = personManager.findSession(request);
		if (session == null) {
			return ImmutableMap.of("errorMessage", "Must be logged in");
		}
		Person person = session.getPerson();
		person.setName(name);
		personManager.update(person);

		return ImmutableMap.of("status", "success");
	}
}
