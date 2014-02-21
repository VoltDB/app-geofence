VoltDB Example: Geo-Fencing 
============================

Use Case
--------
One million devices are tracked as they provide continuous position (lat/long) updates and pass in and out of personalized geo-fence boundaries.  Alert events are generated whenever a device exits or enters the fence, which is defined individually for each device based on a "home" location and a radius in miles.  

The client application creates the set of devices with their geo-fence defintions and then begins generating random device updates.  Each device starts at it's home location and then each subsequent update is a based on a "random walk" incremental change from the last position.

The database ingests these updates and keeps a history of the positions for each device.  As it processes each update, it also checks to see if this device has a geo-fence enabled and if so, whether the device was previously in-bounds or out-of-bounds.  It calculates the distance from "home" to the new location to see if it is in or out of bounds.  If this is a change in status, it generates an "exit" or "entry" event record, which would result in a notification to the device owner.  All of this logic is found in the "PositionUpdate" stored procedure.

Code organization
-----------------
### db
The database schema and stored procedures
### client
The java benchmark client application.

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



