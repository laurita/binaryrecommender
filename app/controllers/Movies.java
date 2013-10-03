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
		List<List<String>> moviePairs = new ArrayList<List<String>>();
		List<String> l1 = new ArrayList<String>();
		l1.add("2710");
		l1.add("231");
		moviePairs.add(l1);
		List<String> l2 = new ArrayList<String>();
		l2.add("2700");
		l2.add("2712");
		moviePairs.add(l2);
		List<String> l3 = new ArrayList<String>();
		l3.add("2427");
		l3.add("1721");
		moviePairs.add(l3);
		User user = User.find.byId(session().get("userId"));
		return ok(preferences.render(moviePairs, user));
	}
	
	public static Result details(String id) {
		final Movie movie = Movie.findById(id);
		if (movie == null) {
			return notFound(String.format("Movie %s does not exist.", id));
		}
		return ok(details.render(movie));
	}
}