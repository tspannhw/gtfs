package dev.datainmotion.gtfs;

import com.google.transit.realtime.GtfsRealtime;

import java.util.Map;

/**
 *
 */
public class Result {

    private String key;
    private String value;
    private String id;
    private boolean isDeleted;
    private GtfsRealtime.TripUpdate tripUpdate;
    private GtfsRealtime.Alert alert;


    public GtfsRealtime.Alert getAlert() {
        return alert;
    }

    public void setAlert(GtfsRealtime.Alert alert) {
        this.alert = alert;
    }

    private GtfsRealtime.VehiclePosition vehicle;

    public Result() {
        super();
    }

    public Result(String id, boolean isDeleted, GtfsRealtime.TripUpdate tripUpdate, GtfsRealtime.VehiclePosition vehicle) {
        this.id = id;
        this.isDeleted = isDeleted;
        this.tripUpdate = tripUpdate;
        this.vehicle = vehicle;
    }

    /**
     *
     * @param key
     * @param value
     * @param id
     * @param isDeleted
     * @param tripUpdate
     * @param vehicle
     */
    public Result(String key, String value, String id, boolean isDeleted, GtfsRealtime.TripUpdate tripUpdate, GtfsRealtime.VehiclePosition vehicle) {
       super();
       this.key = key;
        this.value = value;
        this.id = id;
        this.isDeleted = isDeleted;
        this.tripUpdate = tripUpdate;
        this.vehicle = vehicle;
    }

    public Result(String key, String value, String id, boolean isDeleted, GtfsRealtime.TripUpdate tripUpdate, GtfsRealtime.Alert alert, GtfsRealtime.VehiclePosition vehicle) {
        super();
        this.key = key;
        this.value = value;
        this.id = id;
        this.isDeleted = isDeleted;
        this.tripUpdate = tripUpdate;
        this.alert = alert;
        this.vehicle = vehicle;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public GtfsRealtime.TripUpdate getTripUpdate() {
        return tripUpdate;
    }

    public void setTripUpdate(GtfsRealtime.TripUpdate tripUpdate) {
        this.tripUpdate = tripUpdate;
    }

    public GtfsRealtime.VehiclePosition getVehicle() {
        return vehicle;
    }

    public void setVehicle(GtfsRealtime.VehiclePosition vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Result{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", tripUpdate=").append(tripUpdate);
        sb.append(", alert=").append(alert);
        sb.append(", vehicle=").append(vehicle);
        sb.append('}');
        return sb.toString();
    }



}
