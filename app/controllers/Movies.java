package controllers;

import views.html.movies.list;
import views.html.movies.details;
import java.util.List;
import models.*;
import play.*;
import play.mvc.*;

public class Movies extends Controller {
	
	@Security.Authenticated(Secured.class)
	public static Result list() {
		List<Movie> movies = Movie.findAll();
		User user = User.find.byId(session().get("userId"));
		System.out.println("session userId " + session().get("userId"));
		System.out.println("user " + user);
		return ok(list.render(movies, user));
	}
	
	public static Result details(String id) {
		final Movie movie = Movie.findById(id);
		if (movie == null) {
			return notFound(String.format("Movie %s does not exist.", id));
		}
		return ok(details.render(movie));
	}
}