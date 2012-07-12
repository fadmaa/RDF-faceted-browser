package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.deri.rdf.browser.BrowsingEngine;
import org.deri.rdf.browser.RdfEngine;
import org.deri.rdf.browser.sparql.NaiveFederatedSparqlEngine;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.deri.rdf.browser.util.ParsingUtilities;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public abstract class RdfCommand extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected BrowsingEngine getRdfEngine(HttpServletRequest request) throws JSONException, XPathExpressionException {
		String json = request.getParameter("rdf-engine");
		JSONObject o = ParsingUtilities.evaluateJsonStringToObject(json);
		BrowsingEngine engine = new BrowsingEngine(new NaiveFederatedSparqlEngine(), new SparqlEngine(), new RdfEngine());
		engine.initializeFromJSON(o);

		return engine;
	}

	static protected int getIntegerParameter(HttpServletRequest request,
			String name, int def) {
		if (request == null) {
			throw new IllegalArgumentException(
					"parameter 'request' should not be null");
		}
		try {
			return Integer.parseInt(request.getParameter(name));
		} catch (Exception e) {
			// ignore
		}
		return def;
	}

	static protected void respondException(HttpServletResponse response,
			Exception e) throws IOException, ServletException {

		if (response == null) {
			throw new ServletException("Response object can't be null");
		}

		try {
			JSONObject o = new JSONObject();
			o.put("code", "error");
			o.put("message", e.getMessage());

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();

			o.put("stack", sw.toString());

			response.setCharacterEncoding("UTF-8");
			
			response.setHeader("Content-Type", "application/json");
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0);
			respond(response, o.toString());
		} catch (JSONException e1) {
			e.printStackTrace(response.getWriter());
		}
	}

	static protected void respond(HttpServletResponse response, String content)
			throws IOException, ServletException {

		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		Writer w = response.getWriter();
		if (w != null) {
			w.write(content);
			w.flush();
			w.close();
		} else {
			throw new ServletException("response returned a null writer");
		}
	}
	
	static protected void respondJSON(HttpServletResponse response, BrowsingEngine engine) throws IOException, JSONException {

		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Type", "application/json");

		Writer w = response.getWriter();
		JSONWriter writer = new JSONWriter(w);

		engine.write(writer);
		w.flush();
		w.close();
	}
}
