package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.users.list;
import views.html.users.details;
import java.util.List;
import models.*;

public class Users extends Controller {
	
	public static Result list() {
		List<User> users = User.find.all();
		return ok(list.render(users));
	}
	
	public static Result details(String email) {
		final User user = User.findByEmail(email);
		if (user == null) {
			return notFound(String.format("User %s does not exist.", email));
		}
		return ok(details.render(user));
	}
	
	public static Result rateMovie() {
		if (request().method() == "POST") {
			int value = Integer.parseInt(request().body().asFormUrlEncoded().get("rating")[0]);
			User user = User.find.byId(session().get("userId"));
			Movie movie = Movie.find.byId(request().body().asFormUrlEncoded().get("movieId")[0]);
			Rating r = Rating.find.where().eq("user", user).eq("movie", movie).findUnique();
			if (r != null) {
				r.value = value;
				r.save();
			} else {
				Rating rating = Rating.create(user, movie, value);
			}
			System.out.println("user.ratings: ");
			for (Rating it : user.getRatings()) {
				System.out.println("user: "+ it.user.email +", movie: "+ it.movie.title +", rating: "+ it.value);
			} 
			/**
			System.out.println("movie.ratings: ");
			for (Rating it : movie.getRatings()) {
				System.out.println("user: "+ it.user.email +", movie: "+ it.movie.title +", rating: "+ it.value);
			} 
			System.out.println("Rating.list(): ");
			for (Rating it : Rating.find.all()) {
				System.out.println("user: "+ it.user.email +", movie: "+ it.movie.title +", rating: "+ it.value);
			} 
			**/
		}
		return ok();
	}
	
}