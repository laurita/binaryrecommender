package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.movies.list;
import views.html.movies.details;
import java.util.List;
import models.Movie;

public class Movies extends Controller {
	
	public static Result list() {
		List<Movie> movies = Movie.findAll();
		return ok(list.render(movies));
	}
	
	public static Result details(String id) {
		final Movie movie = Movie.findById(id);
		if (movie == null) {
			return notFound(String.format("Movie %s does not exist.", id));
		}
		return ok(details.render(movie));
	}
}