package org.deri.rdf.browser.model;

import org.deri.rdf.browser.BrowsingEngine;

public class FacetChoiceComputer implements Runnable{

	private Facet facet;
	private BrowsingEngine engine; 
	
	public FacetChoiceComputer(Facet f, BrowsingEngine engine){
		this.facet = f;
		this.engine = engine;
	}
	
	@Override
	public void run() {
		facet.setChoices(engine.getPropertiesWithCount(facet));
	}

}
