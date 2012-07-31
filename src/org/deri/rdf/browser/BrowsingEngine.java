package org.deri.rdf.browser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetChoiceComputer;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.browser.sparql.NaiveFederatedSparqlEngine;
import org.deri.rdf.browser.sparql.EnhancedFederatedSparqlEngine;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.deri.rdf.browser.util.ParsingUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public class BrowsingEngine{

	private SparqlEngine sparqlEngine;
	private NaiveFederatedSparqlEngine naiveFedSparqlEngine;
	private EnhancedFederatedSparqlEngine optimisedFedSparqlEngine;
	private RdfEngine rdfEngine;
	private List<Facet> facets;
	
	private MainFilter mainFilter;
	private String[] endpoints;
	protected String template;
	protected Set<String> properties;
	private int mode;
	
	public BrowsingEngine(EnhancedFederatedSparqlEngine optFedSparqlEngine, NaiveFederatedSparqlEngine fedSparqlEngine, SparqlEngine sparqlEngine, RdfEngine rdfEngine) {
		this.sparqlEngine = sparqlEngine;
		this.naiveFedSparqlEngine = fedSparqlEngine;
		this.optimisedFedSparqlEngine = optFedSparqlEngine;
		this.rdfEngine = rdfEngine;
		facets = new LinkedList<Facet>();
	}

	public void initializeFromJSON(JSONObject o) throws JSONException, XPathExpressionException {
        if (o == null) {
            return;
        }
        if(o.has("mode")){
        	if(o.getString("mode").equals("naive-federated")){
        		mode = NAIVE_FEDERATED_MODE;
        	}else if(o.getString("mode").equals("optimised-federated")){
        		mode = OPTIMISED_FEDERATED_MODE;
        	}
        	endpoints = o.getString("endpoint").split("\\r?\\n");
        }else{
        	mode = NORMAL_MODE;
        	endpoints = new String[] {o.getString("endpoint").trim()};
        }
        JSONObject f= o.getJSONObject("main_selector");
        String fvarname = f.getString("varname");
        String facetsVarname;
        if(f.has("facetsVarname")){
        	facetsVarname = f.getString("facetsVarname");
        }else{
        	facetsVarname = fvarname;
        }
        mainFilter = new MainFilter(fvarname, facetsVarname, f.getString("pattern")); 
        template = o.getString("template");
        properties = ParsingUtilities.getProperties(template);
        
        if (o.has("facets") && !o.isNull("facets")) {
            JSONArray a = o.getJSONArray("facets");
            int length = a.length();
            
            for (int i = 0; i < length; i++) {
                JSONObject fo = a.getJSONObject(i);
                Facet facet = new Facet();
                
                if (facet != null) {
                    facet.initializeFromJSON(fo,mode==OPTIMISED_FEDERATED_MODE);
                    facets.add(facet);
                }
            }
        }
	}


	public long getResourcesCount(){
		String sparql = "";
		if(mode==NORMAL_MODE){
			sparql = sparqlEngine.countFocusItemsSparql(mainFilter, facets);
		}else if(mode==NAIVE_FEDERATED_MODE){
			sparql = naiveFedSparqlEngine.countFocusItemsSparql(endpoints, mainFilter, facets);
		}else if(mode==OPTIMISED_FEDERATED_MODE){
			sparql = optimisedFedSparqlEngine.countFocusItemsSparql(endpoints, mainFilter, facets);
		}
		return rdfEngine.getResourcesCount(sparql, endpoints[0]);
	}
	
	public Collection<RdfResource> getResources(int offset, int limit){
		String sparql = "";
		if(mode==NORMAL_MODE){
			sparql = sparqlEngine.getFocusItemsSparql(mainFilter, facets, offset, limit);
		}else if(mode==NAIVE_FEDERATED_MODE){
			sparql = naiveFedSparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, offset, limit);
		}else if(mode==OPTIMISED_FEDERATED_MODE){
			sparql = optimisedFedSparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, offset, limit);
		}
		Set<RdfDecoratedValue> values = rdfEngine.getResources(sparql, endpoints[0], mainFilter.getVarname());
		
		Set<String> uris = new HashSet<String>();
		Set<RdfResource> literalResources = new HashSet<RdfResource>(); 
		for(RdfDecoratedValue rd:values){
			if(rd.isLiteral()){
				literalResources.add(new RdfResource(rd));
			}else{
				uris.add(rd.getValue());
			}
		}
		if(uris.isEmpty()){
			return literalResources;
		}
		String detailsSparql = "";
		if(mode==NORMAL_MODE){
			detailsSparql = sparqlEngine.resourcesDetailsSparql(uris, properties);
		}else if(mode==NAIVE_FEDERATED_MODE){
			detailsSparql = naiveFedSparqlEngine.resourcesDetailsSparql(endpoints,uris, properties);
		}else if(mode==OPTIMISED_FEDERATED_MODE){
			detailsSparql = optimisedFedSparqlEngine.resourcesDetailsSparql(endpoints,uris, properties);
		}
		
		Collection<RdfResource> resources = rdfEngine.getRdfResources(detailsSparql, endpoints[0]);
		resources.addAll(literalResources);
		return resources;
	}
	
	public List<AnnotatedResultItem> getPropertiesWithCount(Facet focusFacet) {
		String sparql = "", missingValSparql = "";
		if(mode==NORMAL_MODE){
			sparql = sparqlEngine.getFacetValuesSparql(mainFilter, facets, focusFacet);
			missingValSparql = sparqlEngine.countItemsMissingFacetSparql(mainFilter, facets, focusFacet);
		}else if(mode==NAIVE_FEDERATED_MODE){
			sparql = naiveFedSparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, focusFacet);
			missingValSparql = naiveFedSparqlEngine.countItemsMissingFacetSparql(endpoints, mainFilter, facets, focusFacet);
		}else if(mode==OPTIMISED_FEDERATED_MODE){
			sparql = optimisedFedSparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, focusFacet);
			missingValSparql = optimisedFedSparqlEngine.countItemsMissingFacetSparql(endpoints, mainFilter, facets, focusFacet);
		}
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparql, endpoints[0], focusFacet.getVarname());
		
		//for optimised federated browsing, annotate
		if(mode==OPTIMISED_FEDERATED_MODE){
			String[] propSparqls = optimisedFedSparqlEngine.annotateValuesSparql(endpoints, mainFilter, focusFacet);
			rdfEngine.annotatePropertiesWithEndpoints(propSparqls, endpoints, items, focusFacet);
		}
		AnnotatedResultItem item = rdfEngine.countResourcesMissingProperty(missingValSparql, endpoints[0]);
		if(item.getCount()>0){
			items.add(item);
		}
		
		return items;
	}
	
	public MainFilter refocus(Facet refocusFacet){
		if(mode !=NORMAL_MODE){
			throw new UnsupportedOperationException("Can't refocus in federated mode");
		}
		return sparqlEngine.refocusSaprql(mainFilter, facets, refocusFacet);
	}
	
	public void write(JSONWriter writer) throws JSONException {

		writer.object();
		writer.key("endpoint");
		writer.value(StringUtils.join(endpoints, "\n"));

		writer.key("main_selector");
		mainFilter.write(writer);
		
		writer.key("mode");
		writer.value(mode);
		
		writer.key("facets");
		writer.array();
		for (Facet facet : facets) {
			facet.write(writer);
		}
		writer.endArray();
		writer.endObject();
	}
	
	public void computeFacets(){
		List<Thread> threads = new ArrayList<Thread>(facets.size());
		for(Facet f:facets){
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
	
	private static final int NORMAL_MODE = 0;
	private static final int NAIVE_FEDERATED_MODE = 1;
	private static final int OPTIMISED_FEDERATED_MODE = 2;
	
}
