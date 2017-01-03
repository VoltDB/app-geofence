/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
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
import org.voltdb.*;
import org.voltdb.client.*;

public class GeoFenceBenchmark extends BaseBenchmark {

    private DeviceSimulator sim = new DeviceSimulator();
    private int deviceCount = 0;

    // constructor
    public GeoFenceBenchmark(BenchmarkConfig config) {
        super(config);

        this.deviceCount = config.devices;
    }

    // this gets run once before the benchmark begins
    public void initialize() throws Exception {

        // ------ load zipcodes + positions into a map
        sim.loadZipcodes(config.zipcode_filename);

        // initialize devices
        sim.initializeDevices(deviceCount, client);

    }

    public void iterate() throws Exception {

        // pick a random device, with new position
        sim.randomMovement(client);

    }

    public static void main(String[] args) throws Exception {
        BenchmarkConfig config = BenchmarkConfig.getConfig("GeoFenceBenchmark",args);

        BaseBenchmark c = new GeoFenceBenchmark(config);
        c.runBenchmark();
    }


}
