package dev.datainmotion.gtfs;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.transit.realtime.GtfsRealtime;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.google.protobuf.util.JsonFormat;

/**
 *
 */
public class GTFSRealTimeService {


    public static final int STATUS_CODE_GOOD = 200;
    public static final String STATUS_GOOD = "OK";

    /**
     * @param gtfsStringURL
     * @return List<Result></Result>
     */
    public GTFS listData(String gtfsStringURL) {

        GTFS gtfsData = new GTFS();
        List<Result> results = new ArrayList<Result>();
        if ( gtfsStringURL == null) {
            gtfsData.setStatus("Missing URL");
            gtfsData.setStatusCode(500);
            return gtfsData;
        }

        URL url = null;
        try {
            if (gtfsStringURL != null) {
                url = new URL(gtfsStringURL);
            }
        } catch (MalformedURLException e) {
            gtfsData.setStatus("Bad URL: " + e.getLocalizedMessage());
            gtfsData.setStatusCode(501);
            return gtfsData;
        }
        if (url == null) {
            gtfsData.setStatus("Empty URL");
            gtfsData.setStatusCode(502);
            return gtfsData;
        }
        GtfsRealtime.FeedMessage feed = null;
        try {
            feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
            JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.getEmptyTypeRegistry();
            JsonFormat.Printer printer = JsonFormat.printer().usingTypeRegistry(registry);
            JsonFormat.Parser parser = JsonFormat.parser().usingTypeRegistry(registry);
            Message.Builder builder = feed.newBuilderForType();
            parser.merge(printer.print(feed), builder);
            gtfsData.setGtfsString(printer.print(feed).toString());
        } catch (IOException e) {
            e.printStackTrace();
            gtfsData.setStatus("Not a valid GTFS Real-Time Feed");
            gtfsData.setStatusCode(503);
            return gtfsData;
        }
        if (feed != null && feed.getEntityList() != null && feed.getEntityList().size() > 0) {
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity != null) {
                    boolean hasData = false;
                    Result gtfsEntity = new Result();

                    if (entity.hasTripUpdate()) {
                        gtfsEntity.setTripUpdate(entity.getTripUpdate());
                        hasData = true;
                    }
                    if (entity.hasAlert()) {
                        gtfsEntity.setAlert(entity.getAlert());
                        hasData = true;
                    }
                    if (entity.hasVehicle()) {
                        gtfsEntity.setVehicle(entity.getVehicle());
                        hasData = true;
                    }

                    if ( hasData ) {
                        gtfsEntity.setId(entity.getId());
                        gtfsEntity.setDeleted(entity.getIsDeleted());
                        results.add(gtfsEntity);
                    }
                }
            }
            gtfsData.setStatus(GTFSRealTimeService.STATUS_GOOD);
            gtfsData.setStatusCode(GTFSRealTimeService.STATUS_CODE_GOOD);
            gtfsData.setAttributes(results);
        }

        return gtfsData;
    }
}
