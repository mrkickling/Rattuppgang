package se.joakimloxdal.rattuppgang;

/**
 * Created by joakimloxdal on 2018-10-17.
 */

public class Entrance {
    private String name;
    private String placement;
    private String coordinates;
    private String distanceToAddress = null;

    public Entrance(String name, String placement, String coordinates){
        this.name = name;
        this.placement = placement;
        this.coordinates = coordinates;
    }
    public void setDistanceToAddress(String dist){
        this.distanceToAddress = dist;
    }

    public Entrance(String name, String placement){
        this.name = name;
        this.placement = placement;
    }

    public String getPlacement(){
        return this.placement;
    }
    public String getName(){
        return this.name;
    }
    public String getCoordinates(){
        return this.coordinates;
    }
    public String getDistanceToAddress(){
        return this.distanceToAddress;
    }


}
