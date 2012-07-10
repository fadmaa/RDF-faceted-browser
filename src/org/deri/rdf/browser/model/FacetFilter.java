package org.deri.rdf.browser.model;

import org.json.JSONException;
import org.json.JSONWriter;

public class FacetFilter {
	private final String pattern;

	public FacetFilter(String f){
		this.pattern = f;
	}
	public String getPattern(){
		return pattern;
	}
	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		writer.key("pattern"); writer.value(pattern);
		writer.endObject();
	}
}
