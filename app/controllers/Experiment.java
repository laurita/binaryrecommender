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
import java.lang.reflect.Method;
import java.lang.InstantiationException;
import play.mvc.BodyParser;
import play.libs.Json;
import play.libs.Json.*;                        
import static play.libs.Json.toJson;
import com.fasterxml.jackson.databind.JsonNode;           
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.algorithms.*;
//import models.algorithms.helpers.*;

public class Experiment extends Controller {
	
  // GET request actions
  
  @Security.Authenticated(Secured.class)
  public static Result handle_get() {
    System.out.println("handle_get");
    // Check if user is logged in
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    if (user != null) {
      String userState = user.state;
    
      try {
        Class expClass = Class.forName("controllers.Experiment");
        Class[] noparams = new Class[]{};
        System.out.println("handle_get_" + userState);
        Method method = expClass.getMethod("handle_get_" + userState, noparams);
        Result res = (Result) method.invoke(null);
        return res;      
      }
      catch (Exception x) {
        x.printStackTrace();
      }
    }
    return redirect(routes.Application.logout());
  }
  
  public static Result handle_get_0() {
    return ok(tpl_000.render());
  }
  
  public static Result handle_get_110() {
    return ok(tpl_110.render());
  }
  
  public static Result handle_get_120() {
    return ok(tpl_120.render());
  }

