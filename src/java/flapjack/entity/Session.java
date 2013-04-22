package flapjack.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;

/**
 * A user session
 *
 * @author Ray Vanderborght
 */
@Entity(name="session")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
	@NamedQuery(
		name="Session.findByValue",
		query="from session where value = :value",
		hints={ @QueryHint(name="org.hibernate.cacheable", value="true") }
	)
})
public class Session implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String SEQUENCE_NAME = "session_id_seq";

	/**
	 * Here we optimistically assume that a fair number of people are going
	 * to sign in at once, so we use a special database sequence that allocates
	 * multiple ids at the same time to reduce contention.
	 */
	@Id @GeneratedValue(generator=SEQUENCE_NAME)
	@SequenceGenerator(name=SEQUENCE_NAME, sequenceName=SEQUENCE_NAME, allocationSize=50)
	private Long id;

	@ManyToOne
	private Person person;

	@Column(nullable=false, length=255)
	private String value;

	@Column(nullable=false)
	private DateTime updated;

	public Session() {
	}

	public Session(Person person, String value, DateTime updated) {
		this.person = person;
		this.value = value;
		this.updated = updated;
	}

	public Person getPerson() {
		return person;
	}

	public DateTime getUpdated() {
		return updated;
	}
	public void setUpdated(DateTime updated) {
		this.updated = updated;
	}
}
