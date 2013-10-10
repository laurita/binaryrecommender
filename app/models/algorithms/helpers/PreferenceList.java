package models.algorithms.helpers;

import java.util.*;

public class PreferenceList {

	private Map<String, Preference> preferencesMap = null;

	private Map<Integer, List<Preference>> preferencesByItem = null;
	
	private Map<Integer, List<Preference>> preferencesByUser = null;

	private List<Preference> preferences;

	private List<Preference> leftPart = null;

	private List<Preference> middlePart = null;

	private List<Preference> rightPart = null;
	
	public PreferenceList(List<Preference> preferences) {
		this.preferences = preferences;
	}

	public PreferenceList(PreferenceList prefs1, PreferenceList prefs2) {
		this.preferences = new ArrayList<Preference>();
		this.preferences.addAll(prefs1.preferences);
		this.preferences.addAll(prefs2.preferences);
		// TODO do the same for the map
	}

	public PreferenceList(List<Preference> prefs1, List<Preference> prefs2) {
		this.preferences = new ArrayList<Preference>();
		this.preferences.addAll(prefs1);
		this.preferences.addAll(prefs2);
		// TODO do the same for the map
	}
	
	private void initPrefsMap() {
		if (preferencesMap == null) {
			preferencesMap = new HashMap<String, Preference>();
			for (Preference mpd : preferences) {
				preferencesMap.put(getKey(mpd), mpd);
			}
		}
	}

	private String getKey(Preference mpd) {
		return getKey(mpd.getUserId(), mpd.getItemId());
	}

	private String getKey(int userId, int itemId) {
		return userId + "_" + itemId;
	}

	public Preference getPreference(int userId, int itemId) {
		initPrefsMap();
		return preferencesMap.get(getKey(userId, itemId));
	}

	public List<Preference> getPreferences() {
		return preferences;
	}

	public PreferenceList getLeftPart() {
		return new PreferenceList(leftPart);
	}

	public PreferenceList getRightPart() {
		return new PreferenceList(rightPart);
	}

	public PreferenceList getMiddlePart() {
		return new PreferenceList(middlePart);
	}

	public List<Preference> getLeftPartList() {
		return leftPart;
	}

	public List<Preference> getRightPartList() {
		return rightPart;
	}

	public List<Preference> getMiddlePartList() {
		return middlePart;
	}

	public int size() {
		return preferences.size();
	}

	public PreferenceList getRandomGroup(int size) {
		List<Preference> lst = new ArrayList<Preference>(preferences);
		Collections.shuffle(lst);
		return new PreferenceList(lst.subList(0, size));
	}

	public Preference getRandomPreference() {
		Random random = new Random();
		int i = random.nextInt(preferences.size());
		return preferences.get(i);
	}

	public PreferenceList removePreference(Preference pref) {
		List<Preference> newPrefs = new ArrayList<Preference>(preferences);
		newPrefs.remove(pref);
		return new PreferenceList(newPrefs);
	}

	public PreferenceList addPreferences(List<Preference> prefsToAdd) {
		List<Preference> newPrefs = new ArrayList<Preference>(preferences);
		newPrefs.addAll(prefsToAdd);
		return new PreferenceList(newPrefs);
	}

	public PreferenceList removePreferences(PreferenceList group) {
		return removePreferences(group.getPreferences());
	}

	public PreferenceList removePreferences(List<Preference> prefsToRemove) {
		List<Preference> newPrefs = new ArrayList<Preference>(preferences);
		newPrefs.removeAll(prefsToRemove);
		return new PreferenceList(newPrefs);
	}

	public void splitRandomly(int i, int j, int k) {
		Collections.shuffle(preferences);
		this.leftPart = new ArrayList<Preference>(preferences.subList(0, i));
		this.middlePart = new ArrayList<Preference>(preferences.subList(i, i
				+ j));
		this.rightPart = new ArrayList<Preference>(preferences.subList(i + j, i
				+ j + k));
	}

	public Collection<Integer> getItemIds() {
		Set<Integer> ids = new HashSet<Integer>();
		for (Preference p : preferences) {
			ids.add(p.getItemId());
		}
		return ids;
	}
	
	public Set<Integer> getItemIds(int userId) {
		List<Preference> userPrefs = getPreferencesByUser(userId);
		if (userPrefs == null) return new HashSet<Integer>();
		Set<Integer> result = new HashSet<Integer>();
		for (Preference p : userPrefs) {
			result.add(p.getItemId());
		}
		return result;
	}

	public List<Preference> getPreferencesByItem(int itemId) {
		initPrefsByItem();
		List<Preference> prefs = preferencesByItem.get(itemId);
		if (prefs == null) prefs = new ArrayList<Preference>();
		return prefs;
	}

