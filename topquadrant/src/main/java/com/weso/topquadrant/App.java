package com.weso.topquadrant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;

public class App {
	private static final int[] UNIVERSITIES = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
	private static final int ITERS = 1;

    public static void main( String[] args ) throws FileNotFoundException {
		List<Long> times = new ArrayList<>();

		// Load the shapes data model
		Model shapes = JenaUtil.createMemoryModel();
		InputStream shapesFile = new FileInputStream("/home/angel/shacl-validation-benchmark/data/lubm.ttl");
		shapes.read( shapesFile, "", FileUtils.langTurtle );

		for (int university: UNIVERSITIES) {
			// input stream
			String file = String.format("/home/angel/shacl-validation-benchmark/data/%d-lubm.ttl", university);
			InputStream graphFile = new FileInputStream(file);

			// Load the graph data model
			Model graph = JenaUtil.createMemoryModel();
			graph.read( graphFile, "", FileUtils.langTurtle );

			ValidationUtil.validateModel(graph, shapes, true); // avoid cold starts
			for (int i = 0; i < ITERS; i++) {
				long start = System.currentTimeMillis();
				ValidationUtil.validateModel(graph, shapes, true);
				long finish = System.currentTimeMillis();
				times.add(finish - start);
			}

			times.forEach(System.out::println);
		}
    }
}
