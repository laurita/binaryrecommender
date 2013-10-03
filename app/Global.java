import play.*;
import play.libs.*;
import com.avaje.ebean.Ebean;
import models.*;
import java.util.*;

public class Global extends GlobalSettings {
	@Override
	public void onStart(Application app) {
		
		// Check if the database is empty
		if (User.find.findRowCount() == 0) {
			
			Yaml yaml = new Yaml();
			
			@SuppressWarnings("unchecked")
			Map<String,List<Object>> all = (Map<String,List<Object>>) yaml.load("initial-data.yml");
		
			// Insert users
			Ebean.save(all.get("users"));
			
			// Insert movies
			Ebean.save(all.get("movies"));
			
			// Insert ratings
			Ebean.save(all.get("ratings"));

		}
	}
}