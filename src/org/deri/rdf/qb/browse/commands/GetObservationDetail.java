package org.deri.rdf.qb.browse.commands;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.commands.RdfCommand;
import org.deri.rdf.qb.browse.sparql.QueryEngine;
import org.deri.rdf.qb.model.Dimension;
import org.deri.rdf.qb.model.Measure;
import org.deri.rdf.qb.model.Observation;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class GetObservationDetail extends RdfCommand {
	private Configuration cfg;

	public void init() {
		// Initialize the FreeMarker configuration;
		// - Create a configuration instance
		cfg = new Configuration();
		// - Templates are stoted in the WEB-INF/templates directory of the Web
		// app.
		cfg.setServletContextForTemplateLoading(getServletContext(),
				"WEB-INF/classes/tmpl");

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String observationURI = req.getParameter("uri");
		String endpoint = req.getParameter("endpoint");
		QueryEngine queryEngine = new QueryEngine();
		Observation observation = queryEngine.getObservationDetails(observationURI, endpoint);

		Map root = new HashMap();

		root.put("observation", observation);
		root.put("oburi", observationURI);
		root.put("endpoint", endpoint);
		root.put("properties", observation.getProperties());
		
		Template t = cfg.getTemplate("observation.html");
		resp.setContentType("text/html; charset=" + t.getEncoding());
		Writer out = resp.getWriter();

		// Merge the data-model and the template
		try {
			t.process(root, out);
		} catch (TemplateException e) {
			throw new ServletException(
					"Error while processing FreeMarker template", e);
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String observationURI = req.getParameter("uri");
		String endpoint = req.getParameter("endpoint");
		String dir = req.getParameter("dir");
		String dimuri = req.getParameter("dimuri");
		String dimval = req.getParameter("dimval");
		String dimensionsURIs = req.getParameter("arr_dimuri");
		String dimensionsValues = req.getParameter("arr_dimval");
		String measure = req.getParameter("measure");
		QueryEngine queryEngine = new QueryEngine();
		Observation observation=new Observation(observationURI);
		observation.setMeasure(new Measure(measure));
		String[] split = dimensionsURIs.split(",");
		String[] split2 = dimensionsValues.split(",");
		for (int i = 0; i < split.length; i++) {
			String uri = split[i];
			String val = split2[i];
			Dimension dimension=new Dimension(uri, null, val);
			observation.addDimension(dimension);
		}
		Dimension dimension=new Dimension(dimuri, null, dimval);
		String observationDetailsByDirection = queryEngine.getObservationDetailsByDirection(observation, endpoint, dir, dimension);
		resp.setCharacterEncoding("UTF-8");
    	resp.setHeader("Content-Type", "application/json");
		resp.getWriter().write("{\"status\":\"ok\",\"uri\":\""+observationDetailsByDirection+"\"}");
	}

}
