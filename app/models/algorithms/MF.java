package models.algorithms;

import models.algorithms.helpers.*;
import play.data.validation.Constraints;
import java.util.*;
import javax.persistence.*;
import play.db.ebean.Model.*;
import play.db.ebean.*;
import com.avaje.ebean.Expr;
import com.avaje.ebean.*;

public class MF extends Model {
	
	private int max_features = 16;
	
	//static final int MAX_FEATURES = 8;            // Number of features to use
	private static final int MIN_EPOCHS = 120;      // Minimum number of epochs per feature
	
	public int number_of_movies;
	public int number_of_customers;

	private static final float MIN_IMPROVEMENT = 0.0001f;       // Minimum improvement required to continue current feature
	private static final float INIT = 0.1f;                     // Initialization value for features
	private static final float LRATE = 0.001f;                  // Learning rate parameter
	private static final float K = 0.015f;                      // Regularization parameter used to minimize over-fitting


	private int             m_nRatingCount;                 // Current number of loaded ratings
	private Data            m_aRatings[];                   // Array of ratings data
	private Movie           m_aMovies[];                    // Array of movie metrics
	private Customer        m_aCustomers[];                 // Array of customer metrics
	private float           m_aMovieFeatures[][];           // Array of features by movie (using floats to save space)
	private float           m_aCustFeatures[][];            // Array of features by customer (using floats to save space)
	public Map<Integer, Integer> m_mCustIds = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> m_mMovieIds = new HashMap<Integer, Integer>();
	
	float globalAverage;

	private List<Preference> preferences;

	private boolean baseline = false;

	public void setBaseline(boolean baseline) {
		this.baseline = baseline;
	}

	//-------------------------------------------------------------------
	// Initialization
	//-------------------------------------------------------------------
	public MF(List<Preference> preferences) {
    	
		this.preferences = preferences;
    	
		Collections.sort(this.preferences, new Comparator<Preference>() {
			@Override
			public int compare(Preference p1, Preference p2) {
				int item = p1.getItemId() - p2.getItemId();
				if (item != 0) return item; 
				return p1.getUserId() - p2.getUserId();
			}
		});
	}
    
	public void initialize() {   
		loadArrays();    	
		calcMetrics();
		calcFeatures();
	}
	
	public static List<Preference> loadMLPreferences() {
		List<Preference> prefs = new ArrayList<Preference>();
		String sql = "select * from ml_ratings";
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> list = sqlQuery.findList();

		for (SqlRow row : list) {
			int userId = row.getInteger("user_id");
			int movieId = row.getInteger("movie_id");
			Double value = row.getDouble("value");
			Preference pref = new Preference(userId, movieId, value);
			prefs.add(pref);
		}
		return prefs;
	}
	
	public static List<Preference> addNewPreferencesToList(List<Preference> prefs) {
		String sql = "select * from rating";
		SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
		List<SqlRow> list = sqlQuery.findList();

		for (SqlRow row : list) {
			int userId = row.getInteger("user_id");
			int movieId = row.getInteger("movie_id");
			Double value = row.getDouble("value");
			Preference pref = new Preference(userId, movieId, value);
			prefs.add(pref);
		}
		return prefs;
	}
    
	//-------------------------------------------------------------------
	// Calculations - This Paragraph contains all of the relevant code
	//-------------------------------------------------------------------

