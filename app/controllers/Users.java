package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import java.util.List;
import models.*;
import play.mvc.BodyParser;
import play.libs.Json;
import play.libs.Json.*;                        
import static play.libs.Json.toJson;
import com.fasterxml.jackson.databind.JsonNode;           
import com.fasterxml.jackson.databind.node.ObjectNode;


public class Users extends Controller {
	
	public static Result rateMovie() {
		if (request().method() == "POST") {
			int value = Integer.parseInt(request().body().asFormUrlEncoded().get("rating")[0]);
      int userId = Integer.parseInt(session().get("userId"));
  		User user = User.find.byId(userId);
			Movie movie = Movie.find.byId(Integer.parseInt(request().body().asFormUrlEncoded().get("movieId")[0]));
			Rating r = Rating.find.where().eq("user", user).eq("movie", movie).findUnique();
			if (r != null) {
				r.value = value;
				r.save();
			} else {
				Rating rating = Rating.create(user, movie, value);
			}
		}
		return ok();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result addPreferences() {
		if (request().method() == "POST") {
      int userId = Integer.parseInt(session().get("userId"));
  		User user = User.find.byId(userId);
			if (user != null) {
				JsonNode jsonPrefs = request().body().asJson().get("prefs");
				for(JsonNode jsonPref : jsonPrefs) {
					Movie movie1 = Movie.find.byId(jsonPref.get("movie1").asInt());
					Movie movie2 = Movie.find.byId(jsonPref.get("movie2").asInt());
					int value = jsonPref.get("value").asInt();
					Preference pref = Preference.create(user, movie1, movie2, value);
				}
			}			
		}
		return ok();
	}
	
}