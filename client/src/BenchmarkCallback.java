package client;

import java.util.*;
import com.google_voltpatches.common.collect.ConcurrentHashMultiset;
import com.google_voltpatches.common.collect.Multiset;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

public class BenchmarkCallback implements ProcedureCallback {

    private static Multiset<String> calls = ConcurrentHashMultiset.create();
    private static Multiset<String> commits = ConcurrentHashMultiset.create();
    private static Multiset<String> rollbacks = ConcurrentHashMultiset.create();

    String procedureName;
    long maxErrors;

    public static void printProcedureResults(String procedureName) {
        System.out.println("  " + procedureName);
        System.out.println("        calls: " + calls.count(procedureName));
        System.out.println("      commits: " + commits.count(procedureName));
        System.out.println("    rollbacks: " + rollbacks.count(procedureName));
    }

    public static void printAllResults() {
	for (String e : calls.elementSet()) {
	    printProcedureResults(e);
	}
    }

    public BenchmarkCallback(String procedure, long maxErrors) { 
        super();
        this.procedureName = procedure;
        this.maxErrors = maxErrors;
    }

    public BenchmarkCallback(String procedure) {
        this(procedure, 5l);
    }

    @Override
    public void clientCallback(ClientResponse cr) {

	calls.add(procedureName,1);

        if (cr.getStatus() == ClientResponse.SUCCESS) {
	    commits.add(procedureName,1);
        } else {
            long totalErrors = rollbacks.add(procedureName,1);

            System.err.println("DATABASE ERROR: " + cr.getStatusString());

            if (totalErrors > maxErrors) {
                System.err.println("exceeded " + maxErrors + " maximum database errors - exiting client");
                System.exit(-1);
            }

        }
    }
}












