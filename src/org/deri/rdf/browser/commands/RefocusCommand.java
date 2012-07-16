package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.BrowsingEngine;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.util.ParsingUtilities;
import org.json.JSONObject;
import org.json.JSONWriter;

public class RefocusCommand extends RdfCommand{
	private static final long serialVersionUID = 6472295622776147L;
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		try{
			BrowsingEngine engine = getRdfEngine(request);
			String json = request.getParameter("refocus-facet");
			JSONObject o = ParsingUtilities.evaluateJsonStringToObject(json);
			Facet refocusFacet = new Facet();
			refocusFacet.initializeFromJSON(o,false);
			
			MainFilter newMainFilter = engine.refocus(refocusFacet);
			
			response.setCharacterEncoding("UTF-8");
        	response.setHeader("Content-Type", "application/json");
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0);
        
        	Writer w = response.getWriter();
        	JSONWriter writer = new JSONWriter(w);
        	writer.object();
        	writer.key("code"); writer.value("ok");
        	
        	writer.key("main_selector");
        	writer.object();
        	writer.key("pattern"); writer.value(newMainFilter.getPattern());
        	writer.key("varname"); writer.value(newMainFilter.getVarname());
        	writer.endObject();
        	
        	writer.endObject();
        	
		}catch (Exception e) {
			respondException(response, e);
        }
	}

}
