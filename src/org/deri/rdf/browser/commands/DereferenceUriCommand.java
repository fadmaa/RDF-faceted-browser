package org.deri.rdf.browser.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.model.PreviewResourceCannedQuery;
import org.deri.rdf.browser.util.DereferencingUtilities;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

public class DereferenceUriCommand extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
		String uri = req.getParameter("uri");
		Multimap<String, String> props = null;
		try{
			Model model = DereferencingUtilities.dereference(uri);
			InputStream in = this.getClass().getResourceAsStream("/files/preview_properties.properties");
			PreviewResourceCannedQuery previewResourceCannedQuery = new PreviewResourceCannedQuery(in);
			String sparql = previewResourceCannedQuery.getPreviewQueryForResource(uri);
			Query query = QueryFactory.create(sparql, Syntax.syntaxSPARQL_11);
			QueryExecution qExec = QueryExecutionFactory.create(query, model);
			ResultSet resultset = qExec.execSelect();
			props = previewResourceCannedQuery.wrapResourcePropertiesMapResultSet(resultset);
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		out.print("<div>");
		out.print("<p><a target=\"_new\" href=\""); out.print(uri); out.print("\" >"); out.print(uri); out.print("</a></p>");
		if(props!=null){
			Collection<String> labels = props.get("labels");
			if(!labels.isEmpty()){
				//get the first one only
				String s = labels.iterator().next();
				out.print("<h2>");out.print(s);out.print("</h2>");
			}
			Collection<String> descriptions = props.get("descriptions");
			if(!descriptions.isEmpty()){
				//get the first one only
				String s = descriptions.iterator().next();
				out.print("<p>");out.print(s);out.print("</p>");
			}
			Collection<String> images = props.get("images");
			if(!images.isEmpty()){
				String s = images.iterator().next();
				out.print("<img src=\"");out.print(s);out.print("\" />");
			}
		}
		out.print("</div>");
	}

	
}