  public static Result handle_get_111() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<Movie> movies = Movie.find.orderBy("logpopvar desc").findList();
    return ok(tpl_111.render(movies, user));
  }
  
  public static Result handle_get_121() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<List<Integer>> moviePairs = Movie.selectBestMoviePairs(4950);
    return ok(tpl_121.render(moviePairs, user));
  }
  
  public static Result handle_get_112() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    return ok(tpl_112.render(form(AnswerQuestions.class)));
  }
  
  public static Result handle_get_122() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    return ok(tpl_122.render(form(AnswerQuestions.class)));
  }
  
  public static Result handle_get_113() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    return ok(tpl_113.render());
  }
  
  public static Result handle_get_123() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    return ok(tpl_123.render());
  }
  
  public static Result handle_get_210() {
    return ok(tpl_210.render());
  }
  
  public static Result handle_get_220() {
    return ok(tpl_220.render());
  }
  
  public static Result handle_get_211() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<Recommendation> recs = Recommendation.find
      .fetch("movie").where()
        .eq("user", user).eq("updated", false)
          .orderBy("rank asc").findList();
    return ok(tpl_211.render(recs, user));
  }
  
  public static Result handle_get_221() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<Recommendation> recs = Recommendation.find
      .fetch("movie").where()
        .eq("user", user).eq("updated", false)
          .orderBy("rank asc").findList();
    return ok(tpl_221.render(recs, user));
  }
  
  public static Result handle_get_212() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<Movie> seenMoviesFromRecList = user.getSeenMoviesRecommendedOriginally();
    return ok(tpl_212.render(seenMoviesFromRecList, user));
  }
  
  public static Result handle_get_222() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<List<Integer>> seenPairsFromRecList = user.getSeenMoviePairsRecommendedOriginally();
    return ok(tpl_222.render(seenPairsFromRecList, user));
  }
  
  public static Result handle_get_213() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<Recommendation> recs = Recommendation.find
      .fetch("movie").where()
        .eq("user", user).eq("updated", true)
          .orderBy("rank asc").findList();
    for (Recommendation rec : recs) {
      System.out.println(rec);
    }
    return ok(tpl_213.render(recs, user));
  }
  
  public static Result handle_get_223() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    List<Recommendation> recs = Recommendation.find
      .fetch("movie").where()
        .eq("user", user).eq("updated", true)
          .orderBy("rank asc").findList();
    for (Recommendation rec : recs) {
      System.out.println(rec);
    }
    return ok(tpl_223.render(recs, user));
  }
  
  public static Result handle_get_214() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    
    List<Recommendation> firstList = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", false).orderBy("rank").findList(); 
    List<Recommendation> secondList = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", true).orderBy("rank").findList(); 
    
    return ok(tpl_214.render(firstList, secondList, user));
  }
  
  public static Result handle_get_224() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    
    List<Recommendation> firstList = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", false).orderBy("rank").findList(); 
    List<Recommendation> secondList = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", true).orderBy("rank").findList(); 
    
    return ok(tpl_224.render(firstList, secondList, user));
  }
  
  public static Result handle_get_215() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    
    return ok(tpl_215.render());
  }
  
  public static Result handle_get_225() {
    String userIdFromSession = session().get("userId");
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    
    return ok(tpl_225.render());
  }
  
  // POST request actions
    
  public static Result handle_post() {
    System.out.println("handle_post");
    String userIdFromSession = session().get("userId");
    if (userIdFromSession == null) {
      return redirect(routes.Application.register());
    }
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    if (user != null) {
      String userState = user.state;
    
      try {
        Class expClass = Class.forName("controllers.Experiment");
        Class[] noparams = new Class[]{};
        System.out.println("handle_post_" + userState);
        Method method = expClass.getMethod("handle_post_" + userState, noparams);
        Result res = (Result) method.invoke(null);
        return res;      
      }
      catch (Exception x) {
        x.printStackTrace();
      }
    }    
    return redirect(routes.Application.logout());
  }
  
  public static Result handle_post_110() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    user.state = "111";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_120() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    user.state = "121";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_111() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    user.state = "112";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_121() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    user.state = "122";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_112() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    Form<AnswerQuestions> answerForm = form(AnswerQuestions.class).bindFromRequest();
    user.stage1Done = true;
    user.question1 = Integer.parseInt(answerForm.field("likertScaleRadios1").value());
    user.question2 = Integer.parseInt(answerForm.field("likertScaleRadios2").value());
    user.question3 = Integer.parseInt(answerForm.field("likertScaleRadios3").value());
    user.question4 = Integer.parseInt(answerForm.field("likertScaleRadios4").value());		
    user.state = "113";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_122() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    Form<AnswerQuestions> answerForm = form(AnswerQuestions.class).bindFromRequest();
    user.stage1Done = true;
    user.question1 = Integer.parseInt(answerForm.field("likertScaleRadios1").value());
    user.question2 = Integer.parseInt(answerForm.field("likertScaleRadios2").value());
    user.question3 = Integer.parseInt(answerForm.field("likertScaleRadios3").value());
    user.question4 = Integer.parseInt(answerForm.field("likertScaleRadios4").value());		
    user.state = "123";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_210() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    // create MF model and write recommendation list to db
    MF mf = new MF(MF.addNewPreferencesToList(MF.loadMLPreferences()));
    mf.initialize();
    List<Integer> unratedMovieIds = user.getUnratedMovieIds();
    List<Integer> mfRankings = mf.rank(user.id, unratedMovieIds).subList(0, 10);
    for (int i = 0; i < mfRankings.size(); i++) {
      Movie movie = Movie.find.byId(mfRankings.get(i));
      Recommendation rec = Recommendation.create(user, movie, i+1, false);
      System.out.println(rec);
    }
    user.state = "211";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_220() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    // ratings for UP model are already prepared in DB
    
    user.state = "221";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
    
  public static Result handle_post_211() {
    
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
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

    List<Recommendation> recs = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", false).findList();
    
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
    
    user.state = "212";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_221() {
    
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
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

    List<Recommendation> recs = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", false).findList();
    
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
    
    user.state = "222";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_212() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    // create MF model and write recommendation list to db
    MF mf = new MF(MF.addNewPreferencesToList(MF.loadMLPreferences()));
    mf.initialize();
    List<Integer> unratedMovieIds = user.getUnratedMovieIds();
    List<Integer> mfRankings = mf.rank(user.id, unratedMovieIds).subList(0, 10);
    for (int i = 0; i <= 9; i++) {
      Movie movie = Movie.find.byId(mfRankings.get(i));
      Recommendation rec = Recommendation.create(user, movie, i+1, true);
      System.out.println(rec);
    }
    user.state = "213";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_222() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    // get new preferences from db
    // update kmatrix by adding new preferences
    // write new recommendation list to db
    
    List<Preference> newPrefs = Preference.find.where()
      .eq("user", user).eq("additional", true).findList();
    
    for (Preference pref : newPrefs) {
        UP.updateKValueInDB(pref.user.id, pref.movie1.id, pref.movie2.id, (float)pref.value, 1f);
    }
    
    List<Integer> list = UP.predictRankingListFromDB(user.id);
    for (int i = 0; i < list.size(); i++) {
      Movie movie = Movie.find.byId(list.get(i));
      Recommendation rec = Recommendation.create(user, movie, i+1, true);
      System.out.println(rec);
    }
    
    user.state = "223";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_213() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    Map<String, String[]> map = request().body().asFormUrlEncoded();
    System.out.println(map);
    System.out.println(map.keySet());
    String[] goodOnes = map.get("good");
			
    List<Integer> good = new ArrayList<Integer>();
		
    if (goodOnes != null) {
      for (String g : goodOnes) {
        good.add(Integer.parseInt(g));
      }
    }

    List<Recommendation> recs = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", true).findList();
    
    // write good recommendations to db
    // make a list of seen movies to rate
    System.out.println("updating");
    for (Recommendation rec : recs) {
      Movie m = rec.movie;
      rec.good = good.contains(m.id);
      rec.update();
      System.out.println(rec);
    }
    
    user.state = "214";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_223() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    Map<String, String[]> map = request().body().asFormUrlEncoded();
    System.out.println(map);
    System.out.println(map.keySet());
    String[] goodOnes = map.get("good");
			
    List<Integer> good = new ArrayList<Integer>();
		
    if (goodOnes != null) {
      for (String g : goodOnes) {
        good.add(Integer.parseInt(g));
      }
    }

    List<Recommendation> recs = Recommendation.find.fetch("movie")
      .where().eq("user", user).eq("updated", true).findList();
    
    // write good recommendations to db
    // make a list of seen movies to rate
    System.out.println("updating");
    for (Recommendation rec : recs) {
      Movie m = rec.movie;
      rec.good = good.contains(m.id);
      rec.update();
      System.out.println(rec);
    }
    
    user.state = "224";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_214() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    Form<Comparisons> compareForm = form(Comparisons.class).bindFromRequest();
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
      //System.out.println(msg); 
      user.stage2Done = true;
      user.update();
    }
    
    user.state = "215";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
  
  public static Result handle_post_224() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    
    Form<Comparisons> compareForm = form(Comparisons.class).bindFromRequest();
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
      //System.out.println(msg); 
      user.stage2Done = true;
      user.update();
    }
    
    user.state = "225";
    user.update();
    return redirect(routes.Experiment.handle_get());
  }
    
  public static Result handle_ajax() {
    System.out.println("handle_ajax");
    String userIdFromSession = session().get("userId");
    if (userIdFromSession == null) {
      return redirect(routes.Application.register());
    }
    int userId = Integer.parseInt(userIdFromSession);
    User user = User.find.byId(userId);
    if (user != null) {
      String userState = user.state;
    
      try {
        Class expClass = Class.forName("controllers.Experiment");
        Class[] noparams = new Class[]{};
        System.out.println("handle_ajax_" + userState);
        Method method = expClass.getMethod("handle_ajax_" + userState, noparams);
        Result res = (Result) method.invoke(null);
        return res;      
      }
      catch (Exception x) {
        x.printStackTrace();
      }
    }    
    return ok();
  }
  
  @BodyParser.Of(BodyParser.Json.class)
  public static Result handle_ajax_111() {
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
  public static Result handle_ajax_121() {
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
    return ok();
  }
  
  @BodyParser.Of(BodyParser.Json.class)
  public static Result handle_ajax_212() {
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
        rating.additional = true;
        rating.update();
      }
    }
    return ok();
  }
  
  @BodyParser.Of(BodyParser.Json.class)
  public static Result handle_ajax_222() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    if (user != null) {
      JsonNode jsonPrefs = request().body().asJson().get("prefs");
      for(JsonNode jsonPref : jsonPrefs) {
        Movie movie1 = Movie.find.byId(jsonPref.get("movie1").asInt());
        Movie movie2 = Movie.find.byId(jsonPref.get("movie2").asInt());
        int value = jsonPref.get("value").asInt();
        Preference pref = Preference.create(user, movie1, movie2, value);
        pref.additional = true;
        pref.update();
      }
    }
    return ok();
  }
  
  @BodyParser.Of(BodyParser.Json.class)
  public static Result handle_ajax_214() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    if (user != null) {
      int recImprovement = request().body().asJson().get("rec_improvement").asInt();
      user.addRecommendationComparison(recImprovement);
    }
    return ok();
  }
  
  @BodyParser.Of(BodyParser.Json.class)
  public static Result handle_ajax_224() {
    int userId = Integer.parseInt(session().get("userId"));
    User user = User.find.byId(userId);
    if (user != null) {
      int recImprovement = request().body().asJson().get("rec_improvement").asInt();
      user.addRecommendationComparison(recImprovement);
    }
    return ok();
  }
  
  
  public static Result javascriptRoutes() {
    response().setContentType("text/javascript");
    return ok(
      Routes.javascriptRouter("jsRoutes", controllers.routes.javascript.Experiment.handle_ajax())
        );
  }
  
  // Classes for forms
  
  public static class AnswerQuestions {
    public int q1Answer;
    public int q2Answer;
    public int q3Answer;
    public int q4Answer;
  }
  
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
}