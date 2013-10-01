package models;

import play.data.validation.Constraints;
import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;
import models.*;

@Entity
@Table(name="user")
public class User extends Model {
	
	@Id
	public Long userId;
	
	@Constraints.Required
	public String email;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="user")
	private List<Rating> userRatings = new ArrayList<Rating>();
	
	public User() {}
	
	public User(String email) {
		this.email = email;
	}
	
	public static Finder<String,User> find = new Finder<String,User>(
		String.class, User.class);
	
	public String toString() {
		return email;
	}
	
	public List<Rating> getRatings() {
		return Rating.find.where().eq("user", this).findList();
	}

	public static List<User> findAll() {
		return find.all();
	}
	
	public static User findByEmail(String email) {
		for (User candidate : find.all()) {
			if (candidate.email.equals(email)) {
				return candidate;
			}
		}
		return null;
	}
	
	public static User authenticate(String email) {
		return find.where().eq("email", email).findUnique();
	}

	public static boolean remove(User user) {
		return find.all().remove(user);
	}
	
	public static void add(User user) {
		find.all().add(user);
	}

}