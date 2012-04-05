package org.deri.rdf.browser.facet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deri.rdf.browser.model.AnnotatedString;
import org.deri.rdf.browser.sparql.QueryEngine;
import org.deri.rdf.browser.util.ParsingUtilities;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.common.collect.SetMultimap;
import com.google.refine.util.JSONUtilities;

public class RdfRangeFacet implements RdfFacet{


	/*
     * Configuration
     */
    protected String     _name;
    protected String     _expression;
    protected String     sparqlSelector;
    protected boolean    _invert;
    protected boolean _replaceCommas;
    
    // If true, then facet won't show the blank and error choices
    protected boolean _omitBlank;
    protected boolean _omitError;
    
    protected boolean _selectBlank;
    protected boolean _selectError;
    
    /*
     * Derived configuration
     */
    protected String     _errorMessage;
    
    /*
     * Computed results
     */
    protected double _from;
    protected double _to;
    protected boolean _selected;
    /*
     * Computed data, to return to the client side
     */
    protected double    _min;
    protected double    _max;
    protected double    _step;
    protected int[]     _baseBins;
    protected int[]     _bins;
    protected int       _numericCount;
    protected int       _nonNumericCount;
    protected int       _blankCount;
    protected int       _errorCount;
    
    
	@Override
	public void computeChoices(String sparqlEndpoint, QueryEngine engine, String filter, SetMultimap<RdfFacet, RdfDecoratedValue> filters) {
		List<AnnotatedString> values = engine.getPropertiesWithCount(sparqlEndpoint, this.sparqlSelector, filter, filters);
		List<CountedDouble> allValues = new ArrayList<CountedDouble>();
		_max = Double.NEGATIVE_INFINITY;
		_min = Double.POSITIVE_INFINITY;
		for(AnnotatedString a:values){
			try{
				Double v;
				if(_replaceCommas){
					v = ParsingUtilities.replaceCommas(a.value);
				}else{
					v = Double.parseDouble(a.value);
				}
				allValues.add(new CountedDouble(v,a.count));
				if(v<_min){
					_min = v;
				}
				if(v>_max){
					_max = v;
				}
			}catch(Exception ne){
				_errorCount += 1;
			}
		}
		
		computeBins(allValues);
	}

	@Override
	public void initializeFromJSON(JSONObject o) throws JSONException {
		_name = o.getString("name");
		_replaceCommas = o.has("replaceCommas") && o.getBoolean("replaceCommas");
        _expression = o.getString("expression");
        sparqlSelector = o.getString("property");
        _invert = o.has("invert") && o.getBoolean("invert");
        
        if (o.has("from") || o.has("to")) {
            _from = o.has("from") ? o.getDouble("from") : _min;
            _to = o.has("to") ? o.getDouble("to") : _max;
            _selected = true;
        }
        
        _omitBlank = JSONUtilities.getBoolean(o, "omitBlank", false);
        _omitError = JSONUtilities.getBoolean(o, "omitError", false);
        
        _selectBlank = JSONUtilities.getBoolean(o, "selectBlank", false);
        _selectError = JSONUtilities.getBoolean(o, "selectError", false);
	}

	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		
		writer.key("name");writer.value("year");
		writer.key("expression");writer.value("value");
		writer.key("min");writer.value(_min);
		writer.key("max");writer.value(_max);
		writer.key("step");writer.value(_step);
		writer.key("bins");
		writer.array();
		for(int i=0;i<_bins.length;i++){
			writer.value(_bins[i]);
		}
		writer.endArray();
		
		writer.key("baseBins");
		writer.array();
		for(int i=0;i<_baseBins.length;i++){
			writer.value(_baseBins[i]);
		} 
		writer.endArray();
	
		writer.key("from"); writer.value(_from);
		writer.key("to"); writer.value(_to);
		writer.key("baseNumericCount"); writer.value(4);
		writer.key("baseNonNumericCount");writer.value(0);
		writer.key("baseBlankCount");writer.value(0);
		writer.key("baseErrorCount");writer.value(0);
		writer.key("numericCount");writer.value(4);
		writer.key("nonNumericCount");writer.value(0);
		writer.key("blankCount");writer.value(0);
		writer.key("errorCount");writer.value(0);
		writer.endObject();
		
	}

	@Override
	public boolean hasSelection() {
		return _selected; 
	}

	@Override
	public List<RdfDecoratedValue> getSelection() {
		List<RdfDecoratedValue> lst = new LinkedList<RdfDecoratedValue>();
		lst.add(new RdfDecoratedValue(new Double[]{_from,_to}, true));
		return lst;
	}

	@Override
	public String getResourceSparqlSelector(String varname, RdfDecoratedValue val) {
		return null;
	}

	@Override
	public String getLiteralSparqlSelector(String varname, String auxVarName, RdfDecoratedValue val) {
		Double[] range = (Double[])val.getValue();
		if(_replaceCommas){
			return "?" + varname + " " +  sparqlSelector + " ?" + auxVarName + " . FILTER(?" + auxVarName 
			+ ">='" + ParsingUtilities.putCommasBack(range[0]) + "' && ?" + auxVarName + "<='" + ParsingUtilities.putCommasBack(range[1]) + "') ";
		}else{
			return "?" + varname + " " +  sparqlSelector + " ?" + auxVarName + " . FILTER(<http://www.w3.org/2001/XMLSchema#double>(?" + auxVarName 
			+ ")>=" + range[0] + " && <http://www.w3.org/2001/XMLSchema#double>(?" + auxVarName + ")<=" + range[1] + ") ";
		}
	}

	@Override
	public boolean isBlankSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return _name;
	}
	
	private void computeBins(List<CountedDouble> allValues){
        if (_min >= _max) {
            _step = 1;
            _min = Math.min(_min, _max);
            _max = _step;
            _bins = new int[1];
            
            return;
        }
        
        double diff = _max - _min;
        
        _step = 1;
        if (diff > 10) {
            while (_step * 100 < diff) {
                _step *= 10;
            }
        } else {
            while (_step * 100 > diff) {
                _step /= 10;
            }
        }
        
        double originalMax = _max;
        _min = (Math.floor(_min / _step) * _step);
        _max = (Math.ceil(_max / _step) * _step);
        
        double binCount = (_max - _min) / _step;
        if (binCount > 100) {
            _step *= 2;
            binCount = (binCount + 1) / 2;
        }
        
        if (_max <= originalMax) {
            _max += _step;
            binCount++;
        }
        
        _bins = new int[(int) Math.round(binCount)];
        for (CountedDouble cd : allValues) {
            int bin = Math.max((int) Math.floor((cd.v - _min) / _step),0);
            _bins[bin] += cd.count;
        }
        if (_selected) {
            _from = Math.max(_from, _min);
            _to = Math.min(_to, _max);
        } else {
            _from = _min;
            _to = _max;
        }
        //TOD check if this makes sense
        _baseBins = _bins;
        
	}

	private static class CountedDouble{
		int count;
		Double v;
		CountedDouble(Double v, int count){
			this.count = count;
			this.v = v;
		}
	}
	
}
