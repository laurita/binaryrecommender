package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import java.util.List;
import models.Rating;
import views.html.ratings.list;

public class Ratings extends Controller {
	
	public static Result list() {
		List<Rating> ratings = Rating.findAll();
		return ok(list.render(ratings));
	}
}