package com.weso.rdf4j;

import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
	private static final int ITERS = 1;

    public static void main(String[] args) throws IOException {
        List<Long> times = new ArrayList<>();

        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository sailRepository = new SailRepository(shaclSail);
        sailRepository.init();

        try (SailRepositoryConnection connection = sailRepository.getConnection()) {
            connection.begin();
            InputStream shapesFile = new FileInputStream("/home/angel/shacl-validation-benchmark/data/lubm.ttl");
            connection.add(shapesFile, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
            connection.commit();

            for (int university: UNIVERSITIES) {
                connection.begin();
                String file = String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university);
                InputStream graphFile = new FileInputStream(file);
                connection.add(graphFile, "", RDFFormat.TURTLE);
                
                connection.commit(); // avoid cold starts
                for (int i = 0; i < ITERS; i++) {
                    long start = System.currentTimeMillis();
                    connection.commit();
                    long finish = System.currentTimeMillis();
                    times.add(finish - start);
                }

                times.forEach(System.out::println);
            }   
        }
    }
}
