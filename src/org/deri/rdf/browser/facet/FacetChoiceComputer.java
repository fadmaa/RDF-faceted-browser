package org.deri.rdf.browser.facet;

import org.deri.rdf.browser.BrowsingEngine;

public class FacetChoiceComputer implements Runnable{

	private RdfFacet facet;
	private BrowsingEngine engine; 
	
	public FacetChoiceComputer(RdfFacet f, BrowsingEngine engine){
		this.facet = f;
		this.engine = engine;
	}
	
	@Override
	public void run() {
		facet.setChoices(engine.getPropertiesWithCount(facet));
	}

}
