package org.deri.rdf.browser.facet;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.deri.rdf.browser.model.AnnotatedString;
import org.deri.rdf.browser.sparql.QueryEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.common.collect.SetMultimap;
import com.google.refine.browsing.DecoratedValue;
import com.google.refine.browsing.facets.NominalFacetChoice;
import com.google.refine.util.JSONUtilities;

public class RdfListFacet implements RdfFacet{

	/*
     * Configuration
     */
    protected String     _name;
    protected String     _expression;
    protected String     sparqlSelector;
    protected boolean    _invert;
    
    // If true, then facet won't show the blank and error choices
    protected boolean _omitBlank;
    protected boolean _omitError;
    
    protected List<String> _selection = new LinkedList<String>();
    protected boolean _selectBlank;
    protected boolean _selectError;
    
    /*
     * Derived configuration
     */
    protected String     _errorMessage;
    
    /*
     * Computed results
     */
    protected List<NominalFacetChoice> _choices = new LinkedList<NominalFacetChoice>();
    protected int _blankCount;
    protected int _errorCount;
    
    
    @Override
	public String getName() {
    	return _name;
	}

	@Override
    public void initializeFromJSON(JSONObject o) throws JSONException {
        _name = o.getString("name");
        _expression = o.getString("expression");
        sparqlSelector = o.getString("property");
        _invert = o.has("invert") && o.getBoolean("invert");
        
        _selection.clear();
        
        JSONArray a = o.getJSONArray("selection");
        int length = a.length();
        
        for (int i = 0; i < length; i++) {
            JSONObject oc = a.getJSONObject(i);
            JSONObject ocv = oc.getJSONObject("v");
            
            DecoratedValue decoratedValue = new DecoratedValue(
                ocv.get("v"), ocv.getString("l"));
            
            NominalFacetChoice nominalFacetChoice = new NominalFacetChoice(decoratedValue);
            nominalFacetChoice.selected = true;
            
            _selection.add(ocv.get("v").toString());
        }
        
        _omitBlank = JSONUtilities.getBoolean(o, "omitBlank", false);
        _omitError = JSONUtilities.getBoolean(o, "omitError", false);
        
        _selectBlank = JSONUtilities.getBoolean(o, "selectBlank", false);
        _selectError = JSONUtilities.getBoolean(o, "selectError", false);
    }

	@Override
	public void computeChoices(String[] sparqlEndpoints, String graphUri, QueryEngine engine, String filter, SetMultimap<RdfFacet, RdfDecoratedValue> filters) {
		List<AnnotatedString> values = engine.getPropertiesWithCount(sparqlEndpoints, graphUri, this.sparqlSelector, filter, filters);
		for(AnnotatedString cs:values){
			if(cs.value==null){
				//blank choices
				_blankCount = cs.getCount();
				continue;
			}
			String val;
			if(cs.type==AnnotatedString.RESOURCE){
				val = "<" + cs.value + ">";
			}else{
				val = "\"" + cs.value + "\"";
			}
			NominalFacetChoice choice = new NominalFacetChoice(new DecoratedValue(val, cs.value));
			choice.count = cs.getCount();
			if(_selection.contains(val)){
				choice.selected = true;
			}
			this._choices.add(choice);
		}
	}

	@Override
	public String getResourceSparqlSelector(String varname, RdfDecoratedValue val) {
		return "?" + varname + " " + sparqlSelector + " " + val.getValue();
	}
	
	@Override
	public String getLiteralSparqlSelector(String mainSelector, String varname, String auxVarName, RdfDecoratedValue val) {
		return "?" + varname + " " +  sparqlSelector + " ?" + auxVarName + " . FILTER(str(?" + auxVarName + ")=" + val.getValue() + ") ";
	}

	public boolean hasSelection(){
		return ! this._selection.isEmpty();
	}

	public List<RdfDecoratedValue> getSelection(){
		List<RdfDecoratedValue> lst = new LinkedList<RdfDecoratedValue>();
		for(String s:this._selection){
			if (s.startsWith("<")){
				lst.add(new RdfDecoratedValue(s, false));
			}else{
				lst.add(new RdfDecoratedValue(s, true));
			}
		}
		return lst;
	}
	
	@Override
    public void write(JSONWriter writer) throws JSONException {
        
        writer.object();
        writer.key("name"); writer.value(_name);
        writer.key("expression"); writer.value(_expression);
        writer.key("property"); writer.value(sparqlSelector);
        writer.key("invert"); writer.value(_invert);
        
        if (_errorMessage != null) {
            writer.key("error"); writer.value(_errorMessage);
        } else if (_choices.size() > getLimit()) {
            writer.key("error"); writer.value("Too many choices");
        } else {
            writer.key("choices"); writer.array();
            for (NominalFacetChoice choice : _choices) {
                choice.write(writer, new Properties());
            }
            writer.endArray();
            
            if (!_omitBlank && (_selectBlank || _blankCount > 0)) {
                writer.key("blankChoice");
                writer.object();
                writer.key("s"); writer.value(_selectBlank);
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
	
	public boolean isBlankSelected(){
		return _selectBlank;
	}
	
	protected int getLimit() {
		//TODO make this configurable
        return 2000;
    }
	
}
