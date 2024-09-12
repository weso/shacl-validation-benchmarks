package com.weso.jena;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;

import com.opencsv.CSVWriter;

public class App {
    // private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
    private static final int[] UNIVERSITIES = { 10 };
    private static final String SHACL = "/home/angel/shacl-validation-benchmark/data/non-conformant.ttl";
	private static final int ITERS = 1;
    
    public static void main( String[] args ) throws IOException {		
		List<Double> times = new ArrayList<>();
        List<String[]> ans = new ArrayList<>();
        ValidationReport report = null;
        
        Graph shapesGraph = RDFDataMgr.loadGraph(SHACL);
        Shapes shapes = Shapes.parse(shapesGraph);

		for (int university: UNIVERSITIES) {
            times = new ArrayList<>();
			// input stream
			String DATA = String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university);
			Graph dataGraph = RDFDataMgr.loadGraph(DATA);

			ShaclValidator.get().validate(shapes, dataGraph); // avoid cold starts
			for (int i = 0; i < ITERS; i++) {
				long start = System.nanoTime();
				report = ShaclValidator.get().validate(shapes, dataGraph);
				long finish = System.nanoTime();
				times.add((double) (finish - start));
			}

			String[] record = { 
				String.format("%f", times.stream().mapToDouble(d -> d).average().orElse(0.0)),
				String.format("%f", calculateStandardDeviation(times)),
				String.format("%d-LUBM", university),
                String.format("%b", report.conforms()),
                String.format("%d", report.getEntries().size()),
				"Apache Jena"
			};
			ans.add(record);
		}

        try (CSVWriter writer = new CSVWriter(
            new FileWriter("/home/angel/shacl-validation-benchmark/results/jena.csv"),
            CSVWriter.DEFAULT_SEPARATOR,
            CSVWriter.NO_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, 
            CSVWriter.DEFAULT_LINE_END
        )) {
            writer.writeAll(ans);
        }
    }

	public static double calculateStandardDeviation(List<Double> times) {
        // get the sum of array
        double sum = 0.0;
        for (double i : times) {
            sum += i;
        }
    
        // get the mean of array
        int length = times.size();
        double mean = sum / length;
    
        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (double num : times) {
            standardDeviation += Math.pow(num - mean, 2);
        }
    
        return Math.sqrt(standardDeviation / length);
    }
}
