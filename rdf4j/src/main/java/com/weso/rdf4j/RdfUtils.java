package com.weso.rdf4j;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RdfUtils {

    public static Model read(String file, String baseUri, RDFFormat format) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return Rio.parse(inputStream, baseUri, format);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read RDF (IO exception)");
        } catch (RDFParseException e) {
            throw new RuntimeException("Unable to read RDF (parse exception)");
        } catch (RDFHandlerException e) {
            throw new RuntimeException("Unable to read RDF (handler exception)");
        }
    }

}