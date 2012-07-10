package org.deri.rdf.browser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetChoiceComputer;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.deri.rdf.browser.util.ParsingUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public class BrowsingEngine{

	private SparqlEngine sparqlEngine;
	private RdfEngine rdfEngine;
	private List<Facet> facets;
	
	private MainFilter mainFilter;
	private String endpoint;
	protected String template;
	protected Set<String> properties;
	
	public BrowsingEngine(SparqlEngine sparqlEngine, RdfEngine rdfEngine) {
		this.sparqlEngine = sparqlEngine;
		this.rdfEngine = rdfEngine;
		facets = new LinkedList<Facet>();
	}

	public void initializeFromJSON(JSONObject o) throws JSONException, XPathExpressionException {
        if (o == null) {
            return;
        }
        endpoint = o.getString("endpoint");
        JSONObject f= o.getJSONObject("main_selector");
        mainFilter = new MainFilter(f.getString("varname"), f.getString("pattern")); 
        template = o.getString("template");
        properties = ParsingUtilities.getProperties(template);
        
        if (o.has("facets") && !o.isNull("facets")) {
            JSONArray a = o.getJSONArray("facets");
            int length = a.length();
            
            for (int i = 0; i < length; i++) {
                JSONObject fo = a.getJSONObject(i);
                Facet facet = new Facet();
                
                if (facet != null) {
                    facet.initializeFromJSON(fo);
                    facets.add(facet);
                }
            }
        }
	}


	public long getResourcesCount(){
		String sparql = sparqlEngine.countFocusItemsSparql(mainFilter, facets);
		return rdfEngine.getResourcesCount(sparql, endpoint);
	}
	
	public Collection<RdfResource> getResources(int offset, int limit){
		String sparql = sparqlEngine.getFocusItemsSparql(mainFilter, facets, offset, limit);
		Set<RdfDecoratedValue> values = rdfEngine.getResources(sparql, endpoint, mainFilter.getVarname());
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
		String detailsSparql = sparqlEngine.resourcesDetailsSparql(uris, properties);
		Collection<RdfResource> resources = rdfEngine.getRdfResources(detailsSparql, endpoint);
		resources.addAll(literalResources);
		return resources;
	}
	
	public List<AnnotatedResultItem> getPropertiesWithCount(Facet focusFacet) {
		String sparql = sparqlEngine.getFacetValuesSparql(mainFilter, facets, focusFacet);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparql, endpoint, focusFacet.getVarname());
		
		String missingValSparql = sparqlEngine.countItemsMissingFacetSparql(mainFilter, facets, focusFacet);
		AnnotatedResultItem item = rdfEngine.countResourcesMissingProperty(missingValSparql, endpoint);
		if(item.getCount()>0){
			items.add(item);
		}
		
		return items;
	}
	
	public MainFilter refocus(Facet refocusFacet){
		return sparqlEngine.refocusSaprql(mainFilter, facets, refocusFacet);
	}
	
	public void write(JSONWriter writer) throws JSONException {

		writer.object();
		writer.key("endpoint");
		writer.value(endpoint);

		writer.key("main_selector");
		mainFilter.write(writer);
		
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
}
