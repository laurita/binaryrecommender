import play.*;
import play.libs.*;
import com.avaje.ebean.Ebean;
import models.Movie;
import java.util.*;
import models.algorithms.*;
import models.algorithms.helpers.*;
import play.Application.*;
  
public class Global extends GlobalSettings {
	
	@Override
	public void onStart(Application app) {
				
		// Check if the database is empty
		if (Movie.find.findRowCount() == 0) {
						
			Yaml yaml = new Yaml();
			
			@SuppressWarnings("unchecked")
			Map<String,List<Object>> all = (Map<String,List<Object>>) yaml.load("initial-data1.yml");
			
			// Insert movies
			Ebean.save(all.get("movies"));
			
			// Build UP model
      // TODO
      //if (play.Play.application().configuration().getString("experiment.stage") == "2") {
        
      //}			
		}
	}
  
  /*
	@Override
	public void onStop(Application app) {
		System.out.println("stop");
    buildUPmodel();
	}
	
	public void buildUPmodel() {
		List<Preference> prefs = UP.loadMLPreferences();
		List<BinaryPreference> comps = UP.loadComparisons();
		UP up = new UP(prefs, comps);
		up.initialize();
	}
  */
}