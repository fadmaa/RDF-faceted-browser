package org.deri.rdf.browser.sparql.model;

import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.RdfDecoratedValue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class Filter {

	private String property;
	private SetMultimap<String, RdfDecoratedValue> endpointValuesMap;
	private boolean missingValueIncluded;
	
	public Filter(String p) {
		this.property = p;
		this.endpointValuesMap = HashMultimap.create();
	}

	public String getProperty() {
		return property;
	}

	public SetMultimap<String, RdfDecoratedValue> getEndpointValuesMap() {
		return endpointValuesMap;
	}

	public void addValue(String endpoint, String value, int type) {
		endpointValuesMap.put(endpoint, new RdfDecoratedValue(value, type));
	}
	
	public void addMissingValue(){
		missingValueIncluded = true;
	}
	public boolean missingValueIncluded(){
		return missingValueIncluded;
	}

	public boolean selected() {
		return missingValueIncluded || ! endpointValuesMap.isEmpty();
	}

	public boolean contains(AnnotatedResultItem item) {
		return endpointValuesMap.containsValue(item.getValue());
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null){
			return false;
		}
		if(this.getClass().equals(obj.getClass())){
			Filter other = (Filter) obj;
			return this.property.equals(other.property);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.property.hashCode();
	}
	
}
