package com.weso.rdf4j;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.common.exception.ValidationException;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class App 
{
    public static void main(String[] args) throws IOException {
        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository sailRepository = new SailRepository(shaclSail);
        sailRepository.init();

        try (SailRepositoryConnection connection = sailRepository.getConnection()) {
            connection.begin();

            // input stream
            InputStream graphFile = new FileInputStream("/home/angel/shacl-validation-benchmark/data/10-lubm.ttl");
            InputStream shapesFile = new FileInputStream("/home/angel/shacl-validation-benchmark/data/lubm.ttl");;

            connection.add(shapesFile, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
            connection.commit();

            connection.begin();

            connection.add(graphFile, "", RDFFormat.TURTLE);
            
            try {
                connection.commit();
            } catch (RepositoryException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof ValidationException) {
                    Model validationReportModel = ((ValidationException) cause).validationReportAsModel();

                    WriterConfig writerConfig = new WriterConfig()
                        .set(BasicWriterSettings.INLINE_BLANK_NODES, true)
                        .set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true)
                        .set(BasicWriterSettings.PRETTY_PRINT, true);

                    Rio.write(validationReportModel, System.out, RDFFormat.TURTLE, writerConfig);
                }
        
                throw exception;
            }
        }
    }
}
