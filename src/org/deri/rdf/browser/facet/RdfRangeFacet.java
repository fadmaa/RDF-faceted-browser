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

import com.google.common.collect.HashMultimap;
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
    
    protected boolean   _selectNumeric; // whether the numeric selection applies, default true
    protected boolean   _selectNonNumeric;
    protected boolean   _selectBlank;
    protected boolean   _selectError;
    
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
    
    protected int       _baseNumericCount;
    protected int       _baseNonNumericCount;
    protected int       _baseBlankCount;
    
    
	@Override
	public void computeChoices(String[] sparqlEndpoints, String graphUri, QueryEngine engine, String filter, SetMultimap<RdfFacet, RdfDecoratedValue> filters) {
		List<AnnotatedString> values = engine.getPropertiesWithCount(sparqlEndpoints, graphUri, this.sparqlSelector, filter, filters);
		SetMultimap<RdfFacet, RdfDecoratedValue> noFilters = HashMultimap.create();
		List<AnnotatedString> allValues = engine.getPropertiesWithCount(sparqlEndpoints, graphUri, this.sparqlSelector, filter, noFilters);
		List<CountedDouble> filteredValues = new ArrayList<CountedDouble>();
		List<CountedDouble> allValuesCounted = new ArrayList<CountedDouble>();
		_max = Double.NEGATIVE_INFINITY;
		_min = Double.POSITIVE_INFINITY;
		for(AnnotatedString a:allValues){
			try{
				Double v;
				if(a.value==null){
					_baseBlankCount += a.getCount();
					continue;
				}
				if(_replaceCommas){
					v = ParsingUtilities.replaceCommas(a.value);
				}else{
					v = Double.parseDouble(a.value);
				}
				allValuesCounted.add(new CountedDouble(v,a.getCount()));
				if(v<_min){
					_min = v;
				}
				if(v>_max){
					_max = v;
				}
				_baseNumericCount += a.getCount();
			}catch(Exception ne){
				_baseNonNumericCount += a.getCount();
			}
		}
		
		for(AnnotatedString a:values){
			try{
				Double v;
				if(a.value==null){
					_blankCount += a.getCount();
					continue;
				}
				if(_replaceCommas){
					v = ParsingUtilities.replaceCommas(a.value);
				}else{
					v = Double.parseDouble(a.value);
				}
				filteredValues.add(new CountedDouble(v,a.getCount()));
				_numericCount += a.getCount();
			}catch(Exception ne){
				_nonNumericCount += a.getCount();
			}
		}
		computeBins(allValuesCounted, filteredValues);
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
        
        _selectNumeric = JSONUtilities.getBoolean(o, "selectNumeric", true);
        _selectNonNumeric = JSONUtilities.getBoolean(o, "selectNonNumeric", true);
        _selectBlank = JSONUtilities.getBoolean(o, "selectBlank", true);
        _selectError = JSONUtilities.getBoolean(o, "selectError", true);
        
	}

	@Override
	public void write(JSONWriter writer) throws JSONException {
		writer.object();
		
		writer.key("name");writer.value("year");
		writer.key("expression");writer.value("value");
		if(_min==Double.NEGATIVE_INFINITY || _max ==Double.POSITIVE_INFINITY){
			writer.key("code"); writer.value("nodata");
			writer.endObject();
			return;
		}
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
		writer.key("baseNumericCount"); writer.value(_baseNumericCount);
		writer.key("baseNonNumericCount");writer.value(_baseNonNumericCount);
		writer.key("baseBlankCount");writer.value(_baseBlankCount);
		writer.key("baseErrorCount");writer.value(0);
		writer.key("numericCount");writer.value(_numericCount);
		writer.key("nonNumericCount");writer.value(_nonNumericCount);
		writer.key("blankCount");writer.value(_blankCount);
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
	public String getLiteralSparqlSelector(String mainSelector, String varname, String auxVarName, RdfDecoratedValue val) {
		Double[] range = (Double[])val.getValue();
		String auxVarValue, minRange,maxRange;
		if(_replaceCommas){
			auxVarValue = "?" + auxVarName;
			maxRange = "'" + ParsingUtilities.putCommasBack(range[1]) + "'";
			minRange = "'" + ParsingUtilities.putCommasBack(range[0]) + "'";
		}else{
			auxVarValue = "<http://www.w3.org/2001/XMLSchema#double>(?" + auxVarName + ")";
			maxRange = String.valueOf(range[1]);
			minRange = String.valueOf(range[0]);
		}
		return "?" + varname + " " + mainSelector + ". OPTIONAL{ ?" + varname + " " +  sparqlSelector + " ?" + auxVarName 
		+ " } FILTER((bound (?" + auxVarName +
		") && " + auxVarValue  + ">=" + minRange + " && " + auxVarValue + "<=" +  maxRange
		+ ")|| !bound(?" + auxVarName + ")) ";
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
	
	private void computeBins(List<CountedDouble> allValues,List<CountedDouble> filteredValues){
        if (_min >= _max) {
            _step = 1;
            _min = Math.min(_min, _max);
            _max = _step;
            _baseBins = new int[1];
            
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
        
        _baseBins = new int[(int) Math.round(binCount)];
        for (CountedDouble cd : allValues) {
            int bin = Math.max((int) Math.floor((cd.v - _min) / _step),0);
            _baseBins[bin] += cd.count;
        }
        _bins = new int[(int) Math.round(binCount)];
        for (CountedDouble cd : filteredValues) {
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
