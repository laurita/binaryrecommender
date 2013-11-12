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
  //public float similarities[][];          // user-user similarities (matrix of Pearson correlations)
  //public float signSimilarities[][];      // significance corrected user-user similarities [Herlocker et al., 1999]
  
  public float similarities[];        // user similarities (array of Pearson correlations)
  public float signSimilarities[];    // significance corrected user similarities [Herlocker et al., 1999]
  
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
	
  public static List<BinaryPreference> loadComparisons() {
    List<BinaryPreference> prefs = new ArrayList<BinaryPreference>();
    String sql = "select * from preference where value is not null";
    SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
    List<SqlRow> list = sqlQuery.findList();
    for (SqlRow row : list) {
      int userId = row.getInteger("user_id");
      int movie1Id = row.getInteger("movie1_id");
      int movie2Id = row.getInteger("movie2_id");
      int value = row.getInteger("value");
      boolean additional = row.getBoolean("additional");
      BinaryPreference pref = new BinaryPreference(userId, movie1Id, movie2Id, value, additional);
      prefs.add(pref);
    }
    return prefs;
  }

  public void initialize(int userId) {
    loadArrays();
    calcMetrics();
    calcUserItemMatrix();
    calcUserItemItemMatrix(userId);
    calcSimilarities(userId);
    //calculateKMatrix(userId, false);
    //updateKMatrixWithPrefs(userId, false);
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

    for (BinaryPreference binPref : binPrefs) {
      int movie1Id = binPref.getItem1Id();
      int movie2Id = binPref.getItem2Id();
      int custId = binPref.getUserId();
      int ratingDiff = (int) binPref.getValue();
      boolean additional = binPref.additional;

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
      m_aBinPrefs[m_nBinPrefCount].Additional = additional;
      m_aBinPrefs[m_nBinPrefCount].Cache = 0;
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
        movie.MovieId = movieId;
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
        customer.MoviePairsPreferedBy = new ArrayList<BinaryData>();

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
        movie.MovieId = movie1Id;
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
        movie.MovieId = movie2Id;
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
        customer.MoviePairsPreferedBy = new ArrayList<BinaryData>();
        customer.AdditionalPrefs = new ArrayList<BinaryData>();

        m_aCustomers[cid] = customer;
      }

      // Swap sparse id for compact one
      binPref.CustId = cid;
      
      // add inary preference given by user
      m_aCustomers[cid].MoviePairsPreferedBy.add(binPref);
      
      if (binPref.Additional) {
        m_aCustomers[cid].AdditionalPrefs.add(binPref);
      }
      
      // add compact indexes to the lists
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
    * Calculate user x item matrix containing Data objects.
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
    * Calculate user x item x item matrix containing rating differences.
    */
  public void calcUserItemItemMatrix(int userId) {
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

  public void calcSimilarities(int userId) {
    float[] similarities = new float[number_of_customers];
    float[] signSimilarities = new float[number_of_customers];
    int commonPairs;

    if (m_mCustIds.containsKey(userId)) {
      int u = m_mCustIds.get(userId);
      Customer c1 = m_aCustomers[u];
      int cust1Id = c1.CustomerId;
      int[][] u_ratingDiffs;
      ArrayList<BinaryData> strongerBiasFor = m_aCustomers[u].AdditionalPrefs;
    
      for (int v = 0; v < number_of_customers; v++) {
        Customer c2 = m_aCustomers[v];
        int cust2Id = c2.CustomerId;
        if (cust1Id == cust2Id) {
          similarities[v] = 1;
          signSimilarities[v] = 1;
        } else {
          if (c1.newCust) {
            u_ratingDiffs = userItemItemMatrix[u];
            int[][] v_ratingDiffs;
            if (c2.newCust) {
              v_ratingDiffs = userItemItemMatrix[v];
            } else {
              v_ratingDiffs = calcRatingDiffs(v);
            }
            similarities[v] = binarySimilarity(u_ratingDiffs, v_ratingDiffs, strongerBiasFor);
            commonPairs = commonPairs(u_ratingDiffs, v_ratingDiffs);
            signSimilarities[v] = similarities[v] * Math.min(commonPairs, gamma) / gamma;
          } else {
            u_ratingDiffs = calcRatingDiffs(u);
            if (c2.newCust) {
              int[][] v_ratingDiffs = userItemItemMatrix[v];
              similarities[v] = binarySimilarity(u_ratingDiffs, v_ratingDiffs, strongerBiasFor);
              commonPairs = commonPairs(u_ratingDiffs, v_ratingDiffs);
              signSimilarities[v] = similarities[v] * Math.min(commonPairs, gamma) / gamma;
            } else {
              ArrayList<Integer> uv_movies = new ArrayList<Integer>(c1.MoviesRatedBy);
              uv_movies.retainAll(c2.MoviesRatedBy);
              similarities[v] = similarity(u, v, uv_movies);
              signSimilarities[v] = similarities[v] * Math.min(uv_movies.size(), gamma) / gamma;
            }
          }
        }
      }
    }
    this.similarities = similarities;
    this.signSimilarities = signSimilarities;
  }
  
  private float similarity(int u, int v, ArrayList<Integer> uv_movies) {
    float similarity = 0;
    
    double ru = m_aCustomers[u].RatingAvg;
    double rv = m_aCustomers[v].RatingAvg;
    
    if (uv_movies.isEmpty()) {
      similarities[v] = 0;
      signSimilarities[v] = 0;
    } else {
      float numeratorSum = 0;
      float denominatorSumU = 0;
      float denominatorSumV = 0;
      for (int j : uv_movies) {
        int rum = userItemMatrix[u][j].Rating;
        int rvm = userItemMatrix[v][j].Rating;
        
        numeratorSum += (rum - ru) * (rvm - rv);
        denominatorSumU += Math.pow(rum - ru, 2);
        denominatorSumV += Math.pow(rvm - rv, 2);
      }
      double denominator = Math.sqrt(denominatorSumU * denominatorSumV);
      if (denominator == 0) {
        similarity = 0;
      } else {
        similarity = (float) (numeratorSum / denominator);
      }
    }
    return similarity;
  }

  private float binarySimilarity(int[][] arr1, int[][] arr2, ArrayList<BinaryData> strongerBiasFor) {
    
    int i, j;
    float corr=0, mag1=0, mag2=0;
    for (i=0; i<number_of_movies; i++) {
      for (j=0; j<number_of_movies; j++) {
        // -7 stands for null
        if ((i < j) && (arr1[i][j] != -7) && (arr2[i][j] != -7)) {
          mag1 += arr1[i][j] * arr1[i][j];
          mag2 += arr2[i][j] * arr2[i][j];
          corr += arr1[i][j] * arr2[i][j];
        }
      }
    }
    
    // add stronger bias for additional pairs
    for (BinaryData pref : strongerBiasFor) {
      i = pref.Movie1Id;
      j = pref.Movie2Id;
      // -7 stands for null
      if ((arr1[i][j] != -7) && (arr2[i][j] != -7)) {
        mag1 += 5 * arr1[i][j] * arr1[i][j];
        mag2 += 5 * arr2[i][j] * arr2[i][j];
        corr += 5 * arr1[i][j] * arr2[i][j];
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
    * @param userId : If userId = -1, non-personalized, otherwise user specific K matrix for user userId.
    * @param signCorrected : If signCorrected, then uses significance corrected similarities.
    */
  private void calculateKMatrix(int userId, boolean signCorrected) {
    
    System.out.println("calculating kmatrix for user " + userId + " ...");
    if (m_mCustIds.containsKey(userId)) {
      // in a non-personalized case userId is -1
      if (userId != -1) {
        // get the compact id of the user
        userId = m_mCustIds.get(userId);
      }

      cMatrix = new float[number_of_movies][number_of_movies];
      wMatrix = new float[number_of_movies][number_of_movies];
      kMatrix = new float[number_of_movies][number_of_movies];

      for (int u : m_mCustIds.values()) {

        ArrayList<Integer> u_movies = m_aCustomers[u].MoviesRatedBy;
        for (int i = 0; i < u_movies.size(); i++) {
          for (int j = 0; j < u_movies.size(); j++) {
          
            int mi = u_movies.get(i);
            int mj = u_movies.get(j);
                    
            if (mi != mj) {
              if (mi < mj) {
                Data dataI = userItemMatrix[u][mi];
                Data dataJ = userItemMatrix[u][mj];
            
                int cuij = dataI.Rating - dataJ.Rating;
                // personalized case
                if (userId != -1) {
                  double sim;
                  // depending on signCorrected argument get the similarity
                  if (signCorrected) {
                    sim = signSimilarities[u];
                  } else {
                    sim = similarities[u];
                  }
                  // add only those weighted score differences for which the user-user similarity is > 0
                  if (sim > 0) {
                    wMatrix[mi][mj] += sim;
                    cMatrix[mi][mj] += cuij * sim;
                  }
                }
                // non-personalized case
                else {
                  wMatrix[mi][mj] += 1;
                  cMatrix[mi][mj] += cuij;
                }
              } else {
                wMatrix[mi][mj] = wMatrix[mj][mi];
                cMatrix[mi][mj] = -cMatrix[mj][mi];
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
  }

  private void updateKMatrixWithPrefs(int userId, boolean signCorrected) {

    System.out.println("updating kmatrix for user " + userId + " ...");
    
    if (m_mCustIds.containsKey(userId)) {
      // in a non-personalized case userId is -1
      if (userId != -1) {
        // get the compact id of the user
        userId = m_mCustIds.get(userId);
      }

      for (int u : m_mCustIds.values()) {
        if (m_aCustomers[u].newCust) {
          ArrayList<BinaryData> u_moviePairs = m_aCustomers[u].MoviePairsPreferedBy;

          for (int i = 0; i < u_moviePairs.size(); i++) {
            BinaryData pair = u_moviePairs.get(i);

            int mi = pair.Movie1Id;
            int mj = pair.Movie2Id;

            int cuij = userItemItemMatrix[u][mi][mj];
            // personalized case
            if (userId != -1) {
              double sim;
              // depending on signCorrected argument get the similarity
              if (signCorrected) {
                sim = signSimilarities[u];
              } else {
                sim = similarities[u];
              }
              // add only those weighted score differences for which the user-user similarity is > 0
              if (sim > 0) {
                wMatrix[mi][mj] += sim;
                cMatrix[mi][mj] += cuij * sim;
              }
            }
            // non-personalized case
            else {
              wMatrix[mi][mj] += 1;
              cMatrix[mi][mj] += cuij;
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
      updateKMatrixWithPrefs(userId, signCorrected);
    } else {
      calculateKMatrix(-1, signCorrected);
      //updateKMatrixWithPrefs(userId, signCorrected);
    }
    for (Integer m : unratedMovies) {
      Integer mid = m_mMovieIds.get(m);
      // if a movie has no ratings in a training set, then the predicted score difference for it is null
      if (mid == null || !m_mCustIds.containsKey(userId)) map.put(m, null);
      else map.put(m, (double) nullAvg(kMatrix[mid]));
    }
    sorted_map.putAll(map);
    List<Integer> list = new ArrayList<Integer>(sorted_map.keySet());
    return list;
  }
}