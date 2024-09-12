package com.weso.rdf4j;

import org.eclipse.rdf4j.common.exception.ValidationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;

public class App {
    private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
    private static final String SHACL = "/home/angel/shacl-validation-benchmark/data/conformant.ttl";
	private static final int ITERS = 10;

    public static void main(String[] args) throws IOException {
        List<Double> times = new ArrayList<>();
        List<String[]> ans = new ArrayList<>();
        Model report = null;

        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository shapes = new SailRepository(shaclSail);

        for (int university: UNIVERSITIES) {
            for (int i = 0; i < ITERS; i++) {
                try (SailRepositoryConnection connection = shapes.getConnection()) {
                    connection.begin();
                    
                    // load shapes
                    try (InputStream inputStream = new FileInputStream(SHACL)) {
                        connection.add(inputStream, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
                    }
        
                    // load data
                    String DATA = String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university);
                    try (InputStream inputStream = new BufferedInputStream(new FileInputStream(DATA))) {
                        connection.add(inputStream, "", RDFFormat.TURTLE);
                    }

                    long start = System.nanoTime();

                    try {
                        connection.commit();
                    } catch (RepositoryException e){
                        if(e.getCause() instanceof ValidationException){
                            report = ((ValidationException) e.getCause()).validationReportAsModel();
                        }
                    } finally {
                        long finish = System.nanoTime();
				        times.add((double) (finish - start));
                    }

                    connection.clear(RDF4J.SHACL_SHAPE_GRAPH);
                    connection.commit();
                }
            }

            String[] record = { 
                String.format("%f", times.stream().mapToDouble(d -> d).average().orElse(0.0)),
                String.format("%f", calculateStandardDeviation(times)),
                String.format("%d-LUBM", university),
                String.format("%b", report == null ? 0 : report.size() == 0),
                String.format("%d", report == null ? 0 : report.size()),
                "RDF4J"
            };
            ans.add(record);
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
