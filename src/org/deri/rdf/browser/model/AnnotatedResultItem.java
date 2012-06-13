package org.deri.rdf.browser.model;

import org.json.JSONException;
import org.json.JSONWriter;


public class AnnotatedResultItem {

	private int count;
	private final RdfDecoratedValue value;
	//TODO change endpoints to LinkedList
	private String[] endpoints;
	
	public AnnotatedResultItem(int count, String v, int t, String[] eps){
		this.count = count;
		this.value = new RdfDecoratedValue(v, t);
		this.endpoints = eps;
	}
	
	public AnnotatedResultItem(String v,int type){
		this(0,v,type,new String[]{});
	}

	public AnnotatedResultItem(int count) {
		this(count,"missing value",RdfDecoratedValue.NULL,new String[]{});
	}

	public void setCount(int c){
		this.count = c;
	}
	
	public int getCount(){
		return count;
	}
	
	public String[] getEndpoints(){
		return endpoints;
	}
	
	public RdfDecoratedValue getValue(){
		return this.value;
	}
	
	public void addEndpoint(String ep){
		String[] newEndpoints = new String[endpoints.length + 1];
		for(int i=0;i<endpoints.length;i++){
			newEndpoints[i] = endpoints[i];
		}
		newEndpoints[endpoints.length] = ep;
		this.endpoints = newEndpoints;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj.getClass().equals(this.getClass())){
			AnnotatedResultItem other = (AnnotatedResultItem) obj;
			return this.value.equals(other.value);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString() + "(" + count + ")";
	}

	public void write(JSONWriter writer, boolean selected)  throws JSONException {
		writer.object();
		writer.key("v"); 
		
		writer.object();
		writer.key("v"); writer.value(this.value.getValue());
		writer.key("l"); writer.value(this.value.getValue());
		writer.key("t"); writer.value(this.value.getType());
		writer.key("ep"); 
		writer.array();
		for(String ep:endpoints){
			writer.value(ep);
		}
		writer.endArray();
		writer.endObject();
		
		writer.key("c"); 
		writer.value(count);
		writer.key("s"); 
		writer.value(selected);
		writer.endObject();	
	}
}
