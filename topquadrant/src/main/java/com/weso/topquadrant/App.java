package com.weso.topquadrant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;

public class App 
{
    public static void main( String[] args ) throws FileNotFoundException {
        // input stream
        InputStream graphFile = new FileInputStream("/home/angel/shacl-validation-benchmark/data/10-lubm.ttl");
        InputStream shapesFile = new FileInputStream("/home/angel/shacl-validation-benchmark/data/lubm.ttl");

        // Load the graph data model
		Model graph = JenaUtil.createMemoryModel();
		graph.read( graphFile, "urn:dummy", FileUtils.langTurtle );

        // Load the shapes data model
		Model shapes = JenaUtil.createMemoryModel();
		shapes.read( shapesFile, "urn:dummy", FileUtils.langTurtle );
		
		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
		Resource report = ValidationUtil.validateModel(graph, shapes, true);
		
		// Print violations
		System.out.println(ModelPrinter.get().print(report.getModel()));
    }
}
