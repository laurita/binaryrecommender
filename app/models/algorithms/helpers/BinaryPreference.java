package models.algorithms.helpers;

public class BinaryPreference implements Comparable<BinaryPreference>{

    private int userId;

    private int item1Id;

    private int item2Id;

    private double value;

    private double realValue;

    public int getUserId() {
        return userId;
    }

    public int getItem1Id() {
        return item1Id;
    }

    public int getItem2Id() {
        return item2Id;
    }

    public double getValue() {
        return value;
    }

    public BinaryPreference(int userId, int item1Id, int item2Id, double value) {
        this.userId = userId;
        this.item1Id = item1Id;
        this.item2Id = item2Id;
        this.value = value;
    }

    public BinaryPreference(int userId, int item1Id, int item2Id, double value, double realValue) {
        this.userId = userId;
        this.item1Id = item1Id;
        this.item2Id = item2Id;
        this.value = value;
        this.realValue = realValue;
    }

    public BinaryPreference copy() {
        return new BinaryPreference(userId, item1Id, item2Id, 0, value);
    }

    public BinaryPreference binaryCopy() {
        return new BinaryPreference(userId, item1Id, item2Id, 0, 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BinaryPreference)) return false;
        BinaryPreference p = (BinaryPreference) obj;
        return (userId == p.userId && item1Id == p.item1Id && item2Id == p.item2Id && value == p.value);
    }

    @Override
    public int hashCode() {
        return userId + item1Id + item2Id;
    }

    public double getRealValue() {
        return realValue;
    }

    /**
     * Sorts in a descending order if sorted with Collections.sort()
     */
    @Override
    public int compareTo(BinaryPreference o) {
        return Double.compare(o.value, value);
    }

    @Override
    public String toString() {
        return String.valueOf(value) + " uid:" + userId + " itm1id:"+item1Id + " itm2id:"+item2Id;
    }
}
