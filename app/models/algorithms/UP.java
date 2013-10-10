package models.algorithms;

import models.algorithms.helpers.*;
import java.util.*;

public class UP {

	public static float gamma = 7.0f;
	public static final int SEC_IN_DAY = 86400;

	public int number_of_movies;
	public int number_of_customers;

	public int m_nRatingCount;              // Current number of loaded ratings
	private Data m_aRatings[];              // Array of ratings data
	private Movie m_aMovies[];              // Array of movie metrics
	private Customer m_aCustomers[];        // Array of customer metrics

	public Map<Integer, Integer> m_mCustIds = new HashMap<Integer, Integer>();       // map of real userIds to compact userIds
	public Map<Integer, Integer> m_mMovieIds = new HashMap<Integer, Integer>();      // map of real movieIds to compact movieIds

	private List<Preference> preferences;   // list of preferences

	float globalAverage;

	public Data userItemMatrix[][];         // user-item matrix of ratings
	public float similarities[][];          // user-user similarities (matrix of Pearson correlations)
	public float signSimilarities[][];      // significance corrected user-user similarities [Herlocker et al., 1999]
	public float kMatrix[][];               // K matrix (either personalized or non-personalized, as specified)

	//-------------------------------------------------------------------
	// Initialization
	//-------------------------------------------------------------------
	public UP(List<Preference> preferences) {

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

	/**
		* Initializes the model: load the arrays, calculate metrics, user-item matrix and user-user similarities
		*/
	public void initialize() {
		loadArrays();
		calcMetrics();
		calcUserItemMatrix();
		calcSimilarities();
	}

	/**
		* Processes the file and loads the ratings array.
		* Initializes empty arrays for movies and users.
		*/
	private void loadArrays() {

		m_nRatingCount = 0;
		m_aRatings = new Data[preferences.size()];                        // Array of ratings data

		Set<Integer> movieIds = new HashSet<Integer>();
		Set<Integer> customerIds = new HashSet<Integer>();

		for (Preference pref : preferences) {
			int movieId = pref.getItemId();
			int custId = pref.getUserId();
			int rating = (int) pref.getValue();
			long time = pref.getTime();

			movieIds.add(movieId);
			customerIds.add(custId);

			if (m_aRatings[m_nRatingCount] == null) {
				m_aRatings[m_nRatingCount] = new Data();
			}

			m_aRatings[m_nRatingCount].MovieId = (short)movieId;
			m_aRatings[m_nRatingCount].CustId = custId;
			m_aRatings[m_nRatingCount].Rating = (byte) rating;
			m_aRatings[m_nRatingCount].Cache = 0;
			m_aRatings[m_nRatingCount].Time = time;
			m_nRatingCount++;
		}

		number_of_customers = customerIds.size();
		number_of_movies = movieIds.size();

		m_aMovies = new Movie[number_of_movies];                // Array of movie metrics
		m_aCustomers = new Customer[number_of_customers];       // Array of customer metrics
	}

	/**
		* Calculates metrics, such as movie rating average, user rating average.
		* Re-number the customer id's to fit in a fixed array.
		*/
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
				// add the user that rated the movie to the list in the Movie object
				movie.UsersRatedIt = new ArrayList<Integer>();
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
				customer.MoviesRatedBy = new ArrayList<Integer>();

				m_aCustomers[cid] = customer;
			}

			// Swap sparse id for compact one
			rating.CustId = cid;

			m_aCustomers[cid].RatingCount++;
			m_aCustomers[cid].RatingSum += rating.Rating;

			// add compact indexes to the lists
			m_aCustomers[cid].MoviesRatedBy.add(rating.MovieId);
			m_aMovies[mid].UsersRatedIt.add(rating.CustId);

