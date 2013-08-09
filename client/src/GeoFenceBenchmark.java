package client;

import java.io.*;
import java.util.*;
import org.voltdb.*;
import org.voltdb.client.*;

public class GeoFenceBenchmark extends BaseBenchmark {

    //private Random rand = new Random();
    //private String zipcodeFilename = null;
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

    public void printResults() throws Exception {
        
        System.out.print("\n" + HORIZONTAL_RULE);
        System.out.println(" Transaction Results");
        System.out.println(HORIZONTAL_RULE);

        BenchmarkCallback.printProcedureResults("DEVICES.insert");
        BenchmarkCallback.printProcedureResults("AddLocation");

        super.printResults();
    }

    public static void main(String[] args) throws Exception {
        BenchmarkConfig config = BenchmarkConfig.getConfig("GeoFenceBenchmark",args);
        
        BaseBenchmark c = new GeoFenceBenchmark(config);
        c.runBenchmark();
    }


}
