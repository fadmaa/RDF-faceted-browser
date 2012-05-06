package org.deri.rdf.qb.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class Observation {


	protected ArrayList<Dimension> dimensions;
	protected Measure measure;
	protected String value;
	protected HashMap<String, String> properties = new HashMap<String, String>(); 
	
	protected final String uri;

	
	public HashMap<String, String> getProperties() {
		return properties;
	}
	public Observation(String uri) {
		this.uri = uri;
	}
	public ArrayList<Dimension> getDimensions() {
		return dimensions;
	}
	public void setDimensions(ArrayList<Dimension> dimensions) {
		this.dimensions = dimensions;
	}
	public Measure getMeasure() {
		return measure;
	}
	public void setMeasure(Measure measure) {
		this.measure = measure;
	}
	public String getUri() {
		return uri;
	}
	public void addDimension(Dimension dimension){
		if(this.dimensions==null){
			this.dimensions=new ArrayList<Dimension>();
		}
		this.dimensions.add(dimension);
		
	}
}
