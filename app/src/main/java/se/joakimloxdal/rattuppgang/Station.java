package se.joakimloxdal.rattuppgang;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by joakimloxdal on 2018-10-17.
 */

public class Station {
    private String name;
    private int level;
    private Integer[] lines;
    private ArrayList<Entrance> entrances;

    public Station(String name, int level, JSONArray JSONlines) throws JSONException {
        this.entrances = new ArrayList<Entrance>(5);
        this.name = name;
        this.level = level;

        // Add subway lines to array
        this.lines = new Integer[JSONlines.length()];
        for(int i = 0; i<JSONlines.length(); i++){
            lines[i] = JSONlines.getInt(i);
        }
    }

    public void addEntrances(JSONArray entrancesList) throws JSONException {
        for(int i = 0; i<entrancesList.length(); i++){
            JSONObject temp_entrance_JSON = entrancesList.getJSONObject(i);
            String placement = temp_entrance_JSON.getString("placering");
            String name = temp_entrance_JSON.getString("namn");
            String coordinates = null;
            if(temp_entrance_JSON.has("pos")){
                coordinates = temp_entrance_JSON.getString("pos");
            }

            Entrance entrance;
            if(coordinates!=null){
                entrance = new Entrance(name, placement, coordinates);
            }else{
                entrance = new Entrance(name, placement);
            }
            entrances.add(entrance);
        }
    }

    public boolean onSameLine(Station other_station){
        return(numIntersects(this.lines, other_station.lines)>0);
    }

    public int numIntersects(Integer[] array1, Integer[] array2){
        Set<Integer> s1 = new HashSet<Integer>(Arrays.asList(array1));
        Set<Integer> s2 = new HashSet<Integer>(Arrays.asList(array2));
        s1.retainAll(s2);

        Integer[] result = s1.toArray(new Integer[s1.size()]);
        return(result.length);
    }

    public Integer[] getLines(){
        return lines;
    }

    public String getName(){
        return this.name;
    }

    public int getLevel(){
        return level;
    }

    public ArrayList<Entrance> getEntrances(){
        return entrances;
    }


}
