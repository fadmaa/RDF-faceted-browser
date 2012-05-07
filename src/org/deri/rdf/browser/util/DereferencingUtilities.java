package org.deri.rdf.browser.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

import org.deri.any23.Any23;
import org.deri.any23.extractor.ExtractionException;
import org.deri.any23.http.HTTPClient;
import org.deri.any23.source.DocumentSource;
import org.deri.any23.source.HTTPDocumentSource;
import org.deri.any23.writer.NTriplesWriter;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DereferencingUtilities {

	public static Model dereference(String uri) throws IOException, URISyntaxException, TripleHandlerException, ExtractionException{
		Any23 runner = new Any23();
		runner.setHTTPUserAgent("test-user-agent");
		HTTPClient httpClient = runner.getHTTPClient();
		DocumentSource source = new HTTPDocumentSource(httpClient,uri);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TripleHandler handler = new NTriplesWriter(out);
		try {
		  runner.extract(source, handler);
	    } finally {
	      handler.close();
	    }
	    Model model = ModelFactory.createDefaultModel();
	    model.read(new StringReader(out.toString("UTF-8")),null,"N3");
	    
	    return model;
	}
}
