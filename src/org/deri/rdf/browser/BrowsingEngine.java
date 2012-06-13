package org.deri.rdf.browser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.deri.rdf.browser.facet.FacetChoiceComputer;
import org.deri.rdf.browser.facet.RdfFacet;
import org.deri.rdf.browser.facet.RdfListFacet;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.deri.rdf.browser.util.ParsingUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.refine.Jsonizable;

public class BrowsingEngine implements Jsonizable{

	private FederatedQueryEngine sparqlEngine;
	private FederatedRdfEngine rdfEngine;
	
	private String mainFilter;
	private String graphUri;
	private String[] endpoints;
	protected List<RdfFacet> _facets = new LinkedList<RdfFacet>();
	protected String template;
	protected Set<String> properties;
	
	protected Set<Filter> filters = new HashSet<Filter>();
	
	public BrowsingEngine(FederatedQueryEngine sparqlEngine, FederatedRdfEngine rdfEngine) {
		this.sparqlEngine = sparqlEngine;
		this.rdfEngine = rdfEngine;
	}
	
	public BrowsingEngine(FederatedQueryEngine federatedQueryEngine, FederatedRdfEngine federatedRdfEngine, String[] endpoints,	String mainFilter) {
		this(federatedQueryEngine,federatedRdfEngine);
		this.endpoints = endpoints;
		this.mainFilter = mainFilter;
	}

	public void initializeFromJSON(JSONObject o) throws JSONException, XPathExpressionException {
        if (o == null) {
            return;
        }
        endpoints = o.getString("sparqlEndpointUrls").split(" ");
        
        //TODO graph uri is ignored for now.
        if(o.has("graph")){
        	graphUri = o.getString("graph");
        }
        mainFilter = o.getString("mainResourcesSelector");
        template = o.getString("template");
        properties = ParsingUtilities.getProperties(template);
        
        if (o.has("facets") && !o.isNull("facets")) {
            JSONArray a = o.getJSONArray("facets");
            int length = a.length();
            
            for (int i = 0; i < length; i++) {
                JSONObject fo = a.getJSONObject(i);
                String type = fo.has("type") ? fo.getString("type") : "rdf-property-list";
                
                RdfFacet facet = null;
                if ("rdf-property-list".equals(type)) {
                    facet = new RdfListFacet();
                }else if("rdf-search".equals(type)){
                	//TODO restore support for text search facets
                	//facet = new RdfSearchFacet();
                }else if("rdf-range".equals(type)){
                	//TODO restore support for range facets
                	//facet = new RdfRangeFacet();
                }
                
                if (facet != null) {
                    facet.initializeFromJSON(fo);
                    _facets.add(facet);
                    
                    filters.add(facet.getFilter());
                }
            }
        }
	}


	public List<AnnotatedResultItem> getPropertiesWithCount(RdfFacet facet) {
		String property = facet.getFilter().getProperty();
		Set<Filter> filtersWorkingCopy = new HashSet<Filter>(filters);
		filtersWorkingCopy.remove(facet.getFilter());
		String[] sparqls = sparqlEngine.propertiesWithCountSparql(endpoints, mainFilter, filtersWorkingCopy, property);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparqls,endpoints,filtersWorkingCopy.isEmpty());
		if(!filters.isEmpty()){
			String[] propSparqls = sparqlEngine.propertiesSparql(endpoints, mainFilter, property);
			rdfEngine.annotatePropertiesWithEndpoints(propSparqls, endpoints, items);
		}
		
		String[] missingValSparqls = sparqlEngine.propertiesMissingValueSparql(endpoints, mainFilter, filtersWorkingCopy, property);
		AnnotatedResultItem item = rdfEngine.countResourcesMissingProperty(missingValSparqls, endpoints);
		if(item.getCount()>0){
			items.add(item);
		}
		
		return items;
	}
	
	public Collection<RdfResource> getResources(int offset, int limit){
		String sparql = sparqlEngine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		//use the first endpoint by default. doesn't matter as the query explicitly set the SERVICE for each triple pattern
		Set<String> uris = rdfEngine.getResources(sparql, endpoints[0]);
		String detailsSparql = sparqlEngine.resourcesDetailsSparql(uris, properties);
		return rdfEngine.getRdfResources(detailsSparql, endpoints);
	}

	public long getResourcesCount(){
		String sparql = sparqlEngine.resourcesCountSparql(endpoints, mainFilter, filters);
		return rdfEngine.getResourcesCount(sparql, endpoints);
	}
	
	public void write(JSONWriter writer, Properties option) throws JSONException {

		writer.object();
		writer.key("sparqlEndpointUrls");
		String endpointsStr = "";
		for(String s:endpoints){
			endpointsStr += s + " "; 
		}
		writer.value(endpointsStr);

		writer.key("facets");
		writer.array();
		for (RdfFacet facet : _facets) {
			facet.write(writer);
		}
		writer.endArray();
		writer.endObject();
	}
	
	public void computeFacets(){
		List<Thread> threads = new ArrayList<Thread>(_facets.size());
		for(RdfFacet f:_facets){
			FacetChoiceComputer task = new FacetChoiceComputer(f,this);
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
	
	public void setFilters(Set<Filter> fs){
		this.filters = fs;
	}
}
