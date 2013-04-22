package flapjack.manager;

import javax.servlet.http.HttpServletRequest;

import flapjack.entity.Person;
import flapjack.entity.Session;
import flapjack.exception.SessionTimeoutException;

public interface PersonManager {

	public Person find(Long id);

	public void save(Person entity);

	public void update(Person entity);

	public void save(Session entity);

	public Person findByEmail(String email);

	public Session getSession(String value);

	public Session findSession(HttpServletRequest request);

	public Session getSession(HttpServletRequest request) throws SessionTimeoutException;

	public void logout(String sessionValue);
}