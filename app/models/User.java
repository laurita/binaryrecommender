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
@Table(name="users")
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
    createPreferences();
    super.update();
	}
	
	void createdAt() {
		this.createdAt = new Date();
	}
		
	private void addExperimentGroup() {
		int experimentGroup = 1;
		User lastUser = User.find.orderBy().desc("createdAt").setMaxRows(1).findUnique();
		int lastExperimentGroup = (lastUser != null) ? lastUser.experimentGroup : 0;
		if (lastExperimentGroup != 0) {
			experimentGroup = (lastExperimentGroup == 1) ? 2 : 1;
		}
		this.experimentGroup = experimentGroup;
	}
  
	private void createPreferences() {
    if (this.experimentGroup == 2) {
	    String sql = String.format(
        "insert into preference (user_id, movie1_id, movie2_id, logpopcorrrand) " +
        "select %d, movie1_id, movie2_id, logpopcorr * (0.8 + random() * (1 - 0.8)) as x " +
        "from moviepairs order by x desc;", this.id);
		  SqlUpdate update = Ebean.createSqlUpdate(sql);
      int modifiedCount = Ebean.execute(update);
    }
	}
	
	public static Finder<Integer,User> find = new Finder<Integer,User>(
		Integer.class, User.class);
	
	public String toString() {
		return email;
	}
	
	public List<Rating> getRatings() {
		return Rating.find.where().eq("user", this).findList();
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
  
	public List<Integer> getUnratedMovieIdsFromGroup(int group) {
		String sql = String.format("SELECT id FROM (SELECT *, ROW_NUMBER() OVER " +
      "(ORDER BY LOGPOPVAR desc) AS r FROM movie) AS tbl2 " +
      "WHERE r %% 2 = %d;", group);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
		List<Integer> allIds = new ArrayList<Integer>();
		for (SqlRow row : rows) {
			allIds.add(row.getInteger("id"));
		}
		allIds.removeAll(this.getRatedMovieIds());
		return allIds;
	}
  
	public List<SqlRow> getPreferedMovieIds() {
    String sql = String.format("select distinct(movie1_id) from preference " +
      "where user_id = %d and value is not null union select distinct(movie2_id) " +
      "from preference where user_id = %d and value is not null;", this.id, this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> movieIds = sqlQuery.findList();
		return movieIds;
	}
		
	public List<Integer> getUnpreferedMovieIds() {
    String sql = String.format("select distinct(movie1_id) from preference " +
      "where user_id = %d and value is null union select distinct(movie2_id) " +
      "from preference where user_id = %d and value not null;", this.id, this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
    List<Integer> ids = new ArrayList<Integer>();
    for (SqlRow row : rows) {
      ids.add(row.getInteger("id"));
    }
		return ids;
	}
  
	public List<Integer> getUnpreferedMovieIdsFromGroup(int group) {
		String sql = String.format("SELECT id FROM (SELECT *, ROW_NUMBER() OVER " +
      "(ORDER BY LOGPOPVAR desc) AS r FROM movie) AS tbl2 " +
      "WHERE r %% 2 = %d;", group);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
    List<Integer> ids = new ArrayList<Integer>();
    for (SqlRow row : rows) {
      ids.add(row.getInteger("id"));
    }
		ids.removeAll(this.getPreferedMovieIds());
		return ids;
	}
  
  public List<SqlRow> getSeenMoviesRecommendedOriginally() {
		String sql = String.format(
      "select tbl1.id, tbl1.title, tbl1.description, tbl1.length, tbl1.imdb_link, " +
      "tbl1.trailer_link, tbl2.value from " +
	    "(select movie.id, title, description, length, imdb_link, " +
		  "trailer_link from movie, recommendation " +
		  "where user_id = %d and movie.id = movie_id and " +
		  "updated = false and seen = true order by movie.logpopvar " +
	    ") as tbl1 left outer join " +
	    "(select * from rating where user_id = %d) as tbl2 " +
	    "on (tbl1.id = tbl2.movie_id);", this.id, this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
		return rows;
  }
  
  public List<SqlRow> getSeenMoviePairsRecommendedOriginally() {
		String sql = String.format(
      "select tbl1.movie1_id, tbl1.movie1_title, tbl1.movie1_description, " +
		  "tbl1.movie1_length, tbl1.movie1_imdbLink, tbl1.movie1_trailerLink, " +
		  "tbl1.movie2_id, tbl1.movie2_title, tbl1.movie2_description, tbl1.movie2_length, " +
		  "tbl1.movie2_imdbLink, tbl1.movie2_trailerLink, tbl2.value from	" +
	    "(SELECT a.id movie1_id, a.title movie1_title, a.description movie1_description, " +
		  "a.length movie1_length, a.imdb_link movie1_imdbLink, a.trailer_link movie1_trailerLink, " +
		  "b.id movie2_id, b.title movie2_title, b.description movie2_description, b.length movie2_length, " +
		  "b.imdb_link movie2_imdbLink, b.trailer_link movie2_trailerLink " +
		  "FROM movie a, movie b, MOVIEPAIRS c " +
	    "WHERE movie1_id IN " +
			"(SELECT movie_id FROM RECOMMENDATION WHERE user_id = %d AND updated=false AND seen=true) " +
		  "AND movie2_id IN " +
			"(SELECT movie_id FROM RECOMMENDATION WHERE user_id = %d AND updated=false AND seen=true) " +
	    "AND a.id = movie1_id AND b.id = movie2_id " +
	    ") as tbl1 " +
	    "left outer join (select * from preference where user_id = %d) as tbl2 " +
	    "on (tbl1.movie1_id = tbl2.movie1_id and tbl1.movie2_id = tbl2.movie2_id);",
        this.id, this.id, this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
    return rows;
  }
  
  public List<SqlRow> getAllMoviesAndTheirRatings() {
		String sql = String.format(
      "select tbl1.id, tbl1.title, tbl1.description, tbl1.length, tbl1.imdb_link, " +
		  "tbl1.trailer_link, tbl2.value from (select * from movie) as tbl1 " +
      "left outer join (select * from rating where user_id = %d) as tbl2 " +
      "on (tbl1.id = tbl2.movie_id) order by tbl1.logpopvar desc;",
        this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
    return rows;
  }
  
  public int getRecommendationComparison() {
		String sql = String.format(
      "select comparison from recommendation_comparisons " +
      "where user_id = %d;", this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
    int res = 0;
    if (rows.size() != 0) {
      res = rows.get(0).getInteger("comparison");
    }
    return res;
  }
  
  public Map<Integer, Integer> getAnswers() {
		String sql = String.format(
      "select question, answer from comparisons where user_id = %d;", this.id);
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> rows = sqlQuery.findList();
    Map<Integer, Integer> answers = new HashMap<Integer, Integer>();
    for (SqlRow row : rows) {
      answers.put(row.getInteger("question"), row.getInteger("answer"));
    }
    return answers;
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
  
  public void updateRecommendation(int movieId, String name, boolean isChecked, boolean updated) {
    String sql = String.format("update recommendation set %s = %b " +
      "where user_id = %d and movie_id = %d and updated = %b;",
      name, isChecked, this.id, movieId, updated);
    System.out.println(sql);
    SqlUpdate update = Ebean.createSqlUpdate(sql);
    int modifiedCount = Ebean.execute(update);
    System.out.println(modifiedCount + " rows updated");
  }
  
  public static void updateAllUserStates() {
    System.out.println("updateing user states ...");
    String sqlString, msg;
    SqlUpdate update;
    int modifiedCount;
      
    System.out.println("from 113 to 210");
    sqlString = 
      "update users set state = '210' where state = '113';";
    update = Ebean.createSqlUpdate(sqlString);
    modifiedCount = Ebean.execute(update);
    msg = "There were " + modifiedCount + " rows updated";
    System.out.println(msg);
    
    System.out.println("from 123 to 220");
    sqlString = 
      "update users set state = '220' where state = '123';";
    update = Ebean.createSqlUpdate(sqlString);
    modifiedCount = Ebean.execute(update);
    msg = "There were " + modifiedCount + " rows updated";
    System.out.println(msg);
    
    System.out.println("from all other to 000");
    sqlString = 
      "update users set state = '000' where state != '210' and state != '220';";
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