VoltDB Example: Geo-Fencing 
============================

Use Case
--------
I this example application, thousands of devices are tracked as they provide position (lat/long) updates.  Devices can have geo-fences defined individually based on a "home" location and a radius in miles.

The client in this example, initializes the device data in the database and then generates random updates to simulate updates that would be received from the devices.  The simulated updates are rudimentary, based on a random small change in lat/long from the device's previous position, so the devices will tend to move in a 2-dimensional "random walk", starting from their home locations.

The database ingests these updates and keeps a history of the positions for each device.  As it processes each update, it also checks to see if this device has a geo-fence enabled and if so, whether the device was previously in-bounds or out-of-bounds.  It calculates the distance from "home" to the new location to see if it is in or out of bounds.  If this is a change in status, it generates an "exit" or "entry" event record, which would result in a notification to the device owner.  All of this logic is found in the "PositionUpdate" stored procedure.

The "TrackEvent" stored procedure processes these events.  It looks up the corresponding advertiser and campaign based on the creative ID which represents which ad was shown.  It also retrieves the corresponding web site and page based on the inventory ID from the event.  The timestamp and event type fields are converted to aid in aggregation, and all of this data is then inserted into the impression_data table.

Several views maintain real-time aggregations on this table to provide a minutely summary for each advertiser, plus drill-down reports grouped by campaign and creative to show detail-level metrics, costs and rates with real-time accuracy.

Code organization
-----------------
### db
The database project, which contains the schema, stored procedures and other configurations that are compiled into a catalog and run in a VoltDB database.  
### client
The java client that loads a set of cards and then generates random card transactions a high velocity to simulate card activity.

Instructions
------------

1. Start the database in the background

     ./start_db.sh
     
2. Run the client application

    ./run_client.sh

3. Open a web browser to VoltDB Studio

    http://localhost:8080/studio
    
4. Run some queries:

    -- check for events:
    SELECT * FROM device_event;
    
    -- see the settings for a device:
    SELECT * FROM devices WHERE id = ?;
    
    -- see the position history for a device:
    SELECT * FROM device_location WHERE id = ? ORDER BY ts;
    
5. When finished, stop the database and clean up any temp files

    voltadmin shutdown
    ./clean.sh



