package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.model.RdfEngine;
import org.deri.rdf.browser.sparql.QueryEngine;

import com.hp.hpl.jena.rdf.model.Model;

public class ExportRdfCommand extends RdfCommand{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		try{
			String format = req.getParameter("format");
			RdfEngine engine = getRdfEngine(req);
			QueryEngine queryEngine = new QueryEngine();
			Model model = engine.getResourcesRDF(queryEngine);
			resp.setContentType(getMimeType(format));
			resp.setHeader("Content-Disposition", "attachment; filename=data." + getFileExtension(format));
			PrintWriter out = resp.getWriter();
			model.write(out,format);
			out.close();
		}catch (Exception e) {
			respondException(resp, e);
		}
	}
	
	private String getMimeType(String format){
		if(format.equals("TURTLE")){
			return "text/turtle";
		}else if(format.equals("RDF/XML")){
			return "application/rdf+xml";
		}
		return "application/rdf+xml";
	}
	
	private String getFileExtension(String format){
		if(format.equals("TURTLE")){
			return "ttl";
		}else if(format.equals("RDF/XML")){
			return "rdf";
		}
		return "rdf";
	}

}
