package org.deri.rdf.browser.commands;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.model.RdfEngine;
import org.deri.rdf.browser.sparql.QueryEngine;
import org.json.JSONException;

public class ComputeFactesCommand extends RdfCommand{

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		try{
			RdfEngine engine = getRdfEngine(request);
			engine.computeFacets(new QueryEngine());
			respondJSON(response, engine);
		} catch (JSONException e) {
			respondException(response, e);
        }
	}

}
