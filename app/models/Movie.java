package models;

import play.data.validation.Constraints;
import java.util.*;
import javax.persistence.*;
import play.db.ebean.Model.*;
import play.db.ebean.*;
import models.*;
import com.avaje.ebean.Expr;
import com.avaje.ebean.*;

@Entity
@Table(name="movie")
public class Movie extends Model {
	
	@Id
	@Constraints.Required
		public int id;
	
	@Constraints.Required
		public String title;
	
	public String description;
	
	public double logpopvar;
	
	public String trailerLink;
	
	public int length;
	
	public String imdbLink;
	
	public String genres;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="movie")
	private List<Rating> movieRatings;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="movie1")
	private List<Preference> movie1Preferences;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="movie2")
	private List<Preference> movie2Preferences;
	
	public Movie() {
	}
	
	public Movie(int id, String title, String description) {
		this.id = id;
		this.title = title;
		this.description = description;
	}
	
	public String toString() {
		return String.format("%s - %s", id, title);
	}
	
	public List<Rating> getRatings() {
		return Rating.find.where().eq("movie", this).findList();
	}
	
	public static Finder<Integer,Movie> find = new Finder<Integer,Movie>(
		Integer.class, Movie.class);

	public static List<Movie> findAll() {
		return find.all();
	}
	
	public static Movie findById(int id) {
		for (Movie candidate : find.all()) {
			if (candidate.id == id) {
				return candidate;
			}
		}
		return null;
	}
  
	public static List<SqlRow> selectMoviePairs(int userId, int limit, int offset) {
    System.out.println("limit " + limit + ", offset " + offset);
		String sql = String.format("select c.movie1_id, a.title movie1_title, a.description movie1_description, " +
		"a.length movie1_length, a.imdb_link movie1_imdbLink, a.trailer_link movie1_trailerLink, " +
		"c.movie2_id, b.title movie2_title, b.description movie2_description, b.length movie2_length, " +
		"b.imdb_link movie2_imdbLink, b.trailer_link movie2_trailerLink, c.value " +
		"from preference as c, movie as a, movie as b " +
		"where c.user_id = %d and c.movie1_id = a.id and c.movie2_id = b.id " +
		"order by c.logpopcorrrand desc limit %d offset %d", userId, limit, offset);
    
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> moviePairs = sqlQuery.findList();
		
		return moviePairs;
	}
	
	public static List<Movie> findByTitle(String title) {
		final List<Movie> results = new ArrayList<Movie>();
		for (Movie candidate : find.all()) {
			if (candidate.title.toLowerCase().contains(title.toLowerCase())) {
				results.add(candidate);
			}
		}
		return results;
	}
	
	public static boolean remove(Movie movie) {
		return find.all().remove(movie);
	}
	
	public static void add(Movie movie) {
		find.all().add(movie);
	}
	
	public void save() {
		find.all().remove(findById(this.id));
		find.all().add(this);
	}

}