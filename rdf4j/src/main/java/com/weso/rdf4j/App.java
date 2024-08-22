package com.weso.rdf4j;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class App {
    // private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
    private static final int[] UNIVERSITIES = { 10, 20 };
    private static final String SHACL = "/home/angel/shacl-validation-benchmark/data/conformant.ttl";
	private static final int ITERS = 2;

    public static void main(String[] args) throws IOException {
        List<Double> times = new ArrayList<>();
        List<String[]> ans = new ArrayList<>();
        long start = 0;

        for (int university: UNIVERSITIES) {
            times = new ArrayList<>();

            for (int i = 0; i < ITERS; i++) {
                Model shacl = RdfUtils.read(SHACL, null, RDFFormat.TURTLE);
                Model data = RdfUtils.read(
                    String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university),
                    null,
                    RDFFormat.TURTLE
                );

                ShaclSail shaclSail = new ShaclSail(new MemoryStore());
                shaclSail.setRdfsSubClassReasoning(true);
                //shaclSail.setUndefinedTargetValidatesAllSubjects(true);
                SailRepository sailRepository = new SailRepository(shaclSail);
                sailRepository.init();
                try (SailRepositoryConnection connection = sailRepository.getConnection()) {
                    // 2. Save SHACL
                    connection.begin();
                    connection.add(shacl, RDF4J.SHACL_SHAPE_GRAPH);
                    connection.commit();

                    // 3. Validate data                 
                    connection.begin();
                    connection.add(new ArrayList<>(data));
                    start = System.nanoTime();
                    connection.commit();

                } catch (RepositoryException exception) {
                    Throwable cause = exception.getCause();
                    if (cause instanceof ShaclSailValidationException) {
                        Model validationReportModel = ((ShaclSailValidationException) cause).validationReportAsModel();
                        times.add((double) (System.nanoTime() - start));
                    }
                }
            }

            String[] record = { 
                String.format("%f", times.stream().mapToDouble(d -> d).average().orElse(0.0)),
                String.format("%f", calculateStandardDeviation(times)),
                String.format("%d-lubm", university),
                "rdf4j"
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
