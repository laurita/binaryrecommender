package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;
import play.data.validation.Constraints;

@Entity
@Table(name="recommendation")
public class Recommendation extends Model {
	
	@Id
	public int id;
	
	@Constraints.Required
	@Constraints.Min(1)
	@Constraints.Max(10)
	public int rank;
	
	@ManyToOne
	@Constraints.Required
	public User user;
	
	@ManyToOne
	@Constraints.Required
	public Movie movie;
	
	public boolean good;
	
	public boolean seen;
	
	private Recommendation(User user, Movie movie, int rank) {
		this.user = user;
		this.movie = movie;
		this.rank = rank;
	}
	
	public static Recommendation create(User user, Movie movie, int rank) {
		Recommendation r = Recommendation.find.where().eq("user", user).eq("movie", movie).findUnique();
		if (r != null) {
			r.delete();
		}
		r = new Recommendation(user, movie, rank);
		r.save();
		return r;
	}
	
	public static Finder<Integer,Recommendation> find = new Finder<Integer,Recommendation>(
		Integer.class, Recommendation.class);
	
	public static List<Recommendation> findByUser(User user) {  
	    return find.where().eq("user", user).findList();  
	} 
	
	public static List<Recommendation> findByMovie(Movie movie) {  
	    return find.where().eq("movie", movie).findList();  
	} 
	
	public static List<Recommendation> findAll() {
		return find.all();
	}
}