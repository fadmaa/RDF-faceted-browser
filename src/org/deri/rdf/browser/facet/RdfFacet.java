package org.deri.rdf.browser.facet;

import java.util.List;

import org.deri.rdf.browser.sparql.QueryEngine;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.common.collect.SetMultimap;

public interface RdfFacet {

    public void computeChoices(String sparqlEndpoint,QueryEngine engine, String filter, SetMultimap<RdfFacet, String> filters);
    
    public void initializeFromJSON(JSONObject o) throws JSONException;
    
    public void write(JSONWriter writer)throws JSONException;

	public boolean hasSelection();

	public List<String> getSelection();

	public String getResourceSparqlSelector(String varname, String val);
	public String getLiteralSparqlSelector(String varname, String auxVarName, String val);
	
	public boolean isBlankSelected();
	
	public String getName();
}