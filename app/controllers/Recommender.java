package controllers;

import views.html.recommender.*;
import views.html.movies.*;
import java.util.*;
import play.*;
import models.User;
import models.Rating;
import models.Movie;
import models.Recommendation;
import play.mvc.*;
import com.avaje.ebean.*;
import models.algorithms.*;
import models.algorithms.helpers.*;
import static play.data.Form.*;
import play.data.*;

public class Recommender extends Controller {
	
	@Security.Authenticated(Secured.class)
	public static Result recommend() {
		User user = User.find.byId(session().get("userId"));
		switch (user.experimentGroup) {
			case 1: {
				List<Preference> prefs = MF.addNewPreferencesToList(MF.loadMLPreferences());
				MF mf = new MF(prefs);
				mf.initialize();
				List<Integer> unratedMovieIds = user.getUnratedMovieIds();
				List<Integer> mfRankings = mf.rank(user.id, unratedMovieIds).subList(0, 10);
				boolean updated = user.afterUpdate;
				System.out.println("afterUpdate "+ updated);
				List<Movie> bestMovies = new ArrayList<Movie>();
				for (int i = 0; i <= 9; i++) {
					Movie movie = Movie.find.byId(mfRankings.get(i));
					
					Recommendation rec = Recommendation.create(user, movie, i+1, updated);
					bestMovies.add(movie);
					System.out.println(rec);
				}
				System.out.println(bestMovies);
				return ok(recommend.render(bestMovies, user));
			}
			case 2: {
				//TODO: do the same with UP model
				return ok();
			}
			default: return ok();
		}
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
		System.out.println("good");
		System.out.println(good);
		System.out.println("seen");
		System.out.println(seen);
	
		User user = User.find.byId(session().get("userId"));
		
		List<Recommendation> recs = Recommendation.find.fetch("movie")
			.where().eq("user", user).eq("updated", false).findList();
		System.out.println(recs);
			
		//TODO: write good recommendations to db
		//TODO: make a list of seen movies to rate
		System.out.println("updating");
		for (Recommendation rec : recs) {
			Movie m = rec.movie;
			rec.good = good.contains(m.id);
			rec.seen = seen.contains(m.id);
			rec.update();
			System.out.println(rec);
		}
		
		System.out.println(recs);
		
		if (user.experimentGroup == 1) {
			if (seen.size() != 0) {
				List<Movie> seenMovies = Movie.find.where()
					.idIn(seen).orderBy("logpopvar desc").findList();
			
				user.afterUpdate = true;
				user.update();
				
				//TODO: redirect to rate movies list with small list
				return ok(list.render(seenMovies, user));
			} else {
				//TODO: redirect to questions with the 1st list (skip list comparison)
				return ok();
			}
		}
		
		if (user.experimentGroup == 2) {
			if (seen.size() > 1) {
				List<List<Integer>> moviePairs = Movie.selectMoviePairsInList(seen);
				
				user.afterUpdate = true;
				user.update();
				
				//TODO: redirect to compare movie pairs with small list
				return ok(preferences.render(moviePairs, user));
			} else {
				//TODO: redirect to questions with the 1st list (skip list comparison)
				return ok();
			}
			
		}
		return ok();
	}
	
	@Security.Authenticated(Secured.class)
	public static Result compare() {
		User user = User.find.byId(session().get("userId"));
		List<Recommendation> firstList = Recommendation.find.fetch("movie")
			.where().eq("user", user).eq("updated", false).findList(); 
		List<Recommendation> secondList = Recommendation.find.fetch("movie")
			.where().eq("user", user).eq("updated", true).findList(); 
		return ok(compare.render(firstList, secondList, user));
		
	}
	
	@Security.Authenticated(Secured.class)
	public static Result submitComparisons() {
		return ok();
	}
}