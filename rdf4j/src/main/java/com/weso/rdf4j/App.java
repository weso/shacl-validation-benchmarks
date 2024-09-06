package com.weso.rdf4j;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclValidator;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
    private static final String SHACL = "/home/angel/shacl-validation-benchmark/data/non-conformant.ttl";
	private static final int ITERS = 10;

    public static void main(String[] args) throws IOException {
        List<Double> times = new ArrayList<>();
        List<String[]> ans = new ArrayList<>();

        Model shapesModel = RdfUtils.read(SHACL, null, RDFFormat.TURTLE);

        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository shapes = new SailRepository(shaclSail);
        shapes.init();

        try (SailRepositoryConnection connection = shapes.getConnection()) {
            // 2. Save SHACL
            connection.begin();
            connection.add(shapesModel, RDF4J.SHACL_SHAPE_GRAPH);
            connection.commit();
        }

        for (int university: UNIVERSITIES) {
            times = new ArrayList<>();

            Model dataGraph = RdfUtils.read(
                String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university),
                null,
                RDFFormat.TURTLE
            );

            ShaclSail dataSail = new ShaclSail(new MemoryStore());
            SailRepository data = new SailRepository(dataSail);
            shapes.init();
    
            try (SailRepositoryConnection connection = data.getConnection()) {
                connection.begin();
                connection.add(new ArrayList<>(dataGraph));
                connection.commit();
            }

            for (int i = 0; i < ITERS; i++) {
                long start = System.nanoTime();
				ShaclValidator.validate(shapes.getSail(), data.getSail());
				long finish = System.nanoTime();
				times.add((double) (finish - start));
            }

            String[] record = { 
                String.format("%f", times.stream().mapToDouble(d -> d).average().orElse(0.0)),
                String.format("%f", calculateStandardDeviation(times)),
                String.format("%d-LUBM", university),
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
