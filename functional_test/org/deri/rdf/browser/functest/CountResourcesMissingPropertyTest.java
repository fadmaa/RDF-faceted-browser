package org.deri.rdf.browser.functest;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.RdfEngine;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CountResourcesMissingPropertyTest {

	RdfEngine rdfEngine;
	SparqlEngine sparqlEngine;
	String endpoint = "http://localhost:3030/test/query";
	MainFilter mainFilter = new MainFilter("s", "a <http://xmlns.com/foaf/0.1/Person> .");

	@BeforeClass
	public void init(){
		sparqlEngine = new SparqlEngine();
		rdfEngine = new RdfEngine();
	}
	
	@Test
	public void noFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet nickFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick", "nick");
		facets.add(nickFacet);//this is important as the method expects the focusfacet to be in facets
		String sparql = sparqlEngine.countItemsMissingFacetSparql(mainFilter, facets, nickFacet);
		AnnotatedResultItem res = rdfEngine.countResourcesMissingProperty(sparql, endpoint);
		assertEquals(res.getCount()	,2);
		assertEquals(res.getValue().getType(), RdfDecoratedValue.NULL);
	}
	
	@Test
	public void noFilterZeroResult(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet nickFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(nickFacet);//this is important as the method expects the focusfacet to be in facets
		String sparql = sparqlEngine.countItemsMissingFacetSparql(mainFilter, facets, nickFacet);
		AnnotatedResultItem res = rdfEngine.countResourcesMissingProperty(sparql, endpoint);
		assertEquals(res.getCount()	,0);
		assertEquals(res.getValue().getType(), RdfDecoratedValue.NULL);
	}
	
	@Test
	public void oneFacet(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet nickFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick", "nick");
		facets.add(nickFacet);//this is important as the method expects the focusfacet to be in facets
		
		Facet hobbyFacet = new Facet(new FacetFilter("<http://example.org/property/hobby>"), "hobby", "hobby");
		hobbyFacet.addLiteralValue("football");
		facets.add(hobbyFacet);
		
		String sparql = sparqlEngine.countItemsMissingFacetSparql(mainFilter, facets, nickFacet);
		AnnotatedResultItem res = rdfEngine.countResourcesMissingProperty(sparql, endpoint);
		assertEquals(res.getCount()	,1);
		assertEquals(res.getValue().getType(), RdfDecoratedValue.NULL);
	}
}
