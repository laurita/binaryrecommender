package models;

import play.data.validation.*;
import play.data.validation.Constraints;
import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;
import models.*;
import com.avaje.ebean.Expr;
import com.avaje.ebean.*;

@Entity
@Table(name="user")
public class User extends Model {
	
	@Id
	public int id;
		
	@Constraints.Required
	public String email;
	
	@Column(name = "created_at")
	public Date createdAt;
	
  public String state;
  
	@OneToMany(cascade = CascadeType.ALL, mappedBy="user")
	private List<Rating> userRatings = new ArrayList<Rating>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy="user")
	private List<Preference> userPreferences = new ArrayList<Preference>();
	
	// group: 1 (MF / ratings) or 2 (UP / preferences)
	public int experimentGroup;
	
	// true if stage 1 finished
	public boolean stage1Done = false;
	
	// true if stage 2 finished
	public boolean stage2Done = false;
	
	// 1st or 2nd list of recommendations
	public boolean afterUpdate = false;
	
	public int question1;
	public int question2;
	public int question3;
	public int question4;
	
	public User(String email) {
		this.email = email;
	}
	
	@Override
	public void save() {
		createdAt();
		addExperimentGroup();
		super.save();
		//increaseId();
		//super.update(this.id + 6040);
	}
	
	void createdAt() {
		this.createdAt = new Date();
	}
		
	/*
		void increaseId() {
		System.out.println(this.id);
		this.id = this.id + 6040;
		}
		*/
	
	private void addExperimentGroup() {
		int experimentGroup = 1;
		User lastUser = User.find.orderBy().desc("createdAt").setMaxRows(1).findUnique();
		int lastExperimentGroup = (lastUser != null) ? lastUser.experimentGroup : 0;
		if (lastExperimentGroup != 0) {
			experimentGroup = (lastExperimentGroup == 1) ? 2 : 1;
		}
    //String state = (experimentGroup == 1) ? "010" : "020";
		this.experimentGroup = experimentGroup;
    //this.state = state;
	}
	
	public static Finder<Integer,User> find = new Finder<Integer,User>(
		Integer.class, User.class);
	
	public String toString() {
		return email;
	}
	
	public List<Rating> getRatings() {
		return Rating.find.where().eq("user", this).findList();
	}
  
	public ExpressionList<Preference> getPreferenceFilter() {
		return Preference.find.where().eq("user", this);
	}
  
	public List<Preference> getPreferences() {
		return Preference.find.where().eq("user", this).findList();
	}
		
	public int getMovieRating(int movieId) {
		Rating rating = Rating.find.where()
			.eq("user", this)
				.eq("movie", Movie.find.byId(movieId))
					.findUnique();
		int r = 0;
		if (rating != null) { 
			r = rating.value;
		}
		return r;
	} 
		
		
	public List<Integer> getRatedMovieIds() {
		List<Rating> userRatings = Rating.find.fetch("movie")
      .where().eq("user", this).findList();
		List<Integer> movieIds = new ArrayList<Integer>();
		for (Rating r : userRatings) {
			movieIds.add(r.movie.id);
		}
		return movieIds;
	}
		
	public List<Integer> getUnratedMovieIds() {
		List<Movie> all = Movie.find.all();
		List<Integer> allIds = new ArrayList<Integer>();
		for (Movie m : all) {
			allIds.add(m.id);
		}
		allIds.removeAll(this.getRatedMovieIds());
		return allIds;
	}
  
	public List<Integer> getPreferedMovieIds() {
		List<Preference> userPrefs = Preference.find.fetch("movie1")
      .fetch("movie2").where().eq("user", this).findList();
		Set<Integer> movieIds = new TreeSet<Integer>();
    List<Integer> movieIdList = new ArrayList<Integer>();
		for (Preference p : userPrefs) {
			movieIds.add(p.movie1.id);
      movieIds.add(p.movie2.id);
		}
    movieIdList.addAll(movieIds);
		return movieIdList;
	}
		
	public List<Integer> getUnpreferedMovieIds() {
		List<Movie> all = Movie.find.all();
		List<Integer> allIds = new ArrayList<Integer>();
		for (Movie m : all) {
			allIds.add(m.id);
		}
		allIds.removeAll(this.getPreferedMovieIds());
		return allIds;
	}
  
  public List<Movie> getSeenMoviesRecommendedOriginally() {
		String sql = String.format(
      "select recommendation.movie_id as movie_id from movie, recommendation " +
      "where recommendation.user_id = %d and " +
      "movie.id = movie_id and " +
      "updated = false and " +
      "seen = true " +
      "order by movie.logpopvar;", this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
		List<Movie> movies = new ArrayList<Movie>();
		for (SqlRow row : rows) {
      movies.add(Movie.find.byId(row.getInteger("movie_id")));
		}
		return movies;
  }
  
  public List<List<Integer>> getSeenMoviePairsRecommendedOriginally() {
		String sql = String.format(
      "SELECT * FROM MOVIEPAIRS " + 
      "WHERE movie1_id IN " + 
      "(SELECT movie_id FROM RECOMMENDATION WHERE user_id = %d AND updated=false AND seen=true) " +
      "AND movie2_id IN " +
      "(SELECT movie_id FROM RECOMMENDATION WHERE user_id = %d AND updated=false AND seen=true);",
        this.id, this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
		List<List<Integer>> moviePairs = new ArrayList<List<Integer>>();
		for (SqlRow row : rows) {
      List<Integer> pair = new ArrayList<Integer>();
      pair.add(Movie.find.byId(row.getInteger("movie1_id")).id);
      pair.add(Movie.find.byId(row.getInteger("movie2_id")).id);
      moviePairs.add(pair);
		}
    return moviePairs;
  }
  
  public void addRecommendationComparison(int value) {
    
    String sql;
    SqlUpdate update;
    
    sql = String.format("SELECT * FROM recommendation_comparisons where user_id=%d;", this.id);
    SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
    List<SqlRow> rows = sqlQuery.findList();
    
    if (rows.size() != 0) {
      sql = String.format("DELETE FROM recommendation_comparisons WHERE user_id=%d", this.id);
      update = Ebean.createSqlUpdate(sql);
      int modifiedCount = Ebean.execute(update);
    }
    
    sql = String.format(
      "INSERT INTO recommendation_comparisons values (%d, %d);",
      this.id, value
    );
		update = Ebean.createSqlUpdate(sql);
    int modifiedCount = Ebean.execute(update);
    //String msg = "There were " + modifiedCount + " rows inserted";
  }
  
  public void addComparison(int question, int value) {
    
    String sql;
    SqlUpdate update;
    
    sql = String.format("SELECT * FROM comparisons where user_id=%d and question=%d;", this.id, question);
    SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
    List<SqlRow> rows = sqlQuery.findList();
    
    if (rows.size() != 0) {
      sql = String.format("DELETE FROM comparisons WHERE user_id=%d and question=%d;", this.id, question);
      update = Ebean.createSqlUpdate(sql);
      int modifiedCount = Ebean.execute(update);
    }
    
    sql = String.format(
      "INSERT INTO comparisons values (%d, %d, %d);",
      this.id, question, value
    );
		update = Ebean.createSqlUpdate(sql);
    int modifiedCount = Ebean.execute(update);
    //String msg = "There were " + modifiedCount + " rows inserted";
  }
  
  public static void updateAllUserStates() {
    System.out.println("updateing user states ...");
    String sqlString, msg;
    SqlUpdate update;
    int modifiedCount;
      
    System.out.println("from 113 to 210");
    sqlString = 
      "update user set state = 210 where state = 113;";
    update = Ebean.createSqlUpdate(sqlString);
    modifiedCount = Ebean.execute(update);
    msg = "There were " + modifiedCount + " rows updated";
    System.out.println(msg);
    
    System.out.println("from 123 to 220");
    sqlString = 
      "update user set state = 220 where state = 123;";
    update = Ebean.createSqlUpdate(sqlString);
    modifiedCount = Ebean.execute(update);
    msg = "There were " + modifiedCount + " rows updated";
    System.out.println(msg);
    
    System.out.println("from all other to 000");
    sqlString = 
      "update user set state = 000 where state != 210 and state != 220;";
    update = Ebean.createSqlUpdate(sqlString);
    modifiedCount = Ebean.execute(update);
    msg = "There were " + modifiedCount + " rows updated";
    System.out.println(msg);
  }

	public static List<User> findAll() {
		return find.all();
	}
	
	public static User findByEmail(String email) {
		for (User candidate : find.all()) {
			if (candidate.email.equals(email)) {
				return candidate;
			}
		}
		return null;
	}
	
	public static User authenticate(String email) {
    System.out.println("User.authenticate called");
    User res = find.where().eq("email", email).findUnique();
    System.out.println("User is " + res);
		return res;
	}

	public static boolean remove(User user) {
		return find.all().remove(user);
	}
	
	public static void add(User user) {
		find.all().add(user);
	}

}