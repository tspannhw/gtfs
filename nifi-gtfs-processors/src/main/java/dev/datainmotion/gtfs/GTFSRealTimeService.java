package dev.datainmotion.gtfs;

import com.google.protobuf.Descriptors;
import com.google.protobuf.UnknownFieldSet;
import com.google.transit.realtime.GtfsRealtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GTFSRealTimeService {


    public static final int STATUS_CODE_GOOD = 200;
    public static final String STATUS_GOOD = "OK";

    /**
     * @param gtfsStringURL
     * @return List<Result></Result>
     */
    public List<Result> listData(String gtfsStringURL) {

        List<Result> results = new ArrayList<Result>();
        if ( gtfsStringURL == null) {
            Result result = new Result();
            result.setStatus("Missing URL");
            result.setStatusCode(500);
            results.add(result);
            return results;
        }

        URL url = null;
        try {
            if (gtfsStringURL != null) {
                url = new URL(gtfsStringURL);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();

            Result result = new Result();
            result.setStatus("Bad URL");
            result.setStatusCode(501);
            results.add(result);
            return results;
        }
        if (url == null) {
            Result result = new Result();
            result.setStatus("Empty URL");
            result.setStatusCode(502);
            results.add(result);
            return results;
        }
        GtfsRealtime.FeedMessage feed = null;
        try {
            feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
            Result result = new Result();
            result.setStatus("Not a valid GTFS Real-Time Feed");
            result.setStatusCode(503);
            results.add(result);
            return results;
        }
        if (feed != null && feed.getEntityList() != null && feed.getEntityList().size() > 0) {
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity != null) {
                    Result gtfsEntity = new Result();

                    if (entity.hasTripUpdate()) {
                        // System.out.println(entity.getTripUpdate());
                        gtfsEntity.setTripUpdate(entity.getTripUpdate());
                    }
                    if (entity.hasAlert()) {
                        //  System.out.println(entity.getAlert());
                        gtfsEntity.setAlert(entity.getAlert());
                    }
                    if (entity.hasVehicle()) {
                        //    System.out.println(entity.getVehicle());
                        gtfsEntity.setVehicle(entity.getVehicle());
                    }

                    gtfsEntity.setId(entity.getId());
                    gtfsEntity.setDeleted(entity.getIsDeleted());
                    gtfsEntity.setStatus(GTFSRealTimeService.STATUS_GOOD);
                    gtfsEntity.setStatusCode(GTFSRealTimeService.STATUS_CODE_GOOD);
                    results.add(gtfsEntity);
//                UnknownFieldSet unknownFields = entity.getUnknownFields();
//                Map<Descriptors.FieldDescriptor, Object> allFields = entity.getAllFields();
//                System.out.println(allFields);
                }
            }
        }

        return results;
    }
}