	private void initPrefsByItem() {
		if (preferencesByItem == null) {
			preferencesByItem = new HashMap<Integer, List<Preference>>();
			for (Preference p : preferences) {
				int itemId = p.getItemId();
				List<Preference> itemPrefs = preferencesByItem.get(itemId);
				if (itemPrefs == null) {
					itemPrefs = new ArrayList<Preference>();
					preferencesByItem.put(itemId, itemPrefs);
				}
				itemPrefs.add(p);
			}
		}
	}
	
	public List<Preference> getPreferencesByUser(int userId) {
		initPrefsByUser();
		List<Preference> prefs = preferencesByUser.get(userId);
		if (prefs == null) return new ArrayList<Preference>();
		return prefs;
	}
	
	public List<Preference> getPreferences(int userId, Collection<Integer> itemIds) {
		List<Preference> userPrefs = getPreferencesByUser(userId);
		if (userPrefs == null || userPrefs.size() == 0) return new ArrayList<Preference>();		
		List<Preference> result = new ArrayList<Preference>();
		for (Preference p : userPrefs) {
			if (itemIds.contains(p.getItemId())) {
				result.add(p);
			}
		}
		return result;
	}

	private void initPrefsByUser() {
		if (preferencesByUser == null) {
			preferencesByUser = new HashMap<Integer, List<Preference>>();
			for (Preference p : preferences) {
				int userId = p.getUserId();
				List<Preference> userPrefs = preferencesByUser.get(userId);
				if (userPrefs == null) {
					userPrefs = new ArrayList<Preference>();
					preferencesByUser.put(userId, userPrefs);
				}
				userPrefs.add(p);
			}
		}
	}	

	public PreferenceList addPreference(Preference preference) {
		List<Preference> newPrefs = new ArrayList<Preference>(preferences);
		newPrefs.add(preference);
		return new PreferenceList(newPrefs);
	}

	public List<Integer> getUserIds() {
		Set<Integer> ids = new HashSet<Integer>();
		for (Preference p : preferences) {
			ids.add(p.getUserId());
		}
		return new ArrayList<Integer>(ids);
	}

	public List<Preference> getTopPreferences(int uid, int numberOfPrefs) {
		List<Preference> prefs = getPreferencesByUser(uid);
		if (prefs == null) return new ArrayList<Preference>();
		Collections.sort(prefs);
		if (numberOfPrefs > prefs.size()) numberOfPrefs = prefs.size();
		prefs = prefs.subList(0, numberOfPrefs);
		return prefs;
	}

	public List<Preference> getTopPreferencesPrc(int uid, int percentile) {
		List<Preference> prefs = getPreferencesByUser(uid);
		Collections.sort(prefs);
		int i = (int) (percentile * prefs.size() / 100.0);
		double value = prefs.get(i).getValue();
		List<Preference> result = new ArrayList<Preference>();
		for (Preference p : prefs) {
			if (p.getValue() >= value) {
				result.add(p);
			}
		}
		return result;
	}

	public List<Preference> getPreferencesByItems(Collection<Integer> unclearItemIds) {
		List<Preference> result = new ArrayList<Preference>();
		for (Preference p : preferences) {
			if (unclearItemIds.contains(p.getItemId()))
				result.add(p);
		}
		return result;
	}

	public static Collection<Integer> getItemIds(List<Preference> prefs, int userId) {
		Set<Integer> ids = new HashSet<Integer>();
		for (Preference p : prefs) {
			if (p.getUserId() == userId)
				ids.add(p.getItemId());
		}
		return ids;
	}
	
	public static Collection<Integer> getUserIds(List<Preference> preferences) {
		Set<Integer> ids = new HashSet<Integer>();
		for (Preference p : preferences) {
			ids.add(p.getUserId());
		}
		return ids;
	}
	
	/**
	 * returns preferences of the specific userId and for specific items
	 * @param prefs
	 * @param userId
	 * @param itemIds
	 * @return
	 */
	public static List<Preference> getPreferences(Collection<Preference> prefs, int userId, Collection<Integer> itemIds) {
		List<Preference> result = new ArrayList<Preference>();
		for (Preference p : prefs) {
			if (p.getUserId() == userId && itemIds.contains(p.getItemId())) 
				result.add(p);
		}
		return result;
	}
	
	public static List<Integer> getItemIds(Collection<Preference> preferences) {
		Set<Integer> ids = new HashSet<Integer>();
		for (Preference p : preferences) {
			ids.add(p.getItemId());
		}
		return new ArrayList<Integer>(ids);
	}

}
