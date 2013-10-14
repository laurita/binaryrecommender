package controllers;

import views.html.MF.*;
import views.html.movies.*;
import java.util.*;
import play.*;
import models.User;
import models.Movie;
import models.Recommendation;
import play.mvc.*;
import com.avaje.ebean.*;
import models.algorithms.*;
import models.algorithms.helpers.*;
import static play.data.Form.*;
import play.data.*;

public class MFController extends Controller {
	
	@Security.Authenticated(Secured.class)
	public static Result recommend() {
		List<Preference> prefs = MF.addNewPreferencesToList(MF.loadMLPreferences());
		MF mf = new MF(prefs);
		mf.initialize();
		String userId = session().get("userId");
		User user = User.find.byId(userId);
		List<Integer> unratedMovieIds = user.getUnratedMovieIds();
		List<Integer> mfRankings = mf.rank(user.id, unratedMovieIds).subList(0, 10);
		List<Movie> bestMovies = new ArrayList<Movie>();
		for (int i = 0; i <= 9; i++) {
			Movie movie = Movie.find.byId(mfRankings.get(i));
			Recommendation rec = Recommendation.create(user, movie, i+1);
			bestMovies.add(movie);
		}
		System.out.println(bestMovies);
		return ok(recommend.render(bestMovies, user));
	}
	
	@Security.Authenticated(Secured.class)
	public static Result update() {
					
		Map<String, String[]> map = request().body().asFormUrlEncoded();
		System.out.println(map);
		System.out.println(map.keySet());
		String[] goodOnes = map.get("good");
		String[] seenOnes = map.get("seen");
		
		System.out.println("goodOnes");
		System.out.println(Arrays.toString(goodOnes));
		
		System.out.println("seenOnes");		
		System.out.println(Arrays.toString(seenOnes));
			
		List<Integer> good = new ArrayList<Integer>();
		List<Integer> seen = new ArrayList<Integer>();
		
		if (goodOnes != null) {
			for (String g : goodOnes) {
				good.add(Integer.parseInt(g));
			}
		}
		if (seenOnes != null) {
			for (String s : seenOnes) {
				seen.add(Integer.parseInt(s));
			}
		}
	
		User user = User.find.byId(session().get("userId"));
		
		List<Recommendation> recs = Recommendation.find.fetch("movie")
			.where().eq("user", user).findList();
			
		//TODO: write good recommendations to db
		//TODO: make a list of seen movies to rate
		for (Recommendation rec : recs) {
			Movie m = rec.movie;
			rec.good = good.contains(m.id);
			rec.seen = seen.contains(m.id);
		}
		
		List<Movie> seenMovies = new ArrayList<Movie>();
		if (seen.size() != 0) {
			seenMovies = Movie.find.where()
			.idIn(seen).orderBy("logpopvar desc").findList();
			
			//TODO: redirect to rate movies list with small list
			return ok(list.render(seenMovies, user));
		}
		
		//TODO: redirect to questions with the same list (skip list comparison)
		return ok();
	}
}