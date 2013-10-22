package models.algorithms.helpers;

public class Preference implements Comparable<Preference>{

	private int userId;
	
	private int itemId;
	
	private double value;
	
	private double realValue;
  			
	public int getUserId() {
		return userId;
	}

	public int getItemId() {
		return itemId;
	}

	public double getValue() {
		return value;
	}

	public Preference(int userId, int itemId, double value) {
		this.userId = userId;
		this.itemId = itemId;
		this.value = value;
	}
	
	public Preference(int userId, int itemId, double value, double realValue) {
		this.userId = userId;
		this.itemId = itemId;
		this.value = value;
		this.realValue = realValue;
	}

	public Preference copy() {		
		return new Preference(userId, itemId, 0, value);
	}
	
	public Preference binaryCopy() {
		return new Preference(userId, itemId, 0, 1);
	}
	
	@Override
	public boolean equals(Object obj) {		
		if (!(obj instanceof Preference)) return false;
		Preference p = (Preference) obj;
		return (userId == p.userId && itemId == p.itemId && value == p.value);
	}
	
	@Override
	public int hashCode() {		
		return userId + itemId;
	}
	
	public double getRealValue() {
		return realValue;
	}
	
	/**
		* Sorts in a descending order if sorted with Collections.sort()
		*/
		@Override
	public int compareTo(Preference o) {
		return Double.compare(o.value, value);
	}
	
	@Override
	public String toString() {	
		return String.valueOf(value) + " uid:" + userId + " itmid:"+itemId;
	}
	
}
