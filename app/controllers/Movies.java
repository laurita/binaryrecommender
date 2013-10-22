package controllers;

import views.html.movies.*;
import java.util.*;
import models.*;
import play.*;
import play.mvc.*;

public class Movies extends Controller {
	
	public static Result details(int id) {
		final Movie movie = Movie.findById(id);
		if (movie == null) {
			return notFound(String.format("Movie %s does not exist.", id));
		}
		return ok(details.render(movie));
	}
}