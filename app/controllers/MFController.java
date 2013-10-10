package controllers;

import views.html.MF.*;
import java.util.*;
import play.*;
import models.User;
import models.Movie	;
import play.mvc.*;
import com.avaje.ebean.*;
import models.algorithms.*;
import models.algorithms.helpers.*;

public class MFController extends Controller {
	
	@Security.Authenticated(Secured.class)
	public static Result recommend() {
		List<Preference> prefs = MF.addNewPreferencesToList(MF.loadMLPreferences());
		MF mf = new MF(prefs);
		mf.initialize();
		String userId = session().get("userId");
		User user = User.find.byId(userId);
		List<Integer> unratedMovieIds = user.getUnratedMovieIds();
		Set<Integer> mfRankings = mf.rank(user.id, unratedMovieIds);
		List<Movie> movies = new ArrayList<Movie>();
		for (Integer i : mfRankings) {
			movies.add(Movie.find.byId(i));
		}
		return ok(recommend.render(movies.subList(0, 10), user));
	}
}