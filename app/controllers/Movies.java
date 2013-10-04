package controllers;

import views.html.movies.*;
import java.util.*;
import models.*;
import play.*;
import play.mvc.*;
import com.avaje.ebean.*;

public class Movies extends Controller {
	
	@Security.Authenticated(Secured.class)
	public static Result list() {
		List<Movie> movies = Movie.findAll();
		User user = User.find.byId(session().get("userId"));
		return ok(list.render(movies, user));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result preferences() {
		String sql = "select movie1_id, movie2_id from moviePairs";
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> list = sqlQuery.findList();
		List<List<String>> moviePairs = new ArrayList<List<String>>();
		for (SqlRow row : list) {
			List<String> l = new ArrayList<String>();
			l.add(row.getString("movie1_id"));
			l.add(row.getString("movie2_id"));
			moviePairs.add(l);
		}
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