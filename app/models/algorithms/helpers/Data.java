package models.algorithms.helpers;

public class Data {
	
    public int          CustId;
    public int          MovieId;
    public byte         Rating;
    public float        Cache;
    public long         Time;

    @Override
    public String toString() {
        return "CustId: "+ CustId + ", MovieId: "+ MovieId + ", Rating: "+ Rating + ", Time: " + Time;
    }

}
