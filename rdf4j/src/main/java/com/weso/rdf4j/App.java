package com.weso.rdf4j;

import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;

import com.opencsv.CSVWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
	private static final int ITERS = 10;

    public static void main(String[] args) throws IOException {
        List<Double> times = new ArrayList<>();
        List<String[]> ans = new ArrayList<>();

        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository sailRepository = new SailRepository(shaclSail);
        sailRepository.init();

        try (SailRepositoryConnection connection = sailRepository.getConnection()) {
            connection.begin();
            InputStream shapesFile = new FileInputStream("/home/angel/shacl-validation-benchmark/data/lubm.ttl");
            connection.add(shapesFile, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
            connection.commit();

            for (int university: UNIVERSITIES) {
                times = new ArrayList<>();
                
                connection.begin();
                String file = String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university);
                InputStream graphFile = new FileInputStream(file);
                connection.add(graphFile, "", RDFFormat.TURTLE);
                connection.commit(); // avoid cold starts

                for (int i = 0; i < ITERS; i++) {
                    long start = System.nanoTime();
                    connection.begin();
                    connection.commit();
                    long finish = System.nanoTime();
                    times.add((double) (finish - start));
                }

                connection.begin();
                connection.clear();
                connection.commit();

                String[] record = { 
                    String.format("%f", times.stream().mapToDouble(d -> d).average().orElse(0.0)),
                    String.format("%f", calculateStandardDeviation(times)),
                    String.format("%d-lubm", university),
                    "rdf4j"
                };
                ans.add(record);
            }
        }

        try (CSVWriter writer = new CSVWriter(
            new FileWriter("/home/angel/shacl-validation-benchmark/results/rdf4j.csv"),
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
