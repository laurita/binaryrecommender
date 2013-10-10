package models.algorithms.helpers;

import java.util.ArrayList;

public class Customer {
	
    public int         CustomerId;
    public int         RatingCount;
    public int         RatingSum;
    public int		   OffsetSum = 0;

    public double      RatingAvg;

    // I added this
    public ArrayList<Integer>   MoviesRatedBy;

}
