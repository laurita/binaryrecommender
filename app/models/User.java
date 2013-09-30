package models;

import play.data.validation.Constraints;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;

@Entity
public class User extends Model {
	
	/*
	private static List<User> users;
	static {
		users = new ArrayList<User>();
		users.add(new User("user1@gmail.com", "secret"));
		users.add(new User("user2@gmail.com", "secret"));
		users.add(new User("user3@gmail.com", "secret"));
		users.add(new User("user4@gmail.com", "secret"));
		users.add(new User("user5@gmail.com", "secret"));
		users.add(new User("user6@gmail.com", "secret"));
		users.add(new User("user7@gmail.com", "secret"));
		users.add(new User("user8@gmail.com", "secret"));
		users.add(new User("user9@gmail.com", "secret"));
		users.add(new User("user10@gmail.com", "secret"));
		users.add(new User("user11@gmail.com", "secret"));
		users.add(new User("user12@gmail.com", "secret"));
		users.add(new User("user13@gmail.com", "secret"));
		users.add(new User("user14@gmail.com", "secret"));
		users.add(new User("user15@gmail.com", "secret"));
		users.add(new User("user16@gmail.com", "secret"));
		users.add(new User("user17@gmail.com", "secret"));
		users.add(new User("user18@gmail.com", "secret"));
		users.add(new User("user19@gmail.com", "secret"));
		users.add(new User("user20@gmail.com", "secret"));
	}
	*/
		
	@Id
	public String email;
	@Constraints.Required
	public String password; 
	
	//@Constraints.Required
	//@Constraints.Email
	
	public User() {}
	
	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	public static Finder<String,User> find = new Finder<String,User>(
		String.class, User.class);
	
	public String toString() {
		return email;
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
	
	public static User authenticate(String email, String password) {
		return find.where().eq("email", email)
			.eq("password", password).findUnique();
	}

	public static boolean remove(User user) {
		return find.all().remove(user);
	}
	
	public static void add(User user) {
		find.all().add(user);
	}
	
	public void save() {
		find.all().remove(findByEmail(this.email));
		find.all().add(this);
	}

}