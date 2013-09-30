package models;

import play.*;
import play.libs.*;
import models.*;
import org.junit.*;
import static org.junit.Assert.*;
import play.test.WithApplication;
import static play.test.Helpers.*;
import java.util.List;
import com.avaje.ebean.*;
import play.test.*;

public class ModelsTest extends WithApplication {
	@Before
	public void setUp() {
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
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
