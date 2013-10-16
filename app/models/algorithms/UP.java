package models.algorithms;

import models.algorithms.helpers.*;
import java.util.*;
import com.avaje.ebean.*;
import static java.util.Arrays.deepToString;

public class UP {

  public static float gamma = 7.0f;

  private List<Preference> prefs;   // list of preferences
  private List<BinaryPreference> binPrefs; // list of binary preferences

  public int number_of_movies;
  public int number_of_customers;

  public int m_nRatingCount;              // Current number of loaded ratings
  public int m_nBinPrefCount;              // Current number of loaded binary preferences
  private Data m_aRatings[];              // Array of ratings data
  private BinaryData m_aBinPrefs[];              // Array of binary preference data
  private Movie m_aMovies[];              // Array of movie metrics
  private Customer m_aCustomers[];        // Array of customer metrics

  public Map<Integer, Integer> m_mCustIds = new HashMap<Integer, Integer>();       // map of real userIds to compact userIds
  public Map<Integer, Integer> m_mMovieIds = new HashMap<Integer, Integer>();      // map of real movieIds to compact movieIds

  public Data userItemMatrix[][];         // user-item matrix of ratings
  public int userItemItemMatrix[][][];         // user-item-item matrix of rating differences
  public float similarities[][];          // user-user similarities (matrix of Pearson correlations)
  public float signSimilarities[][];      // significance corrected user-user similarities [Herlocker et al., 1999]
  public float kMatrix[][];               // K matrix (either personalized or non-personalized, as specified)
  public float cMatrix[][];               // K matrix numerators
  public float wMatrix[][];               // K matrix denominators

  //-------------------------------------------------------------------
  // Initialization
  //-------------------------------------------------------------------
  public UP(List<Preference> prefs) {

    this.prefs = prefs;
    this.binPrefs = new ArrayList<BinaryPreference>();

    Collections.sort(this.prefs, new Comparator<Preference>() {
      @Override
      public int compare(Preference p1, Preference p2) {
        int item = p1.getItemId() - p2.getItemId();
        if (item != 0) return item;
        return p1.getUserId() - p2.getUserId();
      }
    });
  }

