package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.users.list;
import views.html.users.details;
import java.util.List;
import models.User;

public class Users extends Controller {
	
	public static Result list() {
		List<User> users = User.findAll();
		return ok(list.render(users));
	}
	
	public static Result details(String email) {
		final User user = User.findByEmail(email);
		if (user == null) {
			return notFound(String.format("User %s does not exist.", email));
		}
		return ok(details.render(user));
	}
	
}