	// CalcMetrics
	// - Loop through the history and pre-calculate metrics used in the training 
	// - Also re-number the customer id's to fit in a fixed array
	private void calcMetrics() {
		Integer mid, cid;
		int globalSum = 0;
	
		// Process each row in the training set
		for (Data rating : m_aRatings) {
	        	        
			int movieId = rating.MovieId;
			mid = m_mMovieIds.get(movieId);
			if (mid == null) {	        	 
				mid = m_mMovieIds.size();
				m_mMovieIds.put(movieId, mid);
				Movie movie = new Movie();
				movie.RatingCount = 0;
				movie.RatingSum = 0;
				m_aMovies[mid] = movie;
			}
	         
			rating.MovieId = mid;
	         
			m_aMovies[mid].RatingCount++;
			m_aMovies[mid].RatingSum += rating.Rating;
	         
			// Add customers (using a map to re-number id's to array indexes)
			int custId = rating.CustId;
			cid = m_mCustIds.get(custId);
			if (cid == null) {
				cid = m_mCustIds.size();
	
				// Reserve new id and add lookup
				m_mCustIds.put(custId, cid);
	
				// Store off old sparse id for later
				Customer customer = new Customer();
				customer.CustomerId = custId;
				customer.RatingCount = 0;
				customer.RatingSum = 0;
	             
				m_aCustomers[cid] = customer; 

			}
	
			// Swap sparse id for compact one
			rating.CustId = cid;
	
			m_aCustomers[cid].RatingCount++;
			m_aCustomers[cid].RatingSum += rating.Rating;
			globalSum += rating.Rating;
		}
	     
		float globalAvg = globalSum / m_nRatingCount; 
		this.globalAverage = globalAvg;
	     
		// Do a follow-up loop to calc movie averages
		for (Movie movie : m_aMovies) {
			movie.RatingAvg = movie.RatingSum / (1.0 * movie.RatingCount);
			movie.PseudoAvg = (globalAvg * 25 + movie.RatingSum) / (25.0 + movie.RatingCount);
		}
		for (Data rating : m_aRatings) {
			Movie movie = m_aMovies[rating.MovieId];
			Customer user = m_aCustomers[rating.CustId];	    	
			user.OffsetSum += (rating.Rating - movie.PseudoAvg);
		}
	}
	 
	// CalcFeatures
	// - Iteratively train each feature on the entire data set
	// - Once sufficient progress has been made, move on
	//
	private void calcFeatures() {
		int f, e, i, custId, cnt = 0;
		Data rating;
		double err, p, sq, rmse_last = 0, rmse = 2.0;
		int movieId;
		float cf, mf;

		for (f=0; f<max_features; f++) {
			// Keep looping until you have passed a minimum number 
			// of epochs or have stopped making significant progress
			for (e=0; (e < MIN_EPOCHS) || (rmse <= rmse_last - MIN_IMPROVEMENT); e++) {
				cnt++;
				sq = 0;
				rmse_last = rmse;

				for (i=0; i<m_nRatingCount; i++) {
					rating = m_aRatings[i];
					movieId = rating.MovieId;
					custId = rating.CustId;

					// Predict rating and calc error
					p = predictRating(movieId, custId, f, rating.Cache, true);
					err = (1.0 * rating.Rating - p);
					sq += err*err;
	                
					// Cache off old feature values
					cf = m_aCustFeatures[f][custId];
					mf = m_aMovieFeatures[f][movieId];

					// Cross-train the features
					m_aCustFeatures[f][custId] += (float)(LRATE * (err * mf - K * cf));
					m_aMovieFeatures[f][movieId] += (float)(LRATE * (err * cf - K * mf));
				}
				rmse = Math.sqrt(sq/m_nRatingCount);
			}
			// Cache off old predictions
			for (i=0; i<m_nRatingCount; i++) {
				rating = m_aRatings[i];
				rating.Cache = (float) predictRating(rating.MovieId, rating.CustId, f, rating.Cache, false);
			}            
		}
	}

	//
	// PredictRating
	// - During training there is no need to loop through all of the features
	// - Use a cache for the leading features and do a quick calculation for the trailing
	// - The trailing can be optionally removed when calculating a new cache value
	//
	private double predictRating(int movieId, int custId, int feature, float cache, boolean bTrailing) {
		// Get cached value for old features or default to an average
		//double sum = (cache > 0) ? cache : 1;
		//m_aMovies[movieId].PseudoAvg;
		double sum;
		if (cache > 0) {
			sum = cache;
		}
		else {
			if (baseline) {
				sum = m_aMovies[movieId].PseudoAvg + ((double) m_aCustomers[custId].OffsetSum / m_aCustomers[custId].RatingCount);
			}
			else {
				sum = 1;
			}
		}		

		// Add contribution of current feature
		sum += m_aMovieFeatures[feature][movieId] * m_aCustFeatures[feature][custId];
		if (sum > 5) sum = 5;
		if (sum < 1) sum = 1;

		// Add up trailing defaults values
		if (bTrailing)
		{
			sum += (max_features-feature-1) * (INIT * INIT);
			if (sum > 5) sum = 5;
			if (sum < 1) sum = 1;
		}

		return sum;
	}

