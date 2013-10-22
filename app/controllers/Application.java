package controllers;

import play.*;
import play.mvc.*;
import models.Movie;
import models.User;
import models.Recommendation;
import play.data.*;
import static play.data.Form.*;
import views.html.*;
import com.avaje.ebean.*;
import java.util.*;
import models.algorithms.*;
import models.algorithms.helpers.*;

public class Application extends Controller {
  
  

  // Login
  
  public static Result login() {
    if (request().method() == "POST") {
      Form<Login> loginForm = form(Login.class).bindFromRequest();
      if (loginForm.hasErrors()) {
        flash("error", String.format("User %s does not exist", loginForm.field("email").value()));
        return badRequest(login.render(loginForm));
      }
      session().clear();
      User user = User.findByEmail(loginForm.get().email);
      session("userId", String.valueOf(user.id));
      return redirect(routes.Experiment.handle_get());
    } else {
      return ok(login.render(form(Login.class)));
    }
  }
  
  public static class Login {
    public String email;

    public String validate() {
      if (User.authenticate(email) == null) {
        return "Invalid email";
      }
      return null;
    }
  }
  
  // Logout
  
  public static Result logout() {
    session().clear();
    flash("success", "You've been logged out");
    return redirect(routes.Experiment.handle_get());
  }
  
  // Register
  
  public static Result register() {
    if (request().method() == "POST") {
      Form<Register> registerForm = form(Register.class).bindFromRequest();
      String email = registerForm.field("email").value();
      if (registerForm.hasErrors()) {
        flash("error", String.format("User %s already exists.", email));
        return badRequest(register.render(registerForm));
      }
      User user = new User(email);
      user.save();
      if (user.experimentGroup == 1) {
        user.state = "110";
      } else {
        user.state = "120";
      }
      user.update();
      user = User.find.where().eq("email", email).findUnique();
      flash("success", String.format("Successfully created user %s", user.email));
      session().clear();
      session("userId", String.valueOf(user.id));
      return redirect(routes.Experiment.handle_get());
    }
    return ok(register.render(form(Register.class)));
  }
  
  public static class Register {
    public String email;

    public String validate() {
      System.out.println("Register.validate called");
      if (User.authenticate(email) != null) {
        System.out.println("User.authenticate != null");
        return "Email already exists";
      }
      return null;
    }
  }
  
  
  // Update for second stage
  
  public static Result updateToNextStage() {
    // build UP model
    // write similarities to db
    // write kmatrix to db
		List<Preference> prefs = UP.loadMLPreferences();
		List<BinaryPreference> comps = UP.loadComparisons();
		UP up = new UP(prefs, comps);
		up.initialize();
		
    // make 10 best recommendations
    // write them to DB
    for (User user : User.find.where().eq("experimentGroup", 2).findList()) {
      List<Integer> list = UP.predictRankingListFromDB(user.id);
      for (int i = 0; i < list.size(); i++) {
        Movie movie = Movie.find.byId(list.get(i));
        Recommendation rec = Recommendation.create(user, movie, i+1, false);
        System.out.println(rec);
      }
    }
    
    User.updateAllUserStates();
    return ok();
  }
  
}
