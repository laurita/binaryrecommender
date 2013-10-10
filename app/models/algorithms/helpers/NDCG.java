package models.algorithms.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: laura
 * Date: 5/15/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class NDCG {

    /**
     * Compute the normalized discounted cumulative gain (NDCG) of a list of ranked items
     * See http://recsyswiki.com/wiki/Discounted_Cumulative_Gain
     * @param rankedItems : a list of ranked item id's
     * @param correctItems : a set of relevant items
     * @return : normalized discounted cumulative gain
     */
    public static double ndcg(List<List<Integer>> rankedItems, List<List<Integer>> correctItems) {
        double dcg = 0;

        List<Integer> rankedList = new ArrayList<Integer>();
        int i = 0;
        while (rankedList.size() <= 10) {
            rankedList.addAll(rankedItems.get(i));
            i++;
        }
        rankedList = rankedList.subList(0, 10);

        i = 0;
        List<Integer> correctList = new ArrayList<Integer>();
        while (correctList.size() <= 10) {
            correctList.addAll(correctItems.get(i));
            i++;
        }

        double idcg = getIDCG(correctList.size());

        for (int j = 0; j < rankedList.size(); j++) {
            int itemId = rankedList.get(j);

            if (!correctList.contains(itemId))
                continue;

            // compute NDCG part
            int rank = j + 1;
            dcg += 1 / (Math.log(rank + 1)/Math.log(2));
        }

        return dcg / idcg;
    }

    /// <summary>Computes the ideal DCG given the number of positive items.</summary>
    /// <remarks>
    /// See http://recsyswiki.com/wiki/Discounted_Cumulative_Gain
    /// </remarks>
    /// <returns>the ideal DCG</returns>
    /// <param name='n'>the number of positive items</param>
    static double getIDCG(int n) {
        double idcg = 0;
        for (int i = 0; i < n; i++) {
            idcg += 1 / (Math.log(i + 2)/Math.log(2));
        }
        return idcg;
    }
}
