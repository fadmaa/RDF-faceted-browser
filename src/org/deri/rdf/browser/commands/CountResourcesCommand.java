package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.BrowsingEngine;
import org.json.JSONWriter;

public class CountResourcesCommand extends RdfCommand{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		try{
			BrowsingEngine engine = getRdfEngine(request);
			long filtered = engine.getResourcesCount(); 
			
			response.setCharacterEncoding("UTF-8");
        	response.setHeader("Content-Type", "application/json");
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0);
        
        	Writer w = response.getWriter();
        	JSONWriter writer = new JSONWriter(w);
        	writer.object();
        	writer.key("code"); writer.value("ok");
        	writer.key("filtered");
        	writer.value(filtered);
        	writer.endObject();
        } catch (Exception e) {
			respondException(response, e);
        }
	}

}
