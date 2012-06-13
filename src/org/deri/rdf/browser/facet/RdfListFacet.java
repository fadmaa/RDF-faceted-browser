package org.deri.rdf.browser.facet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.model.Filter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.refine.util.JSONUtilities;

public class RdfListFacet implements RdfFacet{

	/*
     * Configuration
     */
    protected String     _name;
    protected String     _expression;
    protected boolean    _invert;
    
    // If true, then facet won't show the blank and error choices
    protected boolean _omitBlank;
    protected boolean _omitError;
    
    protected Filter filter;
    
    protected boolean _selectError;
    
    /*
     * Derived configuration
     */
    protected String     _errorMessage;
    
    /*
     * Computed results
     */
    protected List<AnnotatedResultItem> _choices = new LinkedList<AnnotatedResultItem>();
    protected int _blankCount;
    protected int _errorCount;
    
    
    @Override
	public String getName() {
    	return _name;
	}
    
    @Override
	public Filter getFilter() {
    	return filter;
	}

	@Override
    public void initializeFromJSON(JSONObject o) throws JSONException {
        _name = o.getString("name");
        _expression = o.getString("expression");
        String property = o.getString("property");
        _invert = o.has("invert") && o.getBoolean("invert");
        
        filter = new Filter(property);
        
        JSONArray a = o.getJSONArray("selection");
        int length = a.length();
        
        for (int i = 0; i < length; i++) {
            JSONObject oc = a.getJSONObject(i);
            JSONObject ocv = oc.getJSONObject("v");
            
//            DecoratedValue decoratedValue = new DecoratedValue(ocv.get("v"), ocv.getString("l"));

          //TODO switch to decorated value as it allows a label different than the value
           JSONArray endpoints = ocv.getJSONArray("ep");
           for(int k=0;k<endpoints.length();k++){
        	   filter.addValue(endpoints.getString(k), ocv.getString("v"), ocv.getInt("t"));
           }
        }
        
        _omitBlank = JSONUtilities.getBoolean(o, "omitBlank", false);
        _omitError = JSONUtilities.getBoolean(o, "omitError", false);
        
        boolean blankSelected = JSONUtilities.getBoolean(o, "selectBlank", false);
        if(blankSelected){
        	filter.addMissingValue();
        }
        _selectError = JSONUtilities.getBoolean(o, "selectError", false);
    }

	@Override
	public void setChoices(List<AnnotatedResultItem> items) {
		this._choices = new ArrayList<AnnotatedResultItem>(items);
	}

	public boolean hasSelection(){
		return filter.selected();
	}

	@Override
    public void write(JSONWriter writer) throws JSONException {
        
        writer.object();
        writer.key("name"); writer.value(_name);
        writer.key("expression"); writer.value(_expression);
        writer.key("property"); writer.value(filter.getProperty());
        writer.key("invert"); writer.value(_invert);
        
        if (_errorMessage != null) {
            writer.key("error"); writer.value(_errorMessage);
        } else if (_choices.size() > getLimit()) {
            writer.key("error"); writer.value("Too many choices");
        } else {
            writer.key("choices"); writer.array();
            for (AnnotatedResultItem choice : _choices) {
            	if(choice.getValue().getType()==RdfDecoratedValue.NULL){
            		_blankCount = choice.getCount();
            	}else{
            		choice.write(writer, filter.contains(choice));
            	}
            }
            writer.endArray();
            
            if (!_omitBlank && (filter.missingValueIncluded() || _blankCount > 0)) {
                writer.key("blankChoice");
                writer.object();
                writer.key("s"); writer.value(filter.missingValueIncluded());
                writer.key("c"); writer.value(_blankCount);
                writer.endObject();
            }
            if (!_omitError && (_selectError || _errorCount > 0)) {
                writer.key("errorChoice");
                writer.object();
                writer.key("s"); writer.value(_selectError);
                writer.key("c"); writer.value(_errorCount);
                writer.endObject();
            }
        }
        
        writer.endObject();
    }
	
	protected int getLimit() {
		//TODO make this configurable
        return 2000;
    }
	
}
