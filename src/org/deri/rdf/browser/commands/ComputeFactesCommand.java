package org.deri.rdf.browser.commands;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.BrowsingEngine;

public class ComputeFactesCommand extends RdfCommand{

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		try{
			BrowsingEngine engine = getRdfEngine(request);
			engine.computeFacets();
			respondJSON(response, engine);
		} catch (Exception e) {
			respondException(response, e);
        }
	}

}
