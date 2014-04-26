package flapjack.manager.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import flapjack.entity.Person;
import flapjack.entity.Session;
import flapjack.exception.SessionTimeoutException;
import flapjack.manager.PersonManager;
import flapjack.utils.AppUtils;

/**
 * Handle data access for persons in the system
 *
 * @author Ray Vanderborght
 */
@Transactional
public class PersonManagerJpa implements PersonManager {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PersonManagerJpa.class);

	@Inject
	private Provider<EntityManager> em;

	@Override
	public Person find(Long id) {
		return em.get().find(Person.class, id);
	}

	@Override
	public void save(Person entity) {
		em.get().persist(entity);
	}

	@Override
	public void update(Person entity) {
		em.get().merge(entity);
	}

	@Override
	public void save(Session entity) {
		em.get().persist(entity);
	}

	@Override
	public Person findByEmail(String email) {
		TypedQuery<Person> query = em.get().createNamedQuery("Person.findByEmail", Person.class);
		query.setParameter("email", email);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Session getSession(String value) {
		TypedQuery<Session> query = em.get().createNamedQuery("Session.findByValue", Session.class);
		query.setParameter("value", value);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Get the user's session, returning null if not found.
	 */
	@Override
	public Session findSession(HttpServletRequest request) {
		String cookie = AppUtils.findCookieValue(request.getCookies(), AppUtils.LOGIN_COOKIE_NAME);
		return (cookie == null) ? null : getSession(cookie);
	}

	/**
	 * Get the user's session, returning null if it doesn't exist and throwing
	 * a SessionTimeoutException if it's found but out of date.
	 */
	@Override
	@Transactional(ignore={ SessionTimeoutException.class })
	public Session getSession(HttpServletRequest request) throws SessionTimeoutException {
		Session session = getSession(request.getCookies());
		if (session == null) {
			return null;
		}
		// if sessions are set to timeout then handle deleting old ones and setting the last updated date
		if (AppUtils.SESSION_TIMEOUT > 0) {
			DateTime now = new DateTime();
			if (session.getUpdated().isBefore(now.minusMinutes(AppUtils.SESSION_TIMEOUT))) {
				em.get().remove(session);
				throw new SessionTimeoutException();
			}
			session.setUpdated(now);
		}
		return session;
	}

	@Override
	public void logout(String sessionValue) {
		Session session = getSession(sessionValue);
		if (session != null) {
			em.get().remove(session);
		}
	}

	private Session getSession(Cookie[] cookies) {
		String cookie = AppUtils.findCookieValue(cookies, AppUtils.LOGIN_COOKIE_NAME);
		return (cookie == null) ? null : getSession(cookie);
	}
}
