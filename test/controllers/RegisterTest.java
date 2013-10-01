package controllers;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import play.mvc.*;
import play.libs.*;
import play.test.*;
import static play.test.Helpers.*;
import com.avaje.ebean.Ebean;
import com.google.common.collect.ImmutableMap;

public class RegisterTest extends WithApplication {
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
	public void registerSuccess() {
		Result result = callAction(
			controllers.routes.ref.Application.submit(),
				fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
					"email", "laura@example.com"))
						);
		assertEquals(Http.Status.SEE_OTHER, status(result));
		assertEquals("laura@example.com", session(result).get("email"));
	}
	
	@Test
	public void registerFailure() {
		Result result = callAction(
			controllers.routes.ref.Application.submit(),
				fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
					"email", "user1@gmail.com"))
						);
		assertEquals(Http.Status.BAD_REQUEST, status(result));
		assertNull(session(result).get("email"));
	}
	
	@Test
	public void registerLogoutLoginSuccess() {
		callAction(
			controllers.routes.ref.Application.submit(),
				fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
					"email", "laura@example.com"))
						);
		callAction(controllers.routes.ref.Application.logout(), fakeRequest());
		Result result = callAction(
			controllers.routes.ref.Application.authenticate(),
				fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
					"email", "laura@example.com"))
						);
		assertEquals(Http.Status.SEE_OTHER, status(result));
		assertEquals("laura@example.com", session(result).get("email"));
	}    
}