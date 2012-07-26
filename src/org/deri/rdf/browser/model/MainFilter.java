package org.deri.rdf.browser.model;

import org.json.JSONException;
import org.json.JSONWriter;

public class MainFilter {

	private String varname;
	private final String facetsVarname;
	private final String pattern;
	private String sparqlPattern;

	public MainFilter(String varname, String facetsVarname, String pattern) {
		this.varname = varname;
		this.facetsVarname = facetsVarname;
		this.pattern = pattern;
		sparqlPattern = "?" + varname + " " + pattern;
	}
	
	public String getVarname() {
		return varname;
	}
	
	public String getFacetsVarname() {
		return facetsVarname;
	}

	public void setVarname(String varname) {
		this.varname = varname;
		sparqlPattern = "?" + varname + " " + pattern;
	}

	public String getSparqlPattern() {
		return sparqlPattern;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	public MainFilter extend(String newvarname, String newPattern){
		return new MainFilter(newvarname, facetsVarname, newPattern + "?" + varname +". " + sparqlPattern); 
	}

	public void write(JSONWriter writer) throws JSONException{
		writer.object();
		writer.key("varname"); writer.value(this.getVarname());
		writer.key("facetsVarname"); writer.value(this.facetsVarname);
		writer.key("pattern"); writer.value(this.getPattern());
		writer.endObject();
	}
	
}
