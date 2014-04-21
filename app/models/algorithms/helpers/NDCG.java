package models.algorithms.helpers;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Computes the ideal DCG given the number of positive items
     * See http://recsyswiki.com/wiki/Discounted_Cumulative_Gain
     * @param n : the number of positive items
     * @return : the ideal DCG
     */
    static double getIDCG(int n) {
        double idcg = 0;
        for (int i = 0; i < n; i++) {
            idcg += 1 / (Math.log(i + 2)/Math.log(2));
        }
        return idcg;
    }
}
