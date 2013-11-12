package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;
import play.data.validation.Constraints;
import com.avaje.ebean.Expr;
import com.avaje.ebean.*;

@Entity
@Table(name="preference")
public class Preference extends Model {
	
	@Id
	public Long id;
	
	public int value;
  
  public boolean additional = false;
	
  @Constraints.Required
	@ManyToOne
	public User user;
	
	@Constraints.Required
	@ManyToOne
	public Movie movie1;
	
	@Constraints.Required
	@ManyToOne
	public Movie movie2;
	
	private Preference(User user, Movie movie1, Movie movie2, int value) {
		this.user = user;
		this.movie1 = movie1;
		this.movie2 = movie2;
		this.value = value;
	}
  
	private Preference(User user, Movie movie1, Movie movie2, int value, boolean additional) {
		this.user = user;
		this.movie1 = movie1;
		this.movie2 = movie2;
		this.value = value;
    this.additional = additional;
	}
	
	public String toString() {
		return String.format("%d - %s - %s - %d", user.id, movie1.id, movie2.id, value);
	}
	
	public static void create(User user, Movie movie1, Movie movie2, int value) {
    System.out.println("create Preference");
    
    String sql;

    sql = String.format("select * from preference where " +
      "user_id = %d and movie1_id = %d and movie2_id = %d ", user.id, movie1.id, movie2.id);
		SqlQuery query = Ebean.createSqlQuery(sql);
    List<SqlRow> prefs = query.findList();
    
		if (prefs.size() != 0) {
      
      sql = String.format("update preference set value = %d where " +
        "user_id = %d and movie1_id = %d and movie2_id = %d ", 
        value, user.id, movie1.id, movie2.id);
		  SqlUpdate update = Ebean.createSqlUpdate(sql);
      int modifiedCount = Ebean.execute(update);
      
		} else {
      Preference p = new Preference(user, movie1, movie2, value);
		  p.save();
    }
	}
  
	public static void create(User user, Movie movie1, Movie movie2, int value, boolean additional) {
    System.out.println("create Preference");
    
    String sql;

    sql = String.format("select * from preference where " +
      "user_id = %d and movie1_id = %d and movie2_id = %d ", user.id, movie1.id, movie2.id);
		SqlQuery query = Ebean.createSqlQuery(sql);
    List<SqlRow> prefs = query.findList();
    
		if (prefs.size() != 0) {
      
      sql = String.format("update preference where " +
        "user_id = %d and movie1_id = %d and movie2_id = %d " +
        "set value = %d and additional = %b", user.id, movie1.id, movie2.id, value, additional);
		  SqlUpdate update = Ebean.createSqlUpdate(sql);
      int modifiedCount = Ebean.execute(update);
      
		} else {
      Preference p = new Preference(user, movie1, movie2, value, additional);
		  p.save();
    }
	}
  
  public static int findRowNumberOfPref(int user_id, int movie1_id, int movie2_id) {
    String sql = String.format("select rownum from (select movie1_id, movie2_id, row_number() " +
      "over (order by logpopcorrrand desc) as rownum from " +
      "(select * from preference where user_id = %d) as a) as b " +
      "where movie1_id = %d and movie2_id = %d;", user_id, movie1_id, movie2_id);
		SqlQuery query = Ebean.createSqlQuery(sql);
    List<SqlRow> rowNumbers = query.findList();
    int rowNumber;
		if (rowNumbers.size() != 0) {
      rowNumber = rowNumbers.get(0).getInteger("rownum");
      
		} else {
      rowNumber = 0;
    }
    return rowNumber;
  }
  
  public static int findRowCountUntil(int user_id, int movie1_id, int movie2_id, int id) {
    String sql = String.format("select count(*) from (select *, row_number() " +
      "over(order by logpopcorrrand desc) as rownum from preference where user_id = %d) as a " +
      "join (select rownum from (select movie1_id, movie2_id, row_number() over (order by logpopcorrrand desc) as rownum " +
      "from (select * from preference where user_id = %d) as a) as b where movie1_id = %d and movie2_id = %d) as c " +
      "on (a.rownum < c.rownum) where movie1_id = %d or movie2_id = %d;", user_id, user_id, movie1_id, movie2_id, id, id);
		SqlQuery query = Ebean.createSqlQuery(sql);
    List<SqlRow> rowCounts = query.findList();
    int rowCount;
		if (rowCounts.size() != 0) {
      rowCount = rowCounts.get(0).getInteger("count");
		} else {
      rowCount = 0;
    }
    return rowCount;
  }
  
  public static void deletePrefs(int user_id, int id) {
    String sql = String.format("delete from preference where user_id = %d " +
      "and (movie1_id = %d or movie2_id = %d);", user_id, id, id);
		SqlUpdate update = Ebean.createSqlUpdate(sql);
    int modifiedCount = Ebean.execute(update);
    System.out.println("deleted " + modifiedCount + " preferences");
  }
  
  public static int countForUser(int user_id) {
    String sql = String.format("select count(*) from preference " +
      "where user_id = %d", user_id);
		SqlQuery query = Ebean.createSqlQuery(sql);
    List<SqlRow> rowCounts = query.findList();
    int rowCount;
		if (rowCounts.size() != 0) {
      rowCount = rowCounts.get(0).getInteger("count");
      
		} else {
      rowCount = 0;
    }
    return rowCount;
    
  }
	
	public static Finder<Long,Preference> find = new Finder<Long,Preference>(
		Long.class, Preference.class);
	
	public static List<Preference> findByUser(User user) {  
	    return find.where().eq("user", user).findList();  
	} 
	
	public static List<Preference> findByMovie1(Movie movie1) {  
	    return find.where().eq("movie1", movie1).findList();  
	} 
	
	public static List<Preference> findByMovie2(Movie movie2) {  
	    return find.where().eq("movie2", movie2).findList();  
	}
	
	public static List<Preference> findAll() {
		return find.all();
	}
}