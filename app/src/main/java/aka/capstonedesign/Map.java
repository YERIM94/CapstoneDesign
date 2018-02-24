package aka.capstonedesign;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by 예림 on 2017-11-22.
 */

public class Map {
    String walkdate;
    String walktime;
    Double distance;
    String walkday;
    String memo;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getWalkday() {
        return walkday;
    }

    public void setWalkday(String walkday) {
        this.walkday = walkday;
    }

    public Map() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getWalkdate() {
        return walkdate;
    }

    public void setWalkdate(String walkdate) {
        this.walkdate = walkdate;
    }

    public String getWalktime() {
        return walktime;
    }

    public void setWalktime(String walktime) {
        this.walktime = walktime;
    }

    public java.util.Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("walkdate",walkdate);
        result.put("walktime",walktime);
        result.put("distance",distance);
        result.put("walkday",walkday);
        result.put("memo",memo);

        return result;
    }
}
