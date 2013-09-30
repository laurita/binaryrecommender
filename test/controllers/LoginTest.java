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

public class LoginTest extends WithApplication {
	@Before
	public void setUp() {
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
		Yaml yaml = new Yaml();
		Ebean.save((List) yaml.load("test-data.yml"));
	}

	@Test
	public void authenticateSuccess() {
		Result result = callAction(
			controllers.routes.ref.Application.authenticate(),
			fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
				"email", "bob@example.com"))
		);
		assertEquals(Http.Status.SEE_OTHER, status(result));
		assertEquals("bob@example.com", session(result).get("email"));
	}
	
	@Test
	public void authenticateFailure() {
		Result result = callAction(
			controllers.routes.ref.Application.authenticate(),
			fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
				"email", "lisa@example.com"))
		);
		assertEquals(Http.Status.BAD_REQUEST, status(result));
		assertNull(session(result).get("email"));
	}
	
	@Test
	public void authenticated() {
	    Result result = callAction(
	        controllers.routes.ref.Application.index(),
	        fakeRequest().withSession("email", "bob@example.com")
	    );
	    assertEquals(Http.Status.OK, status(result));
	}    
	
	@Test
	public void notAuthenticated() {
	    Result result = callAction(
	        controllers.routes.ref.Application.index(),
	        fakeRequest()
	    );
	    assertEquals(Http.Status.SEE_OTHER, status(result));
	    assertEquals("/login", header("Location", result));
	}
}