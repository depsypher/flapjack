package flapjack.controller;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.annotation.Timed;

import flapjack.entity.Session;
import flapjack.manager.PersonManager;

/**
 * Controller for the home page
 *
 * @author Ray Vanderborght
 */
@Path("/")
public class HomeController {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HomeController.class);

	@Inject
	private PersonManager personManager;

	/**
	 * Get the main home page
	 */
	@GET
	@Produces(MediaType.TEXT_HTML) @Timed
	public Viewable home(@Context HttpServletRequest request) {
		Session session = personManager.findSession(request);
		Map<String, String> model = new HashMap<String, String>();

		if (session != null) {
			model.put("person", session.getPerson().getEmail());
		}
		return new Viewable("/home.html", model);
	}

	@GET @Path("/metrics")
	@Produces(MediaType.TEXT_HTML) @Timed
	public Viewable metrics(@Context HttpServletRequest request) {
		Map<String, String> model = new HashMap<String, String>();
		return new Viewable("/metrics.html", model);
	}
}
