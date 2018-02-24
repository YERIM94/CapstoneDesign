package aka.capstonedesign;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 예림 on 2017-11-08.
 */

public class Profile {
    private String name;
    private int age;
    private String varieties;
    private String weight;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getVarieties() {
        return varieties;
    }

    public void setVarieties(String varieties) {
        this.varieties = varieties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", age);
        result.put("varieties", varieties);
        result.put("weight", weight);

        return result;
    }
}
