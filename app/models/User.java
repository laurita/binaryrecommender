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
	
	@Column(name = "created_at")
	public Date createdAt;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="user")
	private List<Rating> userRatings = new ArrayList<Rating>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="user")
	private List<Preference> userPreferences = new ArrayList<Preference>();
	
	// group: 1 (MF / ratings) or 2 (UP / preferences)
	public int experimentGroup;
	
	// true if stage 1 finished
	public boolean stage1Done = false;
	
	public String question1;
	public String question2;
	public String question3;
	public String question4;
	
	public User() {}
	
	public User(String email) {
		this.email = email;
	}
	
	@Override
	public void save() {
		createdAt();
		addExperimentGroup();
		super.save();
	}
	
  @PrePersist
 	void createdAt() {
 		this.createdAt = new Date();
	}
	
	private void addExperimentGroup() {
		int experimentGroup = 1;
		User lastUser = User.find.orderBy().desc("createdAt").setMaxRows(1).findUnique();
		int lastExperimentGroup = (lastUser != null) ? lastUser.experimentGroup : 0;
		if (lastExperimentGroup != 0) {
			experimentGroup = (lastExperimentGroup == 1) ? 2 : 1;
		}
		this.experimentGroup = experimentGroup;
	}
	
		public static Finder<String,User> find = new Finder<String,User>(
			String.class, User.class);
	
		public String toString() {
			return email;
		}
	
		public List<Rating> getRatings() {
			return Rating.find.where().eq("user", this).findList();
		}
		
		public List<Preference> getPreferences() {
			return Preference.find.where().eq("user", this).findList();
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