package org.alnitaka.zenon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import java.util.Set;
import java.util.HashSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.alnitaka.zenon.security.Role;
import org.springframework.data.annotation.CreatedDate;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Entity
@Getter
@Setter
@Table
@NoArgsConstructor
public class User {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Temporal(TIMESTAMP)
	@Column(name = "date_creation", nullable = false, length = 19)
	private Date dateCreation = new Date();

	@Column(nullable = false)
	@NotNull
	private String email;

	@Column
	private String password;

	@Column
	private String lastname;

	@Column
	private String firstname;

	@Column(nullable = false)
	private boolean active = true;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role")
	private Set<Role> roles = new HashSet<>();

	public User(String email, String password, String lastname, String firstname) {
		this.email = email;
		this.password = password;
		this.lastname = lastname;
		this.firstname = firstname;
	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public User(String email) {
		this.email = email;
	}
}
