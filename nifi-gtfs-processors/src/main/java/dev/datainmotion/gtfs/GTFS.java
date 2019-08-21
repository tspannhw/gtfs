package dev.datainmotion.gtfs;

import java.util.List;

/**
 *
 */
public class GTFS {

    private String gtfsString;
    private List<Result> attributes;
    private String status;
    private int statusCode;


    public GTFS() {
        super();
    }

    public GTFS(String gtfsString, List<Result> attributes) {
        super();
        this.gtfsString = gtfsString;
        this.attributes = attributes;
    }

    public GTFS(String gtfsString, List<Result> attributes, String status, int statusCode) {
        this.gtfsString = gtfsString;
        this.attributes = attributes;
        this.status = status;
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getGtfsString() {
        return gtfsString;
    }

    public void setGtfsString(String gtfsString) {
        this.gtfsString = gtfsString;
    }

    public List<Result> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Result> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GTFS{");
        sb.append("gtfsString='").append(gtfsString).append('\'');
        sb.append(", attributes=").append(attributes);
        sb.append(", status='").append(status).append('\'');
        sb.append(", statusCode=").append(statusCode);
        sb.append('}');
        return sb.toString();
    }
}
