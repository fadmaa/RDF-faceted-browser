package org.deri.rdf.qb.browse;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.commands.RdfCommand;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.qb.browse.sparql.QueryEngine;
import org.json.JSONException;
import org.json.JSONWriter;

public class Facets extends RdfCommand{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String endpoint = request.getParameter("endpoint");
		response.setCharacterEncoding("UTF-8");

		//TODO
		//FIXME
		endpoint="http://localhost:3030/test/query";
		//get dimensions
		QueryEngine engine = new QueryEngine();
		Collection<RdfResource> dimensions = engine.getDimensions(endpoint);
		response.setHeader("Content-Type", "application/json");
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0);

		Writer w = response.getWriter();
		JSONWriter writer = new JSONWriter(w);
		try{
			writer.array();
			//search facet
			writer.object();
			writer.key("type"); writer.value("search");
		  	writer.key("config");
		  	writer.object();
		  	writer.key("name"); writer.value("search");
		  	writer.key("property"); writer.value(" ?dimensionProp ?dimensionPropVal. ?dimensionProp a <http://purl.org/linked-data/cube#DimensionProperty>. ?x ?dimensionProp ");
		  	writer.key("endpoint_vendor"); writer.value("standard");
		   	writer.endObject();
		  	writer.endObject();	
		  	//dataset facet
		  	writer.object();
		  	writer.key("type"); writer.value("list");
  			writer.key("config");
  			writer.object();
  			writer.key("name"); writer.value("datasets");
		  	writer.key("property"); writer.value(" <http://purl.org/linked-data/cube#dataSet> ");
		  	writer.key("expression"); writer.value("value");
		  	writer.key("propertyUri"); writer.value("http://purl.org/linked-data/cube#dataSet");
		  	writer.endObject();
		  	writer.endObject();
		  	//measures facet
		  	writer.object();
		  	writer.key("type"); writer.value("list");
  			writer.key("config");
  			writer.object();
  			writer.key("name"); writer.value("measures");
		  	writer.key("property"); writer.value(" ?measureProp ?measurePropVal. ?measureProp a <http://purl.org/linked-data/cube#MeasureProperty>. ?measureProp <http://www.w3.org/2000/01/rdf-schema#label> ");
		  	writer.key("expression"); writer.value("value");
		  	writer.key("propertyUri"); writer.value("http://purl.org/linked-data/cube#MeasureProperty");
		  	writer.endObject();
		  	writer.endObject();
		  	//add facet for each dimension
		  	int count = 1;
		  	for(RdfResource d:dimensions){
		  		writer.object();
		  		writer.key("type"); writer.value("list");
	  			writer.key("config");
	  			writer.object();
	  			writer.key("name"); writer.value(d.getLabel());
	  			writer.key("property"); writer.value(" <" + d.getUri() + "> ");
	  			writer.key("expression"); writer.value("value");
	  			writer.key("propertyUri"); writer.value(d.getUri());
	  			writer.endObject();
		  		writer.endObject();
		  		count +=1;
		  	}
		  	
		  	writer.endArray();
		}catch(JSONException je){
			respondException(response, je);
		}
	}

	
}
