package org.deri.rdf.browser.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.deri.rdf.browser.facet.RdfFacet;
import org.deri.rdf.browser.facet.RdfListFacet;
import org.deri.rdf.browser.sparql.QueryEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.refine.Jsonizable;

public class RdfEngine implements Jsonizable{
	protected List<RdfFacet> _facets = new LinkedList<RdfFacet>();
	protected String sparqlEndpointUrl;
	protected String mainResourcesSelector;
	protected String template;

	public void initializeFromJSON(JSONObject o) throws JSONException {
        if (o == null) {
            return;
        }
        sparqlEndpointUrl = o.getString("sparqlEndpointUrl");
        mainResourcesSelector = o.getString("mainResourcesSelector");
//        template = o.getString("template");
        if (o.has("facets") && !o.isNull("facets")) {
            JSONArray a = o.getJSONArray("facets");
            int length = a.length();
            
            for (int i = 0; i < length; i++) {
                JSONObject fo = a.getJSONObject(i);
                String type = fo.has("type") ? fo.getString("type") : "rdf-property-list";
                
                RdfFacet facet = null;
                if ("rdf-property-list".equals(type)) {
                    facet = new RdfListFacet();
                } 
                
                if (facet != null) {
                    facet.initializeFromJSON(fo);
                    _facets.add(facet);
                }
            }
        }
	}
	
	public void computeFacets(QueryEngine engine){
		List<Thread> threads = new ArrayList<Thread>(_facets.size());
		for(RdfFacet f:_facets){
			FacetChoiceComputer task = new FacetChoiceComputer(f, sparqlEndpointUrl, mainResourcesSelector, engine, getFilters(f));
			Thread worker = new Thread(task);
			worker.setName(f.toString());
			worker.start();
			//some delay
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			threads.add(worker);
		}
		int running = 0;
		do {
			running = 0;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					running++;
				}
			}
		} while (running > 0);
		//return
	}
	
	public Collection<RdfResource> getResources(QueryEngine queryEngine, int offset, int limit){
		return queryEngine.getResources(sparqlEndpointUrl,mainResourcesSelector, getFilters(), offset, limit);
	}

	public int getFilteredResourcesCount(QueryEngine queryEngine){
		return queryEngine.getResourcesCount(sparqlEndpointUrl,mainResourcesSelector, getFilters());		
	}
	
	public void write(JSONWriter writer, Properties option) throws JSONException {

		writer.object();
		writer.key("sparqlEndpointUrl"); writer.value(sparqlEndpointUrl);
		writer.key("facets");
		writer.array();
		for (RdfFacet facet : _facets) {
			facet.write(writer);
		}
		writer.endArray();
		writer.endObject();
	}
	
	protected SetMultimap<String, String> getFilters(RdfFacet except){
		SetMultimap<String, String> filters = HashMultimap.create();
		for(RdfFacet f:_facets){
			if(f.equals(except)){
				continue;
			}
			if(f.hasSelection()){
				for(String s:f.getSelection()){
					filters.put(f.getSparqlSelector(), s);
				}
			}
			if(f.isBlankSelected()){
				filters.put(f.getSparqlSelector(), null);
			}
			
		}
		return filters;
	}
	
	protected SetMultimap<String, String> getFilters(){
		return getFilters(null);
	}
}
