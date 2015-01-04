/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package client;

import java.io.*;
import java.util.*;
import org.voltdb.client.*;
import org.voltdb.types.TimestampType;

public class DeviceSimulator {
    private Random rand = new Random();

    static class Position {
        public double latitude;
        public double longitude;
    }
    private Map<String,Position> zipcodePositions = new HashMap<String,Position>();
    private String[] zipcodes;
    private Position[] devicePositions;

    private int[] radii = {25,50,100,250};

    //constructor
    public DeviceSimulator() {
    }

    public void loadZipcodes(String filename) throws Exception {

        int i=1;
	try {

            // read from file
            FileInputStream fis = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            System.out.println("reading from file: " + filename);

            // skip header line
            String line = br.readLine();

            // load file
	    while ((line = br.readLine()) != null) {
                i++;
                String[] record = line.split("\\t"); // split by tab

                // add to zipcodePositions set
                String zipcode = record[0];
                Position p = new Position();
                p.latitude = Double.parseDouble(record[7]);
                p.longitude = Double.parseDouble(record[8]);
                zipcodePositions.put(zipcode,p);

		if (i % 10000 == 0)
		    System.out.println("  read " + i + " lines");
	    }
	    br.close();
            System.out.println("  read " + Integer.toString(i) + " lines");

            zipcodes = zipcodePositions.keySet().toArray(new String[0]);
            System.out.println("zipcodes array has length " + zipcodes.length);

	} catch (Exception e) {
	    System.err.println("Exception reading line " + i + " of " + filename);
	    throw new Exception(e);
	}

    }

    public void initializeDevices(int devices, Client client) throws Exception {

        System.out.println("Generating and storing " + devices + " devices...");

        devicePositions = new Position[devices];

        for (int i=0; i<devices; i++) {
            // generate new device
            // pick a random zipcode and get it's position
            String zipCode = zipcodes[rand.nextInt(zipcodes.length)];
            Position zipPos = zipcodePositions.get(zipCode);
            Position p = new Position();
            p.latitude = zipPos.latitude;
            p.longitude = zipPos.longitude;
            devicePositions[i] = p;   // save the position of the device for later

            // insert device
            client.callProcedure(new BenchmarkCallback("DEVICES.insert"),
                                 "DEVICES.insert",
                                 i,
                                 zipCode,
                                 p.latitude,
                                 p.longitude,
                                 rand.nextInt(2),
                                 radii[rand.nextInt(radii.length)],
                                 1
                                 );

            if (i % 100000 == 0)
                System.out.println("  " + i + " devices");
        }
        System.out.println("  " + devices + " devices\n");

    }

    public void randomMovement(Client client) throws Exception {

        // get a random device
        int d = rand.nextInt(devicePositions.length);

        // get it's last position
        Position p = devicePositions[d];

        // move it  (should be about 0-30 miles)
        p.latitude += rand.nextGaussian()/10;
        p.longitude += rand.nextGaussian()/10;

        // update the database (stored procedure will check for entry/exit events)
        client.callProcedure(new BenchmarkCallback("PositionUpdate"),
                             "PositionUpdate",
                             d,
                             new TimestampType(),
                             p.latitude,
                             p.longitude);
    }


    public static void main(String[] args) throws Exception {
        DeviceSimulator ds = new DeviceSimulator();
        ds.loadZipcodes(args[0]);
    }
}
