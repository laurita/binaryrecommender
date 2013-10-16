package models.algorithms.helpers;

import java.util.ArrayList;
import java.util.List;

public class Customer {
	
    public int         CustomerId;
    public int         RatingCount;
    public int         RatingSum;
    public int		   OffsetSum = 0;
    public boolean     newCust = false;

    public double      RatingAvg;
    public double      RatingDiffAvg;

    // I added this
    public ArrayList<Integer>   MoviesRatedBy;
    public ArrayList<List<Integer>> MoviePairsPreferedBy;

}