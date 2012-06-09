package org.deri.rdf.browser.model;

import org.deri.rdf.browser.facet.RdfDecoratedValue;
import org.deri.rdf.browser.facet.RdfFacet;
import org.deri.rdf.browser.sparql.QueryEngine;

import com.google.common.collect.SetMultimap;

public class FacetChoiceComputer implements Runnable{

	private RdfFacet facet;
	private QueryEngine engine;
	private SetMultimap<RdfFacet, RdfDecoratedValue> filters;
	private String[] sparqlEndpoints;
	private String graphUri;
	private String mainResourcesSelector;
	public FacetChoiceComputer(RdfFacet facet,String[] sparqlEndpoints, String graphUri, String mainResourcesSelector, QueryEngine engine,SetMultimap<RdfFacet, RdfDecoratedValue> filters){
		this.facet = facet;
		this.engine = engine;
		this.filters = filters;
		this.sparqlEndpoints = sparqlEndpoints;
		this.mainResourcesSelector = mainResourcesSelector;
		this.graphUri = graphUri;
	}
	
	@Override
	public void run() {
		facet.computeChoices(sparqlEndpoints, graphUri, engine, mainResourcesSelector, filters);
	}

}
