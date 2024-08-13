package com.weso.jena;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

public class App 
{
    public static void main( String[] args ) {
        // input stream
        String SHAPES = "/home/angel/shacl-validation-benchmark/data/lubm.ttl";
        String DATA = "/home/angel/shacl-validation-benchmark/data/10-lubm.ttl";
    
        Graph shapesGraph = RDFDataMgr.loadGraph(SHAPES);
        Graph dataGraph = RDFDataMgr.loadGraph(DATA);
    
        Shapes shapes = Shapes.parse(shapesGraph);
    
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
        ShLib.printReport(report);
        System.out.println();
        RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
    }
}
