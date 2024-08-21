package com.weso.topquadrant;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;

import com.opencsv.CSVWriter;

public class App {
	private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
	private static final String SHACL = "/home/angel/shacl-validation-benchmark/data/non-conformant.ttl";
	private static final int ITERS = 10;

    public static void main( String[] args ) throws IOException {
		List<Double> times = new ArrayList<>();
        List<String[]> ans = new ArrayList<>();

		// Load the shapes data model
		Model shapes = JenaUtil.createMemoryModel();
		InputStream shapesFile = new FileInputStream(SHACL);
		shapes.read( shapesFile, "", FileUtils.langTurtle );

		for (int university: UNIVERSITIES) {
			times = new ArrayList<>();
			// input stream
			String file = String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university);
			InputStream graphFile = new FileInputStream(file);

			// Load the graph data model
			Model graph = JenaUtil.createMemoryModel();
			graph.read( graphFile, "", FileUtils.langTurtle );

			ValidationUtil.validateModel(graph, shapes, true); // avoid cold starts
			for (int i = 0; i < ITERS; i++) {
				long start = System.nanoTime();
				ValidationUtil.validateModel(graph, shapes, true);
				long finish = System.nanoTime();
				times.add((double) (finish - start));
			}

			String[] record = { 
				String.format("%f", times.stream().mapToDouble(d -> d).average().orElse(0.0)),
				String.format("%f", calculateStandardDeviation(times)),
				String.format("%d-lubm", university),
				"topquadrant"
			};
			ans.add(record);
		}
		
        try (CSVWriter writer = new CSVWriter(
            new FileWriter("/home/angel/shacl-validation-benchmark/results/topquadrant.csv"),
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
