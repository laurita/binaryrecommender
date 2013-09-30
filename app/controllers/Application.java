package controllers;

import play.*;
import play.mvc.*;
import models.*;
import play.data.*;
import static play.data.Form.*;
import views.html.*;
import com.avaje.ebean.*;

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

	public static class Register {
		public String email;

		public String validate() {
			if (User.authenticate(email) != null) {
				return "Email already exists";
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
	
	public static Result register() {
		return ok(register.render(form(Register.class)));			
	}
	
	public static Result submit() {
		Form<Register> registerForm = form(Register.class).bindFromRequest();
		String email = registerForm.field("email").value();
		if (registerForm.hasErrors()) {
			flash("error", String.format("User %s could not be saved. Maybe the user with such email already exists.", email));
			return badRequest(register.render(registerForm));
		} else {
			Ebean.save(new User(email));
			User user = User.find.where().eq("email", email).findUnique();
			flash("success", String.format("Successfully created user %s", user.email));
			session().clear();
			session("email", email);
			return redirect(routes.Application.index());
		}
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
			flash("error", String.format("User %s does not exist", loginForm.field("email").value()));
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