	// PredictRating
	// - This version is used for calculating the final results
	// - It loops through the entire list of finished features
	public Double predictRating(int movieId, int custId) {
		Integer cid = m_mCustIds.get(custId);
		Integer mid = m_mMovieIds.get(movieId);
		
		if (cid == null || mid == null) {
			return null;
		}
		
		movieId = mid;
		custId = cid;
		
		double sum;
		if (baseline) {
			sum = m_aMovies[movieId].PseudoAvg + ((double) m_aCustomers[custId].OffsetSum / m_aCustomers[custId].RatingCount);
		}
		else {
			sum = 1;
		}
		//double sum = 1;
		//m_aMovies[movieId].PseudoAvg;
		//double sum = m_aMovies[movieId].PseudoAvg;

		for (int f=0; f<max_features; f++) 
		{	    		    	
			sum += m_aMovieFeatures[f][movieId] * m_aCustFeatures[f][custId];
			if (sum > 5) sum = 5;
			if (sum < 1) sum = 1;
		}

		return sum;
	}
	    
	// ProcessFile
	// - Load a history file in the format:
	//
	//   <MovieId>:
	//   <CustomerId>,<Rating>
	//   <CustomerId>,<Rating>
	//   ...
	private void loadArrays() {
		
		m_nRatingCount = 0;
		m_aRatings = new Data[preferences.size()];                        // Array of ratings data
	    
		Set<Integer> movieIds = new HashSet<Integer>();
		Set<Integer> customerIds = new HashSet<Integer>();
		
		for (Preference pref : preferences) {			
			int movieId = pref.getItemId();
			int custId = pref.getUserId();
			int rating = (int) pref.getValue();

			movieIds.add(movieId);
			customerIds.add(custId);
			
			if (m_aRatings[m_nRatingCount] == null) {
				m_aRatings[m_nRatingCount] = new Data();
			}
		    
			m_aRatings[m_nRatingCount].MovieId = (short)movieId;
			m_aRatings[m_nRatingCount].CustId = custId;
			m_aRatings[m_nRatingCount].Rating = (byte) rating;
			m_aRatings[m_nRatingCount].Cache = 0;
			m_nRatingCount++;
		}
		
		number_of_customers = customerIds.size();
		number_of_movies = movieIds.size();
		
		m_aMovies = new Movie[number_of_movies];                          // Array of movie metrics
		m_aCustomers = new Customer[number_of_customers];                 // Array of customer metrics
		m_aMovieFeatures = new float[max_features][number_of_movies];     // Array of features by movie (using floats to save space)
		m_aCustFeatures = new float[max_features][number_of_customers];   // Array of features by customer (using floats to save space)
	            
		for (int f=0; f<max_features; f++) {
			for (int i=0; i<number_of_movies; i++) m_aMovieFeatures[f][i] = (float)INIT;
			for (int i=0; i<number_of_customers; i++) m_aCustFeatures[f][i] = (float)INIT;
		}
				
	}

	public List<Integer> rank(int user, List<Integer> unratedMovies) {
		HashMap<Integer,Double> map = new HashMap<Integer,Double>();
		ValueComparator bvc =  new ValueComparator(map);
		TreeMap<Integer,Double> sorted_map = new TreeMap<Integer,Double>(bvc);

		for (Integer m : unratedMovies) {
			Double pred = predictRating(m, user);
			map.put(m, pred);
		}
		sorted_map.putAll(map);
		List<Integer> list = new ArrayList<Integer>(sorted_map.keySet());
		return list;
	}

	public List<List<Integer>> predictRankingListWithTies(List<Integer> unratedMovies, int userId) {

		Map<Double, List<Integer>> map = new HashMap<Double, List<Integer>>();
		for (Integer m : unratedMovies) {
			Integer mid = m_mMovieIds.get(m);
			if (mid != null) {
				List<Integer> list;
				Double key  = predictRating(m, userId);
				if (map.get(key) == null) {
					list = new ArrayList<Integer>();
				} else {
					list = map.get(key);
				}
				list.add(m);
				map.put(key, list);
			}
		}
		Map<Double, List<Integer>> treeMap = new TreeMap<Double, List<Integer>>(map);
		List<List<Integer>> lst = new ArrayList<List<Integer>>(treeMap.values());
		Collections.reverse(lst);
		return lst;
	}
}

class ValueComparator implements Comparator<Integer> {

	Map<Integer, Double> base;
	public ValueComparator(Map<Integer, Double> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.
	public int compare(Integer a, Integer b) {
		if (base.get(a) == null) {
			return 1;
		}
		if (base.get(b) == null) {
			return -1;
		}
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
