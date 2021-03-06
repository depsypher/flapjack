package flapjack.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.common.base.Objects;

import flapjack.auth.Role;

/**
 * Represents a happy user of our wonderful system
 *
 * @author Ray Vanderborght
 */
@Entity(name="person")
@NamedQueries({
	@NamedQuery(
		name="Person.findByEmail",
		query="from person where email = :email"
	)
})
public class Person implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue
	@Column(name="person_id")
	private Long id;

	@Column
	private String name;

	@Column(nullable=false)
	private String email;

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private Role role;

	public Person() {
	}

	public Person(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(email);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;

		if (getClass().equals(obj.getClass())) {
			Person other = (Person) obj;
			return Objects.equal(this.email, other.getEmail());
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("email", email)
				.add("name", name)
				.add("role", role)
				.toString();
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
}
