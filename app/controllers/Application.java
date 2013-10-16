package controllers;

import play.*;
import play.mvc.*;
import models.*;
import play.data.*;
import static play.data.Form.*;
import views.html.*;
import views.html.movies.*;
import com.avaje.ebean.*;
import java.util.*;

public class Application extends Controller {
	
	public static int stage = 1;
		
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
	
	public static class AnswerQuestions {
		public String q1Answer;
		public String q2Answer;
		public String q3Answer;
		public String q4Answer;
	}
	
	public static Result register() {
		return ok(register.render(form(Register.class)));			
	}
	
	@Security.Authenticated(Secured.class)
	public static Result index() {
		User user = User.find.byId(session().get("userId"));
		System.out.println("user is " + user);
		if (user == null) {
			return redirect(routes.Application.login());
		} else {
			if (stage == 1) {
				if (!user.stage1Done) {
					return ok(about.render());
				} else {
					return ok(wait.render());
				}
			} else if (stage == 2) {
				if (user.stage2Done) {
					return ok(finish.render());
				} else if (user.stage1Done) {
					return ok(start2Stage.render());
				} else {
					return ok(stage1Unfinished.render());
				}
			}
		}
		return ok();
	}
	
	public static Result answerQuestions() {
		return ok(answerQuestions.render(form(AnswerQuestions.class)));
	}
	
	public static Result submitAnswers() {
		Form<AnswerQuestions> answerForm = form(AnswerQuestions.class).bindFromRequest();
		if (session().get("userId") != null) {
			User user = User.find.byId(session().get("userId"));
			user.stage1Done = true;
			user.question1 = answerForm.field("likertScaleRadios1").value();
			user.question2 = answerForm.field("likertScaleRadios2").value();
			user.question3 = answerForm.field("likertScaleRadios3").value();
			user.question4 = answerForm.field("likertScaleRadios4").value();
			user.update();
		}	else {
			return redirect(routes.Application.login());
		}
		return ok(wait.render());
	}
	
	public static Result submit() {
		Form<Register> registerForm = form(Register.class).bindFromRequest();
		String email = registerForm.field("email").value();
		if (registerForm.hasErrors()) {
			flash("error", String.format("User %s already exists.", email));
			return badRequest(register.render(registerForm));
		} else {
			User user = new User(email);
			user.save();
			user = User.find.where().eq("email", email).findUnique();
			flash("success", String.format("Successfully created user %s", user.email));
			session().clear();
			session("userId", String.valueOf(user.id));
			return redirect(routes.Application.index());
		}
	}
	
	public static Result startExperiment() {
		User user = User.find.byId(session().get("userId"));
		switch (user.experimentGroup) {
			case 1: return redirect(routes.Movies.list());
			case 2: return redirect(routes.Movies.preferences());
			default: return redirect(routes.Application.login());
		}
	}
	
	public static Result start2Stage() {
		User user = User.find.byId(session().get("userId"));
		return redirect(routes.Recommender.recommend());
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
			User user = User.findByEmail(loginForm.get().email);
			session("userId", String.valueOf(user.id));
			return redirect(routes.Application.index());
		}
	}
	
	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(
			Routes.javascriptRouter("jsRoutes", controllers.routes.javascript.Users.rateMovie())
		);
	}

}