  //-------------------------------------------------------------------
  // Initialization
  //-------------------------------------------------------------------
  public UP(List<Preference> prefs, List<BinaryPreference> binPrefs) {

    this.prefs = prefs;
    this.binPrefs = binPrefs;

    Collections.sort(this.prefs, new Comparator<Preference>() {
      @Override
      public int compare(Preference p1, Preference p2) {
        int item = p1.getItemId() - p2.getItemId();
        if (item != 0) return item;
        return p1.getUserId() - p2.getUserId();
      }
    });

    Collections.sort(this.binPrefs, new Comparator<BinaryPreference>() {
      @Override
      public int compare(BinaryPreference p1, BinaryPreference p2) {
        int item1 = p1.getItem1Id() - p2.getItem1Id();
        if (item1 != 0) return item1;
        int item2 = p1.getItem2Id() - p2.getItem2Id();
        if (item2 != 0) return item2;
        return p1.getUserId() - p2.getUserId();
      }
    });
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
	
  public static List<List<Integer>> loadComparisons() {
    List<List<Integer>> prefs = new ArrayList<List<Integer>>();
    String sql = "select * from preference";
    SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
    List<SqlRow> list = sqlQuery.findList();

    for (SqlRow row : list) {
      int userId = row.getInteger("user_id");
      int movie1Id = row.getInteger("movie1_id");
      int movie2Id = row.getInteger("movie2_id");
      int value = row.getInteger("value");
      Integer[] array = {userId, movie1Id, movie2Id, value};
      List<Integer> pref = new ArrayList<Integer>(Arrays.asList(array));
      prefs.add(pref);
    }
    return prefs;
  }

  /**
    * Initializes the model: load the arrays, calculate metrics, user-item matrix and user-user similarities
    */
  public void initialize() {
    loadArrays();
    calcMetrics();
    calcUserItemMatrix();
    calcUserItemItemMatrix();
    calcSimilarities();
  }

  /**
    * Processes the file and loads the ratings array.
    * Initializes empty arrays for movies and users.
    */
  private void loadArrays() {

    m_nRatingCount = 0;
    m_nBinPrefCount = 0;
    m_aRatings = new Data[prefs.size()];                        // Array of ratings data
    m_aBinPrefs = new BinaryData[binPrefs.size()];                        // Array of ratings data

    Set<Integer> movieIds = new HashSet<Integer>();
    Set<Integer> customerIds = new HashSet<Integer>();

    for (Preference pref : prefs) {
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

    for (BinaryPreference binPref : binPrefs) {
      int movie1Id = binPref.getItem1Id();
      int movie2Id = binPref.getItem2Id();
      int custId = binPref.getUserId();
      int ratingDiff = (int) binPref.getValue();
      long time = binPref.getTime();

      movieIds.add(movie1Id);
      movieIds.add(movie2Id);
      customerIds.add(custId);

      if (m_aBinPrefs[m_nBinPrefCount] == null) {
        m_aBinPrefs[m_nBinPrefCount] = new BinaryData();
      }

      m_aBinPrefs[m_nBinPrefCount].Movie1Id = (short)movie1Id;
      m_aBinPrefs[m_nBinPrefCount].Movie2Id = (short)movie2Id;
      m_aBinPrefs[m_nBinPrefCount].CustId = custId;
      m_aBinPrefs[m_nBinPrefCount].RatingDiff = (byte) ratingDiff;
      m_aBinPrefs[m_nBinPrefCount].Cache = 0;
      m_aBinPrefs[m_nBinPrefCount].Time = time;
      m_nBinPrefCount++;
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
    Integer mid, cid, m1id, m2id;

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
        movie.UsersPreferedIt = new ArrayList<Integer>();
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
        customer.MoviePairsPreferedBy = new ArrayList<List<Integer>>();

        m_aCustomers[cid] = customer;
      }

      // Swap sparse id for compact one
      rating.CustId = cid;

      m_aCustomers[cid].RatingCount++;
      m_aCustomers[cid].RatingSum += rating.Rating;

      // add compact indexes to the lists
      m_aCustomers[cid].MoviesRatedBy.add(rating.MovieId);
      m_aMovies[mid].UsersRatedIt.add(rating.CustId);

    }

    // Process each row in the training set
    for (BinaryData binPref : m_aBinPrefs) {

      int movie1Id = binPref.Movie1Id;
      int movie2Id = binPref.Movie2Id;
      m1id = m_mMovieIds.get(movie1Id);
      m2id = m_mMovieIds.get(movie2Id);
      if (m1id == null) {
        m1id = m_mMovieIds.size();
        m_mMovieIds.put(movie1Id, m1id);
        Movie movie = new Movie();
        movie.RatingCount = 0;
        movie.RatingSum = 0;
        // add the user that rated the movie to the list in the Movie object
        movie.UsersRatedIt = new ArrayList<Integer>();
        movie.UsersPreferedIt = new ArrayList<Integer>();
        m_aMovies[m1id] = movie;
      }
      if (m2id == null) {
        m2id = m_mMovieIds.size();
        m_mMovieIds.put(movie2Id, m2id);
        Movie movie = new Movie();
        movie.RatingCount = 0;
        movie.RatingSum = 0;
        // add the user that rated the movie to the list in the Movie object
        movie.UsersRatedIt = new ArrayList<Integer>();
        movie.UsersPreferedIt = new ArrayList<Integer>();
        m_aMovies[m2id] = movie;
      }
      binPref.Movie1Id = m1id;
      binPref.Movie2Id = m2id;

      // Add customers (using a map to re-number id's to array indexes)
      int custId = binPref.CustId;
      cid = m_mCustIds.get(custId);
      if (cid == null) {
        cid = m_mCustIds.size();

        // Reserve new id and add lookup
        m_mCustIds.put(custId, cid);

        // Store off old sparse id for later
        Customer customer = new Customer();
        customer.CustomerId = custId;
        customer.newCust = true;
        customer.RatingCount = 0;
        customer.RatingSum = 0;
        customer.MoviesRatedBy = new ArrayList<Integer>();
        customer.MoviePairsPreferedBy = new ArrayList<List<Integer>>();

        m_aCustomers[cid] = customer;
      }

      // Swap sparse id for compact one
      binPref.CustId = cid;

      // add compact indexes to the lists
      List<Integer> pair = new ArrayList<Integer>();
      pair.add(m1id);
      pair.add(m2id);
      m_aCustomers[cid].MoviePairsPreferedBy.add(pair);
      m_aMovies[m1id].UsersPreferedIt.add(cid);
      m_aMovies[m2id].UsersPreferedIt.add(cid);
    }

    for (Movie movie : m_aMovies) {
      movie.RatingAvg = movie.RatingSum / (1.0 * movie.RatingCount);
    }

    for (Customer cust : m_aCustomers) {
      cust.RatingAvg = cust.RatingSum / (1.0 * cust.RatingCount);
    }
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
    * Calculated user x item matrix containing Data objects.
    */
  public void calcUserItemItemMatrix() {
    int[][][] userItemItemMatrix = new int[number_of_customers][number_of_movies][number_of_movies];
    for (int u = 0; u < number_of_customers; u++) {
      for (int i = 0; i < number_of_movies; i++) {
        Arrays.fill(userItemItemMatrix[u][i], -7);
      }
    }
    for (int k = 0; k < m_nBinPrefCount; k++) {
      BinaryData pref = m_aBinPrefs[k];
      int u = pref.CustId;
      int i = pref.Movie1Id;
      int j = pref.Movie2Id;
      userItemItemMatrix[u][i][j] = -pref.RatingDiff;
      userItemItemMatrix[u][j][i] = +pref.RatingDiff;
    }
    this.userItemItemMatrix = userItemItemMatrix;
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
    int commonPairs;

    for (int u = 0; u < number_of_customers; u++) {
      Customer c1 = m_aCustomers[u];
      ArrayList<Integer> u_movies = c1.MoviesRatedBy;
      double ru = m_aCustomers[u].RatingAvg;
      int[][] u_calcRatingDiffs = calcRatingDiffs(u);
      for (int v = 0; v < number_of_customers; v++) {
        if (u == v) {
          similarities[u][v] = 1;
          signSimilarities[u][v] = 1;
        } else if (u < v) {
          Customer c2 = m_aCustomers[v];
          ArrayList<Integer> v_movies = c2.MoviesRatedBy;
          if (c1.newCust) {
            int[][] u_ratingDiffs = userItemItemMatrix[u];
            int[][] v_ratingDiffs;
            if (c2.newCust) {
              v_ratingDiffs = userItemItemMatrix[v];
            } else {
              v_ratingDiffs = calcRatingDiffs(v);
            }
            similarities[u][v] = binarySimilarity(u_ratingDiffs, v_ratingDiffs);
            commonPairs = commonPairs(u_ratingDiffs, v_ratingDiffs);
            signSimilarities[u][v] = similarities[u][v] * Math.min(commonPairs, gamma) / gamma;
          } else {
            if (c2.newCust) {
              int[][] v_ratingDiffs = userItemItemMatrix[v];
              similarities[u][v] = binarySimilarity(u_calcRatingDiffs, v_ratingDiffs);
              commonPairs = commonPairs(u_calcRatingDiffs, v_ratingDiffs);
              signSimilarities[u][v] = similarities[u][v] * Math.min(commonPairs, gamma) / gamma;
            } else {
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
        } else {
          similarities[u][v] = similarities[v][u];
          signSimilarities[u][v] = signSimilarities[v][u];
        }
      }
    }
    this.similarities = similarities;
    this.signSimilarities = signSimilarities;
  }

  private float binarySimilarity(int[][] arr1, int[][] arr2) {
    int i, j;
    float corr=0, mag1=0, mag2=0;
    for (i=0; i<number_of_movies; i++)
    for (j=0; j<number_of_movies; j++) {
      // -7 stands for null
      if ((i < j) && (arr1[i][j] != -7) && (arr2[i][j] != -7)) {
        mag1 += arr1[i][j] * arr1[i][j];
        mag2 += arr2[i][j] * arr2[i][j];
        corr += arr1[i][j] * arr2[i][j];
      }
    }
    float denom = (float)Math.sqrt(mag1*mag2);
    if (denom != 0) { corr /= denom; }
    return corr;
  }

  private int commonPairs(int[][] arr1, int[][] arr2) {
    int i, j;
    int count = 0;

    for (i=0; i<number_of_movies; i++)
    for (j=0; j<number_of_movies; j++) {
      // -7 stands for null
      if ((i < j) && (arr1[i][j] != -7) && (arr2[i][j] != -7)) {
        count++;
      }
    }
    return count;
  }

  private int[][] calcRatingDiffs(int u) {
    int[][] diffs = new int[number_of_movies][number_of_movies];
    // -7 stands for null
    for (int i = 0; i < number_of_movies; i++) {
      Arrays.fill(diffs[i], -7);
    }
    Customer c2 = m_aCustomers[u];
    for (int m1 : c2.MoviesRatedBy) {
      for (int m2 : c2.MoviesRatedBy) {
        if (m1 != m2) {
          diffs[m1][m2] = userItemMatrix[u][m1].Rating - userItemMatrix[u][m2].Rating;
        }
      }
    }
    return diffs;
  }

  /**
    * Calculates K matrix.
    * @param userID : If userID = -1, non-personalized, otherwise user specific K matrix for user userID.
    * @param signCorrected : If signCorrected, then uses significance corrected similarities.
    */
  public void calculateKMatrix(int userID, boolean signCorrected) {
    // in a non-personalized case userID is -1
    if (userID != -1) {
      // get the compact id of the user
      userID = m_mCustIds.get(userID);
    }

    cMatrix = new float[number_of_movies][number_of_movies];
    wMatrix = new float[number_of_movies][number_of_movies];
    kMatrix = new float[number_of_movies][number_of_movies];

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

    for (int i = 0; i < number_of_movies; i++) {
      System.arraycopy(cMatrix[i], 0, this.kMatrix[i], 0, cMatrix[i].length);
    }

    for (int i = 0; i < number_of_movies; i++) {
      for (int j = 0; j < number_of_movies; j++) {
        if (!(wMatrix[i][j] == 0)) {
          kMatrix[i][j] *= 1.0 / wMatrix[i][j];
        }
      }
    }
  }

  public void updateKMatrixWithPrefs(int userID, boolean signCorrected) {

    // in a non-personalized case userID is -1
    if (userID != -1) {
      // get the compact id of the user
      userID = m_mCustIds.get(userID);
    }

    for (int u : m_mCustIds.values()) {
      if (m_aCustomers[u].newCust) {
        ArrayList<List<Integer>> u_moviePairs = m_aCustomers[u].MoviePairsPreferedBy;

        for (int i = 0; i < u_moviePairs.size(); i++) {
          List<Integer> pair = u_moviePairs.get(i);

          int m1 = pair.get(0);
          int m2 = pair.get(1);

          int cuij = userItemItemMatrix[u][m1][m2];
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
              wMatrix[m1][m2] += sim;
              wMatrix[m2][m1] += sim;
              cMatrix[m1][m2] += cuij * sim;
              cMatrix[m2][m1] -= cuij * sim;
            }
          }
          // non-personalized case
          else {
            wMatrix[m1][m2] += 1;
            wMatrix[m2][m1] += 1;
            cMatrix[m1][m2] += cuij;
            cMatrix[m2][m1] -= cuij;
          }
        }
      }
    }

    for (int i = 0; i < number_of_movies; i++) {
      System.arraycopy(cMatrix[i], 0, kMatrix[i], 0, cMatrix[i].length);
    }

    for (int i = 0; i < number_of_movies; i++) {
      for (int j = 0; j < number_of_movies; j++) {
        if (!(wMatrix[i][j] == 0)) {
          kMatrix[i][j] *= 1.0 / wMatrix[i][j];
        }
      }
    }
  }

  private float nullAvg(float array[]) {
    float sum = 0.0f;
    for (float k : array) {
      if (k != 10.0f) {
        sum += k;
      }
    }
    return sum / array.length;
  }

  public List<Integer> predictRankingList(int userId, List<Integer> unratedMovies, boolean signCorrected) {
    HashMap<Integer,Double> map = new HashMap<Integer,Double>();
    ValueComparator bvc =  new ValueComparator(map);
    TreeMap<Integer,Double> sorted_map = new TreeMap<Integer,Double>(bvc);
    // in a non-personalized case, calculate global K matrix, otherwise user specific K matrix
    if (userId != -1) {
      calculateKMatrix(userId, signCorrected);
    } else {
      calculateKMatrix(-1, signCorrected);
    }
    for (Integer m : unratedMovies) {
      Integer mid = m_mMovieIds.get(m);
      // if a movie has no ratings in a training set, then the predicted score difference for it is null
      if (mid == null) map.put(m, null);
      else map.put(m, (double) nullAvg(kMatrix[mid]));
    }
    sorted_map.putAll(map);
    List<Integer> list = new ArrayList<Integer>(sorted_map.keySet());
    return list;
  }
}
