package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.model.RdfEngine;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.browser.sparql.QueryEngine;
import org.json.JSONException;
import org.json.JSONWriter;

public class GetResourcesCommand extends RdfCommand{

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		int offset = getIntegerParameter(request, "offset", 0);
		int limit = getIntegerParameter(request, "limit", 10);
		RdfEngine engine;
		try{
			engine = getRdfEngine(request);
		}catch(JSONException je){
			respondException(response, je);
			return;
		}
		QueryEngine queryEngine = new QueryEngine();
		Collection<RdfResource> resources = engine.getResources(queryEngine,offset,limit);
			
		response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "application/json");
        
        Writer w = response.getWriter();
        JSONWriter writer = new JSONWriter(w);
        try{
        	writer.object();
        	writer.key("code"); writer.value("ok");
        	writer.key("resources");
        	writer.array();
        	for(RdfResource r:resources){
        		r.write(writer);
        	}
        	writer.endArray();
        	
        	writer.key("limit"); writer.value(limit);
        	writer.key("offset"); writer.value(offset);
        	
        	writer.endObject();
        }catch(JSONException e){
        	e.printStackTrace(response.getWriter());
        }
	}

	
}
