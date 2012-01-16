package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.model.RdfEngine;
import org.deri.rdf.browser.sparql.QueryEngine;
import org.json.JSONException;
import org.json.JSONWriter;

public class CountResourcesCommand extends RdfCommand{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		RdfEngine engine;
		try{
			engine = getRdfEngine(request);
		}catch(JSONException je){
			respondException(response, je);
			return;
		}
		QueryEngine queryEngine = new QueryEngine();
		int filtered = engine.getFilteredResourcesCount(queryEngine); 
			
		response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "application/json");
        
        Writer w = response.getWriter();
        JSONWriter writer = new JSONWriter(w);
        try{
        	writer.object();
        	writer.key("code"); writer.value("ok");
        	writer.key("filtered");
        	writer.value(filtered);
        	writer.endObject();
        }catch(JSONException e){
        	e.printStackTrace(response.getWriter());
        }
	}

}
