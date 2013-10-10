package models.algorithms.helpers;

import java.util.ArrayList;

public class Movie {

    public int         MovieId;
    public int         RatingCount;
    public int         RatingSum;
    public double      RatingAvg;
    public double      PseudoAvg;            // Weighted average used to deal with small movie counts

    // I added this
    public ArrayList<Integer>   UsersRatedIt;         // Array of usersID's that rated this movie
}
