package org.deri.rdf.browser.facet;

import java.util.List;

import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.sparql.model.Filter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public interface RdfFacet {

    public void setChoices(List<AnnotatedResultItem> items);
    
    public void initializeFromJSON(JSONObject o) throws JSONException;
    
    public void write(JSONWriter writer)throws JSONException;

	public boolean hasSelection();

	public String getName();
	
	public Filter getFilter();
}