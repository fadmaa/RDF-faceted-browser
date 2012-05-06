package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.model.RdfEngine;
import org.deri.rdf.browser.sparql.QueryEngine;

import com.hp.hpl.jena.rdf.model.Model;

import de.dfki.km.json.jsonld.impl.JenaJSONLDSerializer;

public class ExportJsonLdCommand  extends RdfCommand{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		try{
			RdfEngine engine = getRdfEngine(req);
			QueryEngine queryEngine = new QueryEngine();
			Model model = engine.getResourcesRDF(queryEngine);
			resp.setContentType("text/json");
			resp.setHeader("Content-Disposition", "attachment; filename=data.json");
			PrintWriter out = resp.getWriter();
			
			JenaJSONLDSerializer serializer = new JenaJSONLDSerializer();
			// import the Jena Model
			serializer.importModel(model);
			// grab the resulting JSON-LD map
			out.print(serializer.asString());
			out.close();
		}catch (Exception e) {
			respondException(resp, e);
		}
	}

}
