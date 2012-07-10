package org.deri.rdf.browser.model;

import org.json.JSONException;
import org.json.JSONWriter;


public class AnnotatedResultItem {

	private int count;
	private final RdfDecoratedValue value;
	
	public AnnotatedResultItem(int count, String v, byte t){
		this.count = count;
		this.value = new RdfDecoratedValue(v, t);
	}
	
	public AnnotatedResultItem(String v,byte type){
		this(0,v,type);
	}

	public AnnotatedResultItem(int count) {
		this(count,"missing value",RdfDecoratedValue.NULL);
	}

	public void setCount(int c){
		this.count = c;
	}
	
	public int getCount(){
		return count;
	}
	
	public RdfDecoratedValue getValue(){
		return this.value;
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
		writer.endArray();
		writer.endObject();
		
		writer.key("c"); 
		writer.value(count);
		writer.key("s"); 
		writer.value(selected);
		writer.endObject();	
	}
}
