package org.deri.rdf.browser.model;


import java.util.Collection;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class RdfResource {

	protected final RdfDecoratedValue value;
	protected SetMultimap<String, String> properties = HashMultimap.create(); 
	
	public RdfResource(RdfDecoratedValue v){
		this.value = v;
	}

	public RdfDecoratedValue getValue() {
		return value;
	}

	public SetMultimap<String, String> getProperties() {
		return properties;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		
		if(obj.getClass().equals(this.getClass())){
			return this.getValue().equals( ((RdfResource)obj).getValue());
		}
		return false;
	}
	
	public void write(JSONWriter writer) throws JSONException{
		writer.object();
		writer.key("@"); writer.value(this.value);
		for(Entry<String, Collection<String>> entry:properties.asMap().entrySet()){
			writer.key(entry.getKey());
			writer.array();
			for(String v:entry.getValue()){
				writer.value(v);
				
			}
			writer.endArray();
		}
		
		writer.endObject();
	}

	public void addProperty(String p, String v) {
		this.properties.put(p, v);
	}

	@Override
	public String toString() {
		return value.toString();
	}
	
}
