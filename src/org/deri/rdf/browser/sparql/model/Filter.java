package org.deri.rdf.browser.sparql.model;

import org.deri.rdf.browser.facet.RdfDecoratedValue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class Filter {

	private String property;
	private SetMultimap<String, RdfDecoratedValue> endpointValuesMap;
	
	public Filter(String p) {
		this.property = p;
		this.endpointValuesMap = HashMultimap.create();
	}

	public void addValue(String endpoint, String value, boolean literal) {
		endpointValuesMap.put(endpoint, new RdfDecoratedValue(value, literal));
	}

	public String getProperty() {
		return property;
	}

	public SetMultimap<String, RdfDecoratedValue> getEndpointValuesMap() {
		return endpointValuesMap;
	}

	public void addValue(String endpoint, String value) {
		addValue(endpoint, value, true);
	}

}
