package com.weso.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;

public class App {
    private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
	private static final int ITERS = 1;
    
    public static void main( String[] args ) {		
        List<Long> times = new ArrayList<>();
        
        String SHAPES = "/home/angel/shacl-validation-benchmark/data/lubm.ttl";
        Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
        Shapes shapes = Shapes.parse(shapesGraph);

		for (int university: UNIVERSITIES) {
			// input stream
			String DATA = String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university);
			Graph dataGraph = RDFDataMgr.loadGraph(DATA);

			ShaclValidator.get().validate(shapes, dataGraph); // avoid cold starts
			for (int i = 0; i < ITERS; i++) {
				long start = System.currentTimeMillis();
				ShaclValidator.get().validate(shapes, dataGraph);
				long finish = System.currentTimeMillis();
				times.add(finish - start);
			}

			times.forEach(System.out::println);
		}
    }
}
