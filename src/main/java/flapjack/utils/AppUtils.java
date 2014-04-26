package flapjack.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.commons.jexl2.JexlContext;
import org.joda.time.DateTime;

/**
 * Utility class for the application
 *
 * NOTE: take care if you refactor anything here, templates may use it
 *
 * @author Ray Vanderborght
 */
public class AppUtils {

	public static final String LOGIN_COOKIE_NAME = "login";
	public static final int SESSION_TIMEOUT = Integer.valueOf(System.getProperty("session.timeout", "30"));	// minutes

	private static final String YYYY_MM_DD = "yyyy-MM-dd";
	private static final String YYYY_MM = "yyyy-MM";
	private static final String YYYY = "yyyy";

	/** To let jexl use this class in the templates */
	private AppUtils(JexlContext context) {
	}

	/**
	 * Find a cookie's value by its name
	 */
	public static String findCookieValue(Cookie[] cookies, String name) {
		Cookie cookie = findCookie(cookies, name);
		return (cookie == null) ? null : cookie.getValue();
	}

	/**
	 * Find a cookie by its name
	 */
	public static Cookie findCookie(Cookie[] cookies, String name) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * Test for valid string input
	 */
	public static boolean isValid(String input, int minLength, int maxLength) {
		if (input == null || input.length() < minLength || input.length() > maxLength) {
			return false;
		}
		return true;
	}

	/**
	 * Get just the domain part of a url
	 */
	public static String getDomainName(String url) {
		try {
			return new URI(url).getHost();
		} catch (URISyntaxException e) {
		}
		return null;
	}

	public static String formatDay(DateTime date) {
		return date.toString(YYYY_MM_DD, Locale.US);
	}

	public static String formatMonth(DateTime date) {
		return date.toString(YYYY_MM, Locale.US);
	}

	public static String formatYear(DateTime date) {
		return date.toString(YYYY, Locale.US);
	}
}
