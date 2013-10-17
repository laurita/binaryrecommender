package controllers;

import views.html.recommender.*;
import views.html.movies.*;
import views.html.*;
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
	
	public static class Comparisons {
		public int q1Answer;
		public int q2Answer;
		public int q3Answer;
		public int q4Answer;
		public int q5Answer;
		public int q6Answer;
		public int q7Answer;
		public int q8Answer;
		public int q9Answer;
		public int q10Answer;
		public int q11Answer;
		public int q12Answer;
		public int q13Answer;
		public int q14Answer;
	}
	
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
				List<Preference> prefs = UP.loadMLPreferences();
				List<BinaryPreference> comps = UP.loadComparisons();
				UP up = new UP(prefs, comps);
				up.initialize();
				boolean signCorrected = false;
				up.calculateKMatrix(user.id, signCorrected);
				up.updateKMatrixWithPrefs(user.id, signCorrected);
				List<Integer> unpreferedMovieIds = user.getUnpreferedMovieIds();
				List<Integer> list = up.predictRankingList(user.id, unpreferedMovieIds, signCorrected).subList(0, 10);
				boolean updated = user.afterUpdate;
				System.out.println("afterUpdate "+ updated);
				List<Movie> bestMovies = new ArrayList<Movie>();
				for (int i = 0; i <= 9; i++) {
					Movie movie = Movie.find.byId(list.get(i));
					
					Recommendation rec = Recommendation.create(user, movie, i+1, updated);
					bestMovies.add(movie);
					System.out.println(rec);
				}
				System.out.println("10 best movies");
				System.out.println(bestMovies);
				return ok(recommend.render(bestMovies, user));
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
			.where().eq("user", user).eq("updated", user.afterUpdate).findList();
			
		// write good recommendations to db
		// make a list of seen movies to rate
		System.out.println("updating");
		for (Recommendation rec : recs) {
			Movie m = rec.movie;
			rec.good = good.contains(m.id);
			rec.seen = seen.contains(m.id);
			rec.update();
			System.out.println(rec);
		}
				
		if (user.experimentGroup == 1) {
			if (seen.size() != 0) {
				List<Movie> seenMovies = Movie.find.where()
					.idIn(seen).orderBy("logpopvar desc").findList();
			
				user.afterUpdate = true;
				user.update();
				
				// redirect to rate movies list with small list
				return ok(list.render(seenMovies, user));
			} else {
				if (user.afterUpdate) {
					return redirect(routes.Recommender.compare());
				}
				//TODO: redirect to questions with the 1st list (skip list comparison)
				return ok();
			}
		}
		
		if (user.experimentGroup == 2) {
			if (seen.size() > 1) {
				List<List<Integer>> moviePairs = Movie.selectMoviePairsInList(seen);
				
				user.afterUpdate = true;
				user.update();
				
				// redirect to compare movie pairs with small list
				return ok(preferences.render(moviePairs, user));
			} else {
				if (user.afterUpdate) {
					return redirect(routes.Recommender.compare());
				}
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
			.where().eq("user", user).eq("updated", false).orderBy("rank").findList(); 
		List<Recommendation> secondList = Recommendation.find.fetch("movie")
			.where().eq("user", user).eq("updated", true).orderBy("rank").findList(); 
		return ok(views.html.recommender.compare.render(firstList, secondList, user));
		
	}
	
	@Security.Authenticated(Secured.class)
	public static Result submitComparisons() {
		Form<Comparisons> compareForm = form(Comparisons.class).bindFromRequest();
		if (session().get("userId") != null) {
			User user = User.find.byId(session().get("userId"));
			int listNr = 1;
			for (int i = 1; i <= 14; i++) {
				if (i <= 7) { listNr = 1; } else { listNr = 2; };
				int answer = Integer.parseInt(compareForm.field("likertScaleRadios"+ i).value());
				int question = (i - 1) % 7 + 1;
				String sqlString = String.format(
					"insert into comparisons values (%d, %d, %d, %d)", user.id, question, listNr, answer
				);
				SqlUpdate update = Ebean.createSqlUpdate(sqlString);
				int modifiedCount = Ebean.execute(update);
 			 	String msg = "There where " + modifiedCount + "rows updated";
				System.out.println(msg); 
				user.stage2Done = true;
				user.update();
			}
		}	else {
			return redirect(routes.Application.login());
		}
		return ok(finish.render());
	}
}