package models;

import play.data.validation.Constraints;
import java.util.List;
import java.util.ArrayList;

public class Movie {
	
	private static List<Movie> movies;
	  static {
	    movies = new ArrayList<Movie>();
	    movies.add(new Movie("2710", "Blair Witch Project, The (1999)", ""));
	    movies.add(new Movie("1924", "Plan 9 from Outer Space (1958)", ""));
	    movies.add(new Movie("231", "Dumb & Dumber (1994)", ""));
	    movies.add(new Movie("2657", "Rocky Horror Picture Show, The (1975)", ""));
	    movies.add(new Movie("288", "Natural Born Killers (1994)", ""));
			movies.add(new Movie("2700", "South Park: Bigger, Longer and Uncut (1999)", ""));
			movies.add(new Movie("2712", "Eyes Wide Shut (1999)", ""));
			movies.add(new Movie("1676", "Starship Troopers (1997)", ""));
			movies.add(new Movie("1917", "Armageddon (1998)", ""));
			movies.add(new Movie("1721", "Titanic (1997)", ""));
			movies.add(new Movie("2427", "Thin Red Line, The (1998)", ""));
			movies.add(new Movie("1391", "Mars Attacks! (1996)", ""));
			movies.add(new Movie("2628", "Star Wars: Episode I - The Phantom Menace (1999)", ""));
			movies.add(new Movie("2459", "Texas Chainsaw Massacre, The (1974)", ""));
			movies.add(new Movie("1183", "English Patient, The (1996)", ""));
			movies.add(new Movie("2683", "Austin Powers: The Spy Who Shagged Me (1999)", ""));
			movies.add(new Movie("3785", "Scary Movie (2000)", ""));
			movies.add(new Movie("2384", "Babe: Pig in the City (1998)", ""));
			movies.add(new Movie("327", "Tank Girl (1995)", ""));
			movies.add(new Movie("3608", "Pee-wee's Big Adventure (1985)", ""));
		}
	
	@Constraints.Required
	public String id;
	@Constraints.Required
	public String name;
	public String description;
	
	public Movie() {}
	
	public Movie(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	public String toString() {
		return String.format("%s - %s", id, name);
	}

	public static List<Movie> findAll() {
		return new ArrayList<Movie>(movies);
	}
	
	public static Movie findById(String id) {
		for (Movie candidate : movies) {
			if (candidate.id.equals(id)) {
				return candidate;
			}
		}
		return null;
	}
			
	public static List<Movie> findByName(String term) {
		final List<Movie> results = new ArrayList<Movie>();
		for (Movie candidate : movies) {
			if (candidate.name.toLowerCase().contains(term.toLowerCase())) {
				results.add(candidate);
			}
		}
		return results;
	}
	
	public static boolean remove(Movie movie) {
		return movies.remove(movie);
	}
	
	public static void add(Movie movie) {
		movies.add(movie);
	}
	
	public void save() {
		movies.remove(findById(this.id));
		movies.add(this);
	}

}