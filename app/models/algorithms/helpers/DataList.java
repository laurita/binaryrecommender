package models.algorithms.helpers;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: laura
 * Date: 4/22/13
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataList {

    List<Data> dataList = new ArrayList<Data>();

    public Map<Integer, List<Data>> dataByMovie = new HashMap<Integer, List<Data>>();
    public Map<Integer, List<Data>> dataByUser = new HashMap<Integer, List<Data>>();

    public DataList() {
        this.dataList = new ArrayList<Data>();
    }

    public DataList(Data[] data) {
        dataList = new ArrayList<Data>();
        for (Data d : data) {
            dataList.add(d);
        }
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public Data getDatum(int userId, int movieId) {
        Data result = null;
        for (Data d : dataList) {
            if (d.CustId == userId && d.MovieId == movieId)
                result = d;
        }
        return result;
    }

    public void addData(Data data) {
        dataList.add(data);
    }

    public List<Data> getDataByMovie(int movieId) {
        //initDataByMovie();
        List<Data> result = dataByMovie.get(movieId);
        if (result == null) result = new ArrayList<Data>();
        return result;
    }

    public List<Data> getDataByUser(int userId) {
        //initDataByMovie();
        List<Data> result = dataByUser.get(userId);
        if (result == null) result = new ArrayList<Data>();
        return result;
    }

    /*
    private void initDataByMovie() {
        if (dataByMovie == null) {
            dataByMovie = new HashMap<Integer, List<Data>>();
            for (Data d : dataList) {
                int movieId = d.MovieId;
                List<Data> movieData = dataByMovie.get(movieId);
                if (movieData == null) {
                    movieData = new ArrayList<Data>();
                    dataByMovie.put(movieId, movieData);
                }
                movieData.add(d);
            }
        }
    }
    */
}
