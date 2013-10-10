package models.algorithms.helpers;

import java.util.*;

/*************************************************************************
 *  Compilation:  javac KendallTau.java
 *  Execution:    java KendallTau N
 *
 *  Generate two random permutations of size N, then compute the
 *  Kendall tau distance between them.
 *
 *************************************************************************/


public class KendallTau {

    public static double kendalTau(int[] p, int[] q) {

        int N = p.length;

        try {
            assert N == q.length;
        } catch (Exception e) {
            System.err.println("Lengths of arrays must be equal");
            e.printStackTrace();
        }

        // inverse of 2nd permutation
        Map<Integer,Integer> inv = new HashMap<Integer, Integer>();

        for (int i = 0; i < N; i++) {
            inv.put(q[i], i);
        }

        // calculate Kendall tau distance
        int n_d = 0;
        int n_c = 0;
        for (int i = 0; i < N; i++) {
            for (int j = i+1; j < N; j++) {
                // check if p[i] and p[j] are inverted
                if (inv.get(p[i]) > inv.get(p[j])) n_d++;
                else n_c++;
            }
        }
        return N == 1 ? 0 : (n_c - n_d) / (N * (N - 1) / 2.0);
    }

    /**
     * Calculates S - the numerator of the Kendall Tau formula
     * @param p List of Integer ranked indexes
     * @param q List of Integer ranked indexes
     * @return S - the difference between the number of concordant pairs and discordant pairs
     */
    public static double kendalTauLists(List<Integer> p, List<Integer> q) {

        int N = p.size();

        try {
            assert N == q.size();
        } catch (Exception e) {
            System.err.println("Lengths of arrays must be equal");
            e.printStackTrace();
        }

        // inverse of 2nd permutation
        Map<Integer,Integer> inv = new HashMap<Integer, Integer>();

        for (int i = 0; i < N; i++) {
            inv.put(q.get(i), i);
        }

        // calculate Kendall tau distance
        int n_d = 0;  // number of discordant pairs
        int n_c = 0;  // number of concordant pairs
        for (int i = 0; i < N; i++) {
            for (int j = i+1; j < N; j++) {
                // check if p[i] and p[j] are inverted
                if (inv.get(p.get(i)) > inv.get(p.get(j))) n_d++;
                else n_c++;
            }
        }
        return N == 1 ? 0 : (n_c - n_d);
    }

    /**
    public static double kendalTauWithTies(int[] p, int[] q, double p_correction, double q_correction) {

        int N = p.length;

        try {
            assert N == q.length;
        } catch (Exception e) {
            System.err.println("Lengths of arrays must be equal");
            e.printStackTrace();
        }

        // inverse of 2nd permutation
        Map<Integer,Integer> inv = new HashMap<Integer, Integer>();

        for (int i = 0; i < N; i++) {
            inv.put(q[i], i);
        }

        // calculate Kendall tau distance
        int tau = 0;
        for (int i = 0; i < N; i++) {
            for (int j = i+1; j < N; j++) {
                // check if p[i] and p[j] are inverted
                if (inv.get(p[i]) > inv.get(p[j])) tau++;
            }
        }
        return N == 1 ? 0 : tau / Math.sqrt(((N * (N - 1) - p_correction) / 2.0) * ((N * (N - 1) - q_correction) / 2.0));
    }
    **/

    public static double kendalTauWithTies(List<List<Integer>> p, List<List<Integer>> q) {
        List<Integer>  pList = new ArrayList<Integer>();
        List<Integer>  qList = new ArrayList<Integer>();
        double pCorrection = 0.0;
        double qCorrection = 0.0;

        for (List<Integer> lst : p) {
            int nrTies = lst.size();
            pCorrection += nrTies * (nrTies - 1) / 2.0;
            pList.addAll(lst);
        }

        for (List<Integer> lst : q) {
            int nrTies = lst.size();
            qCorrection += nrTies * (nrTies - 1) / 2.0;
            qList.addAll(lst);
        }

        int n = pList.size();
        int pairs = n * (n - 1) / 2;

        // S is the difference between the number of concordant pairs and discordant pairs
        double s = kendalTauLists(pList, qList);
        double res;
        if (pairs == pCorrection || pairs == qCorrection) res = 0.0;
        else res = s / Math.sqrt((n * (n - 1) / 2.0 - pCorrection) * (n * (n - 1) / 2.0 - qCorrection));
        return res;
    }
}