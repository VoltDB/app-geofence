package procedures;

import java.lang.Math;
import java.lang.Double;
import org.voltdb.*;
import org.voltdb.types.TimestampType;
import org.voltdb.client.ClientResponse;

public class PositionUpdate extends VoltProcedure {

    public static double calcDistance(double latA, double longA, double latB, double longB)
    {
        double theDistance = (Math.sin(Math.toRadians(latA)) *
                              Math.sin(Math.toRadians(latB)) +
                              Math.cos(Math.toRadians(latA)) *
                              Math.cos(Math.toRadians(latB)) *
                              Math.cos(Math.toRadians(longA - longB)));

        return (Math.toDegrees(Math.acos(theDistance))) * 69.09; // in miles
    }

    public final SQLStmt getDevice = new SQLStmt(
        "SELECT * FROM devices WHERE id = ?;");

    public final SQLStmt newPosition = new SQLStmt(
        "INSERT INTO device_location VALUES (?,?,?,?);");

    public final SQLStmt newEvent = new SQLStmt(
        "INSERT INTO device_event VALUES (?,?,?,?);");


    public final SQLStmt updateInsideFence = new SQLStmt(
        "UPDATE devices SET inside_geofence = ? WHERE id = ?;");

    // delete any locations more than 60 minutes old
    public final SQLStmt deleteOldLocations = new SQLStmt(
	"DELETE FROM device_location" +
	" WHERE id = ?"+
	" AND ts < TO_TIMESTAMP(SECOND,SINCE_EPOCH(SECOND,NOW)-(60*60))"); 

    public long run(int id,
                    TimestampType ts,
                    double newLat,
                    double newLong
                    ) throws VoltAbortException {

        // get device record
        voltQueueSQL(getDevice,id);
        // insert new location
        voltQueueSQL(newPosition,id,ts,newLat,newLong);
	// delete any old locations (housekeeping)
        voltQueueSQL(deleteOldLocations,id);
	
        VoltTable[] a = voltExecuteSQL();
        VoltTable t = a[0];
        t.advanceRow();
        int hasFence = (int)t.getLong(4);

        // if has_geofence, calculate distance from home
        if (hasFence == 1) {
            double homeLat = t.getDouble(2);
            double homeLong = t.getDouble(3);
            double radius = t.getDouble(5);
            int inside = (int)t.getLong(6);
            double dist = calcDistance(homeLat,homeLong,
                                       newLat,newLong);

            // if previously inside fence, and now beyond radius
            if (inside == 1 && dist > radius) {
                // exit event
                voltQueueSQL(newEvent,id,ts,1,0);
                // update inside_geofence = 0
                voltQueueSQL(updateInsideFence,0,id);
                voltExecuteSQL(true);
            }

            // if previously outside fence, and now within radius
            if (inside == 0 && dist < radius) {
                // entry event
                voltQueueSQL(newEvent,id,ts,0,1);
                // update inside_geofence = 1
                voltQueueSQL(updateInsideFence,1,id);
                voltExecuteSQL(true);
            }
                                     
        }

        return ClientResponse.SUCCESS;
    }
}
