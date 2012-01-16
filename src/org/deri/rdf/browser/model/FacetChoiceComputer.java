package org.deri.rdf.browser.model;

import org.deri.rdf.browser.facet.RdfFacet;
import org.deri.rdf.browser.sparql.QueryEngine;

import com.google.common.collect.SetMultimap;

public class FacetChoiceComputer implements Runnable{

	private RdfFacet facet;
	private QueryEngine engine;
	private SetMultimap<String, String> filters;
	private String sparqlEndpoint;
	private String mainResourcesSelector;
	public FacetChoiceComputer(RdfFacet facet,String sparqlEndpoint, String mainResourcesSelector, QueryEngine engine,SetMultimap<String, String> filters){
		this.facet = facet;
		this.engine = engine;
		this.filters = filters;
		this.sparqlEndpoint = sparqlEndpoint;
		this.mainResourcesSelector = mainResourcesSelector;
	}
	
	@Override
	public void run() {
		facet.computeChoices(sparqlEndpoint, engine, mainResourcesSelector, filters);
	}

}
