package org.deri.rdf.browser.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class Facet {

	private FacetFilter filter;
	private String varname;//must be unique i.e. unambiguously identify a facet
	private String name;
	private boolean missingValueSelected;
	private SetMultimap<String, RdfDecoratedValue> selections;
	protected List<AnnotatedResultItem> _choices = new LinkedList<AnnotatedResultItem>();
	protected int _blankCount;
	protected String _errorMessage;
	
	public Facet(FacetFilter f, String v,String n){
		this();
		this.filter = f;
		this.varname = v;
		this.name = n;
	}
	
	public Facet(){
		this.selections = HashMultimap.create();
	}
	
	public String getVarname(){
		return varname;
	}
	
	public void addResourceValue(String v){
		this.selections.put(DEFAULT_ENDPOINT, new RdfDecoratedValue(v, RdfDecoratedValue.RESOURCE));
	}
	
	public void addLiteralValue(String v){
		this.selections.put(DEFAULT_ENDPOINT, new RdfDecoratedValue(v, RdfDecoratedValue.LITERAL));
	}
	
	public void addResourceValue(String v, String ep){
		this.selections.put(ep ,new RdfDecoratedValue(v, RdfDecoratedValue.RESOURCE));
	}
	
	public void addLiteralValue(String v, String ep){
		this.selections.put(ep, new RdfDecoratedValue(v, RdfDecoratedValue.LITERAL));
	}
	
	public Set<RdfDecoratedValue> getSelections(){
		return selections.get(DEFAULT_ENDPOINT);
	}
	
	public FacetFilter getFilter(){
		return filter;
	}

	public boolean missingValueSelected() {
		return missingValueSelected;
	}
	public void setMissingValueSelected(boolean b) {
		missingValueSelected = b;
	}
	
	public void setChoices(List<AnnotatedResultItem> items){
		this._choices = new ArrayList<AnnotatedResultItem>(items);
	}
    
    public void initializeFromJSON(JSONObject o, boolean endpointsTracked) throws JSONException{
    	name = o.getString("name");
    	varname = o.getString("varname");
        filter = new FacetFilter(o.getJSONObject("filter").getString("pattern"));
        
        JSONArray a = o.getJSONArray("selection");
        int length = a.length();
        
        for (int i = 0; i < length; i++) {
            JSONObject oc = a.getJSONObject(i);
            JSONObject ocv = oc.getJSONObject("v");
            if(endpointsTracked){
            	JSONArray eps = ocv.getJSONArray("ep");
            	for(int k=0;k<eps.length();k++){
            		String ep = eps.getString(k);
            		selections.put(ep,new RdfDecoratedValue(ocv.getString("v"), (byte)ocv.getInt("t")));
            	}
            }else{
            	selections.put(DEFAULT_ENDPOINT,new RdfDecoratedValue(ocv.getString("v"), (byte)ocv.getInt("t")));
            }
        }
        
        missingValueSelected = o.has("selectBlank") && o.getBoolean("selectBlank");
    }
    
    public void write(JSONWriter writer)throws JSONException{
    	writer.object();
        writer.key("name"); writer.value(name);
        writer.key("varname"); writer.value(varname);
        writer.key("filter"); filter.write(writer);
        
        if (_errorMessage != null) {
            writer.key("error"); writer.value(_errorMessage);
        } else if (_choices.size() > getLimit()) {
            writer.key("error"); writer.value("Too many choices");
        } else {
            writer.key("choices"); 
            writer.array();
            for (AnnotatedResultItem choice : _choices) {
            	if(choice.getValue().getType()==RdfDecoratedValue.NULL){
            		_blankCount = choice.getCount();
            	}else{
            		choice.write(writer, selections.containsValue(choice.getValue()));
            	}
            }
            writer.endArray();
            
            if (missingValueSelected || _blankCount > 0) {
                writer.key("blankChoice");
                writer.object();
                writer.key("s"); writer.value(missingValueSelected);
                writer.key("c"); writer.value(_blankCount);
                writer.endObject();
            }
        }
        writer.endObject();
    }

	public boolean hasSelection(){
		return missingValueSelected || ! selections.isEmpty();
	}

	public String getName(){
		return name;
	}

	public SetMultimap<String, RdfDecoratedValue> getEndpointValuesMap() {
		return selections;
	} 
	
	protected int getLimit() {
		//TODO make this configurable
        return 2000;
    }

	@Override
	public boolean equals(Object obj) {
		if(obj!= null && obj.getClass().equals(this.getClass())){
			Facet otehr = (Facet)obj;
			return varname.equals(otehr.getVarname());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return varname.hashCode();
	}

	public void setFilter(FacetFilter facetFilter) {
		this.filter = facetFilter;
	}
	
	@Override
	public String toString(){
		return varname + selections;
	}
	
	private static final String DEFAULT_ENDPOINT ="default";

}
