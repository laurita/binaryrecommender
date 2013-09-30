package models;

import play.data.validation.Constraints;
import java.util.List;
import java.util.ArrayList;

public class User {
	
	private static List<User> users;
	static {
		users = new ArrayList<User>();
		users.add(new User("user1@gmail.com"));
		users.add(new User("user2@gmail.com"));
		users.add(new User("user3@gmail.com"));
		users.add(new User("user4@gmail.com"));
		users.add(new User("user5@gmail.com"));
		users.add(new User("user6@gmail.com"));
		users.add(new User("user7@gmail.com"));
		users.add(new User("user8@gmail.com"));
		users.add(new User("user9@gmail.com"));
		users.add(new User("user10@gmail.com"));
		users.add(new User("user11@gmail.com"));
		users.add(new User("user12@gmail.com"));
		users.add(new User("user13@gmail.com"));
		users.add(new User("user14@gmail.com"));
		users.add(new User("user15@gmail.com"));
		users.add(new User("user16@gmail.com"));
		users.add(new User("user17@gmail.com"));
		users.add(new User("user18@gmail.com"));
		users.add(new User("user19@gmail.com"));
		users.add(new User("user20@gmail.com"));
	}
	
	@Constraints.Required
	@Constraints.Email
	public String email;
	
	public User() {}
	
	public User(String email) {
		this.email = email;
	}
	
	public String toString() {
		return email;
	}

	public static List<User> findAll() {
		return new ArrayList<User>(users);
	}
	
	public static User findByEmail(String email) {
		for (User candidate : users) {
			if (candidate.email.equals(email)) {
				return candidate;
			}
		}
		return null;
	}
	
	public static boolean remove(User user) {
		return users.remove(user);
	}
	
	public static void add(User user) {
		users.add(user);
	}
	
	public void save() {
		users.remove(findByEmail(this.email));
		users.add(this);
	}

}