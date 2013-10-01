package models;

import play.data.validation.Constraints;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;
import play.db.ebean.*;
import models.*;

@Entity
@Table(name="movie")
public class Movie extends Model {
	
	@Id
	@Constraints.Required
	public String movieId;
	
	@Constraints.Required
	public String title;
	
	public String description;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="movie")
	private List<Rating> movieRatings;
	
	public Movie() {}
	
		public Movie(String id, String title, String description) {
			this.movieId = id;
			this.title = title;
			this.description = description;
		}
	
		public String toString() {
			return String.format("%s - %s", movieId, title);
		}
	
		public List<Rating> getRatings() {
			return Rating.find.where().eq("movie", this).findList();
		}
	
		public static Finder<String,Movie> find = new Finder<String,Movie>(
			String.class, Movie.class);

		public static List<Movie> findAll() {
			return find.all();
		}
	
		public static Movie findById(String id) {
			for (Movie candidate : find.all()) {
				if (candidate.movieId.equals(id)) {
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
			find.all().remove(findById(this.movieId));
			find.all().add(this);
		}

	}