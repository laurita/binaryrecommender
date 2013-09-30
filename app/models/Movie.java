package models;

import play.data.validation.Constraints;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.*;

@Entity
public class Movie extends Model {
	
	@Id
	@Constraints.Required
	public String id;
	@Constraints.Required
	public String title;
	public String description;
	
	public Movie() {}
	
	public Movie(String id, String title, String description) {
		this.id = id;
		this.title = title;
		this.description = description;
	}
	
	public String toString() {
		return String.format("%s - %s", id, title);
	}
	
	public static Finder<String,Movie> find = new Finder<String,Movie>(
		String.class, Movie.class);

	public static List<Movie> findAll() {
		return find.all();
	}
	
	public static Movie findById(String id) {
		for (Movie candidate : find.all()) {
			if (candidate.id.equals(id)) {
				return candidate;
			}
		}
		return null;
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