package models.algorithms.helpers;

public class BinaryData {
    public int          CustId;
    public int          Movie1Id;
    public int          Movie2Id;
    public byte         RatingDiff;
    public float        Cache;

    @Override
    public String toString() {
        return "CustId: "+ CustId + ", Movie1Id: "+ Movie1Id + ", Movie2Id: "+ Movie2Id + ", Rating: "+ RatingDiff;
    }
}
