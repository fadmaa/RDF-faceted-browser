package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.BrowsingEngine;
import org.deri.rdf.browser.model.RdfResource;
import org.json.JSONWriter;

public class GetResourcesCommand extends RdfCommand{

	private static final long serialVersionUID = 7526472295622776147L;
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		try{
		int offset = getIntegerParameter(request, "offset", 0);
		int limit = getIntegerParameter(request, "limit", 10);
		BrowsingEngine engine = getRdfEngine(request);
		
		Collection<RdfResource> resources = engine.getResources(offset,limit);
			
		response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "application/json");
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0);
        
        Writer w = response.getWriter();
        JSONWriter writer = new JSONWriter(w);
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
        }catch (Exception e) {
			respondException(response, e);
        }
	}

	
}
