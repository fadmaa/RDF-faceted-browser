package org.deri.rdf.browser;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.deri.rdf.browser.facet.RdfFacet;
import org.deri.rdf.browser.facet.RdfListFacet;
import org.deri.rdf.browser.facet.RdfRangeFacet;
import org.deri.rdf.browser.facet.RdfSearchFacet;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BrowsingEngine {

	private FederatedQueryEngine sparqlEngine;
	private FederatedRdfEngine rdfEngine;
	
	private String mainFilter;
	private String graphUri;
	private String[] endpoints;
	
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
        
        //TODO
        // graph uri is ignored for now.
        if(o.has("graph")){
        	graphUri = o.getString("graph");
        }
        mainFilter = o.getString("mainResourcesSelector");
        /*template = o.getString("template");
        properties = getProperties(template);
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
                	facet = new RdfSearchFacet();
                }else if("rdf-range".equals(type)){
                	facet = new RdfRangeFacet();
                }
                
                if (facet != null) {
                    facet.initializeFromJSON(fo);
                    _facets.add(facet);
                }
            }
        }*/
	}


	public List<AnnotatedResultItem> getPropertiesWithCount(Set<Filter> filters, String property) {
		String[] sparqls = sparqlEngine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparqls,endpoints,filters.isEmpty());
		if(!filters.isEmpty()){
			String[] propSparqls = sparqlEngine.propertiesSparql(endpoints, mainFilter, property);
			rdfEngine.annotatePropertiesWithEndpoints(propSparqls, endpoints, items);
		}
		
		String[] missingValSparqls = sparqlEngine.propertiesMissingValueSparql(endpoints, mainFilter, filters, property);
		AnnotatedResultItem item = rdfEngine.countResourcesMissingProperty(missingValSparqls, endpoints);
		if(item.getCount()>0){
			items.add(item);
		}
		
		return items;
	}
	
	public Collection<RdfResource> getResources(Set<Filter> filters, Set<String> properties, int limit){
		String sparql = sparqlEngine.resourcesSparql(endpoints, mainFilter, filters, limit);
		//use the first endpoint by default. doesn't matter as the query explicitly set the SERVICE for each triple pattern
		Set<String> uris = rdfEngine.getResources(sparql, endpoints[0]);
		String detailsSparql = sparqlEngine.resourcesDetailsSparql(uris, properties);
		return rdfEngine.getRdfResources(detailsSparql, endpoints);
	}

	public long getResourcesCount(Set<Filter> filters){
		String sparql = sparqlEngine.resourcesCountSparql(endpoints, mainFilter, filters);
		return rdfEngine.getResourcesCount(sparql, endpoints);
	}
}
