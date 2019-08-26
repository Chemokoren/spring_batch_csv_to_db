package com.infotech.batch.model;

import javax.persistence.Column;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import java.io.Serializable;


@SecondaryTables({
		@SecondaryTable(name="email",
				pkJoinColumns=@PrimaryKeyJoinColumn(name="person_id")),
		@SecondaryTable(name="age",
				pkJoinColumns=@PrimaryKeyJoinColumn(name="person_id")),
})
public class Person implements Serializable{

	private static final long serialVersionUID = -6402068923614583448L;
	private String firstName;
    private String lastName;

	@Column(table = "email")
    private String email;

	@Column(table = "age")
    private Integer age;
    
    public Person() {
	}

	public Person(String firstName, String lastName, String email, Integer age) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.age = age;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Person [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", age=" + age + "]";
	}
}
