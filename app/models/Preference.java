package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;
import play.data.validation.Constraints;

@Entity
@Table(name="preference")
public class Preference extends Model {
	
	@Id
	public Long id;
	
	@Constraints.Required
	public int value;
	
	@ManyToOne
	public User user;
	
	@Constraints.Required
	@ManyToOne
	public Movie movie1;
	
	@Constraints.Required
	@ManyToOne
	public Movie movie2;
	
	private Preference(User user, Movie movie1, Movie movie2, int value) {
		this.user = user;
		this.movie1 = movie1;
		this.movie2 = movie2;
		this.value = value;
	}
	
	public String toString() {
		return String.format("%d - %s - %s - %d", user.id, movie1.id, movie2.id, value);
	}
	
	public static Preference create(User user, Movie movie1, Movie movie2, int value) {
		Preference p = Preference.find.where().eq("user", user)
			.eq("movie1", movie1).eq("movie2", movie2).findUnique();
		if (p != null) {
			p.delete();
		}
		p = new Preference(user, movie1, movie2, value);
		p.save();
		return p;
	}
	
	public static Finder<Long,Preference> find = new Finder<Long,Preference>(
		Long.class, Preference.class);
	
	public static List<Preference> findByUser(User user) {  
	    return find.where().eq("user", user).findList();  
	} 
	
	public static List<Preference> findByMovie1(Movie movie1) {  
	    return find.where().eq("movie1", movie1).findList();  
	} 
	
	public static List<Preference> findByMovie2(Movie movie2) {  
	    return find.where().eq("movie2", movie2).findList();  
	}
	
	public static List<Preference> findAll() {
		return find.all();
	}
}