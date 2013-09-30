package controllers;

import play.*;
import play.mvc.*;
import models.*;
import play.data.*;
import static play.data.Form.*;
import views.html.*;

public class Application extends Controller {
	
	public static class Login {
		public String email;

		public String validate() {
			if (User.authenticate(email) == null) {
				return "Invalid email";
			}
			return null;
		}
	}

	@Security.Authenticated(Secured.class)
	public static Result index() {
		return ok(
			index.render("Index")
				);
	}
		
	public static Result login() {
		return ok(login.render(form(Login.class)));
	}
	
	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(
			routes.Application.login()
		);
	}
	
	public static Result authenticate() {

		Form<Login> loginForm = form(Login.class).bindFromRequest();
		
		if (loginForm.hasErrors()) {
			return badRequest(login.render(loginForm));
		} else {
			session().clear();
			session("email", loginForm.get().email);

			return redirect(
				routes.Application.index()
					);
				
		}
	}

}