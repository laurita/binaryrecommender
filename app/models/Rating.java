package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;
import play.data.validation.Constraints;

@Entity
@Table(name="rating")
public class Rating extends Model {
	
	@Id
	public int id;
	
	@Constraints.Required
	public int value;
  
  public boolean additional = false;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public Movie movie;
	
	private Rating(User user, Movie movie, int value) {
		this.user = user;
		this.movie = movie;
		this.value = value;
	}
	
	public static Rating create(User user, Movie movie, int value) {
		Rating r = Rating.find.where().eq("user", user).eq("movie", movie).findUnique();
		if (r != null) {
			r.delete();
		}
		r = new Rating(user, movie, value);
		r.save();
		return r;
	}
	
	public static Finder<Integer,Rating> find = new Finder<Integer,Rating>(
		Integer.class, Rating.class);
	
	public static List<Rating> findByUser(User user) {  
	    return find.where().eq("user", user).findList();  
	} 
	
	public static List<Rating> findByMovie(Movie movie) {  
	    return find.where().eq("movie", movie).findList();  
	} 
	
	public static List<Rating> findAll() {
		return find.all();
	}
}