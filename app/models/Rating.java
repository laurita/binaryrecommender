package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;
import play.data.validation.Constraints;

@Entity
@Table(name="rating")
public class Rating extends Model {
	
	@Id
	public Long ratingId;
	
	@Constraints.Required
	public int value;
	
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
	
	public static Finder<Long,Rating> find = new Finder<Long,Rating>(
		Long.class, Rating.class);
	
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