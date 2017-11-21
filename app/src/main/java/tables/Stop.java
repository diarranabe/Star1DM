package tables;

/**
 * Created by diarranabe on 20/11/2017.
 */

public class Stop {

    private String name;
    private String description;
    private float latitude;
    private float longitude;
    private String wheelChairBoalding;

    public Stop(String name, String description, float latitude, float longitude, String wheelChairBoalding) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.wheelChairBoalding = wheelChairBoalding;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getWheelChairBoalding() {
        return wheelChairBoalding;
    }

    public void setWheelChairBoalding(String wheelChairBoalding) {
        this.wheelChairBoalding = wheelChairBoalding;
    }

    @Override
    public String toString() {
        return "Stop{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", wheelChairBoalding='" + wheelChairBoalding + '\'' +
                '}';
    }

}
