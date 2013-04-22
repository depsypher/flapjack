package flapjack.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import flapjack.entity.Person;
import flapjack.entity.Session;
import flapjack.manager.PersonManager;
import flapjack.utils.AppUtils;

/**
 * Controller handling authentication
 *
 * @author Ray Vanderborght
 */
@Path("/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String PERSONA_VERIFIER_URL = "https://verifier.login.persona.org/verify";
	private static final String PERSONA_SUCCESS = "okay";

	@Inject
	private PersonManager personManager;

	/**
	 * Handle mozilla persona login
	 */
	@POST @Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> login(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@FormParam("assertion") String assertion) {

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(PERSONA_VERIFIER_URL).openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write("assertion=" + URLEncoder.encode(assertion, "UTF-8"));
			writer.write("&audience=" + request.getServerName());
			writer.close();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				@SuppressWarnings("unchecked")
				Map<String, Object> results = mapper.readValue(connection.getInputStream(), Map.class);

				// persona has authenticated the user
				if (PERSONA_SUCCESS.equals(results.get("status"))) {
					String email = (String) results.get("email");

					// find user in our system, or trigger creation of new user
					Person user = personManager.findByEmail(email);
					if (user == null) {
						user = new Person(email);
						personManager.save(user);
					}

					String value = user.getId() + ":" + UUID.randomUUID().toString();
					Session session = new Session(user, value, new DateTime());
					personManager.save(session);

					Cookie loginCookie = new Cookie(AppUtils.LOGIN_COOKIE_NAME, value);
					loginCookie.setPath("/");
					loginCookie.setMaxAge(Long.valueOf(((Long) results.get("expires")) / 1000).intValue());
					response.addCookie(loginCookie);

					String name = Objects.firstNonNull(user.getName(), "");
					return ImmutableMap.of("email", user.getEmail(), "name", name);
				}
			}
		} catch (MalformedURLException e) {
			log.error("error", e);
		} catch (IOException e) {
			log.error("error", e);
		}

		return null;
	}

	/**
	 * Handle logout
	 */
	@POST @Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		Cookie loginCookie = AppUtils.findCookie(request.getCookies(), AppUtils.LOGIN_COOKIE_NAME);

		if (loginCookie != null) {
			personManager.logout(loginCookie.getValue());
			loginCookie.setMaxAge(0);
			loginCookie.setPath("/");
			response.addCookie(loginCookie);
			return true;
		}
		return false;
	}
}