			globalSum += rating.Rating;
		}

		this.globalAverage = globalSum / m_nRatingCount;

		for (Movie movie : m_aMovies) {
			movie.RatingAvg = movie.RatingSum / (1.0 * movie.RatingCount);
		}

		for (Customer cust : m_aCustomers) {
			cust.RatingAvg = cust.RatingSum / (1.0 * cust.RatingCount);
		}
	}

	/**
		* Calculates similarities between users.
		* If a pair of users has not rated any movies in common, the similarity is 0.
		* If a user has rated only one movie (which is equal to user's avg rating),
		* the denominator is 0 => similarity would be NaN, but I make it = 0.
		*/
	public void calcSimilarities() {
		float[][] similarities = new float[number_of_customers][number_of_customers];
		float[][] signSimilarities = new float[number_of_customers][number_of_customers];

		for (int u = 0; u < number_of_customers; u++) {
			ArrayList<Integer> u_movies = m_aCustomers[u].MoviesRatedBy;
			double ru = m_aCustomers[u].RatingAvg;
			for (int v = 0; v < number_of_customers; v++) {
				if (u == v) {
					similarities[u][v] = 1;
					signSimilarities[u][v] = 1;
				} else {
					ArrayList<Integer> v_movies = m_aCustomers[v].MoviesRatedBy;
					double rv = m_aCustomers[v].RatingAvg;
					ArrayList<Integer> uv_movies = new ArrayList<Integer>(u_movies);
					uv_movies.retainAll(v_movies);
					if (uv_movies.isEmpty()) {
						similarities[u][v] = 0;
						signSimilarities[u][v] = 0;
					} else {
						float numeratorSum = 0;
						float denominatorSumU = 0;
						float denominatorSumV = 0;
						for (int j : uv_movies) {
							int rum = userItemMatrix[u][j].Rating;
							int rvm = userItemMatrix[v][j].Rating;
							numeratorSum += (rum - ru) * (rvm - rv);
							denominatorSumU += Math.pow(userItemMatrix[u][j].Rating - ru, 2);
							denominatorSumV += Math.pow(userItemMatrix[v][j].Rating - rv, 2);
						}
						double denominator = Math.sqrt(denominatorSumU * denominatorSumV);
						if (denominator == 0) {
							similarities[u][v] = 0;
							signSimilarities[u][v] = 0;
						} else {
							similarities[u][v] = (float) (numeratorSum / denominator);
							signSimilarities[u][v] = similarities[u][v] * Math.min(uv_movies.size(), gamma) / gamma;
						}
					}
				}
			}
		}
		this.similarities = similarities;
		this.signSimilarities = signSimilarities;
	}

	/**
		* Calculated user x item matrix containing Data objects.
		*/
	public void calcUserItemMatrix() {
		Data[][] userItemMatrix = new Data[number_of_customers][number_of_movies];
		for (int k = 0; k < m_nRatingCount; k++) {
			Data rating = m_aRatings[k];
			int u = rating.CustId;
			int i = rating.MovieId;
			userItemMatrix[u][i] = rating;
		}
		this.userItemMatrix = userItemMatrix;
	}

	/**
		* Calculates K matrix.
		* @param userID : If userID = -1, non-personalized, otherwise user specific K matrix for user userID.
		* @param signCorrected : If signCorrected, then uses significance corrected similarities.
		*/
	public void calculateKMatrix(int userID, boolean signCorrected, boolean withSession) {
		// in a non-personalized case userID is -1
		if (userID != -1) {
			// get the compact id of the user
			userID = m_mCustIds.get(userID);
		}

		float[][] cMatrix = new float[number_of_movies][number_of_movies];
		float[][] wMatrix = new float[number_of_movies][number_of_movies];

		for (int u : m_mCustIds.values()) {
			ArrayList<Integer> u_movies = m_aCustomers[u].MoviesRatedBy;
			for (int i = 0; i < u_movies.size(); i++) {
				for (int j = 0; j < u_movies.size(); j++) {
					if (i != j) {
						int mi = u_movies.get(i);
						int mj = u_movies.get(j);
						Data dataI = userItemMatrix[u][mi];
						Data dataJ = userItemMatrix[u][mj];
						// If not withSession, add all score differences. Otherwise, check the time difference
						// and add only score diffs for the movies rated within 24 hours
						if (!withSession || (Math.abs(dataI.Time - dataJ.Time) < SEC_IN_DAY)) {
							int cuij = dataI.Rating - dataJ.Rating;
							// personalized case
							if (userID != -1) {
								double sim;
								// depending on signCorrected argument get the similarity
								if (signCorrected) {
									sim = signSimilarities[userID][u];
								} else {
									sim = similarities[userID][u];
								}
								// add only those weighted score differences for which the user-user similarity is > 0
								if (sim > 0) {
									wMatrix[dataI.MovieId][dataJ.MovieId] += sim;
									cMatrix[dataI.MovieId][dataJ.MovieId] += cuij * sim;
								}
							}
							// non-personalized case
							else {
								wMatrix[dataI.MovieId][dataJ.MovieId] += 1;
								cMatrix[dataI.MovieId][dataJ.MovieId] += cuij;
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < number_of_movies; i++) {
			for (int j = 0; j < number_of_movies; j++) {
				if (!(wMatrix[i][j] == 0)) {
					cMatrix[i][j] *= 1.0 / wMatrix[i][j];
				}
				// TODO: trying to set null in K when not enough item pairs
				// TODO: when predicting I do not take into account null K values
				// TODO: null = 10.0
				else if (i != j) {
					cMatrix[i][j] = 10.0f;
				}
			}
		}
		this.kMatrix = cMatrix;
	}

	/**
		* Calculates a sorted set of predicted rankings. If a userId is given as a second parameter,
		* a personalized version is calculated. Otherwise, a global ranking. Uses real, i.e. sparse indices.
		* @param testMovies : set of test movie indices
		* @param userId : target user id
		* @param signCorrected : if signCorrected, use significance corrected similarities
		* @param withSession : if withSession, use session data
		* @return : a sorted set of movie id's
		*/
	public Set<Integer> predictRankingList(Set<Integer> testMovies, int userId, boolean signCorrected, boolean withSession, boolean withoutNullK) {
		HashMap<Integer,Double> map = new HashMap<Integer,Double>();
		ValueComparator bvc =  new ValueComparator(map);
		TreeMap<Integer,Double> sorted_map = new TreeMap<Integer,Double>(bvc);
		// in a non-personalized case, calculate global K matrix, otherwise user specific K matrix
		if (userId != -1) {
			calculateKMatrix(userId, signCorrected, withSession);
		} else {
			calculateKMatrix(-1, signCorrected, withSession);
		}
		for (Integer m : testMovies) {
			Integer mid = m_mMovieIds.get(m);
			// if a movie has no ratings in a training set, then the predicted score difference for it is null
			if (mid == null) map.put(m, null);
			// if WithoutNullK, do not count null K values in the average
			else if (withoutNullK) map.put(m, (double) nonNullAvg(kMatrix[mid]));
			else map.put(m, (double) nullAvg(kMatrix[mid]));
		}
		sorted_map.putAll(map);
		return sorted_map.keySet();
	}

	/**
		* Computes a list of lists of movies grouped and sorted according to the predicted score differences.
		* @param testMovies list of lists of test movies grouped by the rating value
		* @param userId target user id if personal, -1 if global
		* @param signCorrected true if using significance correction
		* @param withSession true if using session data (within 24 hours)
		* @param withoutNullK true if excluding null K values in average calculation
		* @return list of lists of movie ids
		*/
		public List<List<Integer>> predictRankingListWithTies(List<List<Integer>> testMovies, int userId,
			boolean signCorrected, boolean withSession,
	boolean withoutNullK) {

		Map<Double, List<Integer>> map = new HashMap<Double, List<Integer>>();
		// in a non-personalized case, calculate global K matrix, otherwise user specific K matrix
		if (userId != -1) {
			calculateKMatrix(userId, signCorrected, withSession);
		} else {
			calculateKMatrix(-1, signCorrected, withSession);
		}
		for (List<Integer> mList : testMovies) {
			for (Integer m : mList) {
				Integer mid = m_mMovieIds.get(m);
				// if a movie has no ratings in a training set, then the predicted rating for it is null
				if (mid != null) {
					List<Integer> list;
					// nonNullAvg sets null in K when not enough item pairs
					// null = 10.0
					Double key;
					if (withoutNullK) key = (double) nonNullAvg(kMatrix[mid]);
					else key = (double) nullAvg(kMatrix[mid]);
					if (map.get(key) == null) {
						list = new ArrayList<Integer>();
					} else {
						list = map.get(key);
					}
					list.add(m);
					map.put(key, list);
				}
			}
		}
		Map<Double, List<Integer>> treeMap = new TreeMap<Double, List<Integer>>(map);
		//System.out.println(treeMap);
		List<List<Integer>> lst = new ArrayList<List<Integer>>(treeMap.values());
		Collections.reverse(lst);
		//System.out.println(lst);
		return lst;
	}

	private float nonNullAvg(float array[]) {
		float sum = 0.0f;
		int count = 0;
		for (float k : array) {
			if (k != 10.0f) {
				sum += k;
				count += 1;
			}
		}
		//System.out.println("percentage of null K: "+ (array.length - count) / array.length);
		return sum / count;
	}

	/*public float nullKPercentage(float array[]) {
		float sum = 0.0f;
		int count = 0;
		for (float k : array) {
		if (k != 10.0f) {
		sum += k;
		count += 1;
		}
		}
		return (array.length - count) / array.length;
		} */

		private float nullAvg(float array[]) {
			float sum = 0.0f;
			for (float k : array) {
				if (k != 10.0f) {
					sum += k;
				}
			}
			return sum / array.length;
		}
	}