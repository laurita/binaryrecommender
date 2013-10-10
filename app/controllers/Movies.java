package controllers;

import views.html.movies.*;
import java.util.*;
import models.*;
import play.*;
import play.mvc.*;

public class Movies extends Controller {
	
	@Security.Authenticated(Secured.class)
	public static Result list() {
		List<Movie> movies = Movie.findAll();
		User user = User.find.byId(session().get("userId"));
		return ok(list.render(movies, user));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result preferences() {
		List<List<String>> moviePairs = Movie.selectBestMoviePairs(30);
		User user = User.find.byId(session().get("userId"));
		return ok(preferences.render(moviePairs, user));
	}
	
	public static Result details(int id) {
		final Movie movie = Movie.findById(id);
		if (movie == null) {
			return notFound(String.format("Movie %s does not exist.", id));
		}
		return ok(details.render(movie));
	}
}