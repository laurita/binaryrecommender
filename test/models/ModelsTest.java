package models;

import play.*;
import play.libs.*;
import models.*;
import org.junit.*;
import static org.junit.Assert.*;
import play.test.WithApplication;
import static play.test.Helpers.*;
import java.util.*;
import com.avaje.ebean.*;
import play.test.*;

public class ModelsTest extends WithApplication {
	@Before
	public void setUp() {
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
		Yaml yaml = new Yaml();
		
		@SuppressWarnings("unchecked")
		Map<String,List<Object>> all = (Map<String,List<Object>>) yaml.load("test-data.yml");
	
		// Insert users
		Ebean.save(all.get("users"));
		
		// Insert movies
		Ebean.save(all.get("movies"));
		
		// Insert ratings
		Ebean.save(all.get("ratings"));
	}
	
	@Test
	public void usersTotal() {
		assertEquals(8, User.find.findRowCount());
	}
	
	@Test
	public void moviesTotal() {
		assertEquals(20, Movie.find.findRowCount());
	}
	
	@Test
	public void ratingsTotal() {
		assertEquals(40, Rating.find.findRowCount());
	}
	
	@Test
	public void usersRatingsTotal() {
		User user = User.find.where().eq("email", "user1@gmail.com").findUnique();
		assertEquals(5, user.getRatings().size());
	}
	
	@Test
	public void moviesRatingsTotal() {
		Movie movie = Movie.find.where().eq("movieId", 2710).findUnique();
		assertEquals(4, movie.getRatings().size());
	}
	
	@Test
	public void moviesRatingTotalAfterModifyingRating() {
		User user = User.find.where().eq("email", "user1@gmail.com").findUnique();
		Movie movie = Movie.find.where().eq("movieId", 2710).findUnique();
		int sizeBefore = movie.getRatings().size();
		Rating rating = Rating.create(user, movie, 5);
		rating.save();
		int sizeAfter = movie.getRatings().size();
		assertEquals(sizeBefore, sizeAfter);
		
	}
	
	@Test
	public void moviesRatingTotalAfterAddingRating() {
		User user = User.find.where().eq("email", "user1@gmail.com").findUnique();
		Movie movie = Movie.find.where().eq("movieId", 1924).findUnique();
		int sizeBefore = movie.getRatings().size();
		Rating rating = Rating.create(user, movie, 5);
		rating.save();
		int sizeAfter = movie.getRatings().size();
		assertEquals(sizeBefore + 1, sizeAfter);
	}
		
	@Test
	public void createAndRetrieveUser() {
		Ebean.save(new User("bob@gmail.com"));
		User bob = User.find.where().eq("email", "bob@gmail.com").findUnique();
		assertNotNull(bob);
		assertEquals("bob@gmail.com", bob.email);
	}
	
	@Test
	public void tryAuthenticateUser() {
		Ebean.save(new User("bob@gmail.com"));
		assertNotNull(User.authenticate("bob@gmail.com"));
		assertNull(User.authenticate("bobby@gmail.com"));
	}
}
