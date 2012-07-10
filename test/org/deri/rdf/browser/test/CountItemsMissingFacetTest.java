package org.deri.rdf.browser.test;

import static org.testng.Assert.assertEquals;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CountItemsMissingFacetTest {


	//fixture
	MainFilter mainFilter = new MainFilter("s", "a <http://xmlns.com/foaf/0.1/Person> .");
	SparqlEngine engine;
	
	@BeforeClass
	public void init(){
		engine = new SparqlEngine();
	}
	
	@Test
	public void noFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);
		String sparql = engine.countItemsMissingFacetSparql(mainFilter,facets,memberFacet);
		String expected = 
			"SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"OPTIONAL {?s <http://xmlns.com/foaf/0.1/member> ?org .} FILTER(!bound(?org)) ." +
			"} GROUP BY ?org";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void oneFacet(){
		//use a tree set so that order is predictable
		Set<Facet> facets = new TreeSet<Facet>(new Comparator<Facet>() {
			@Override
			public int compare(Facet o1, Facet o2) {
				//compare facets based on their triple patterns
				return o1.getFilter().getPattern().compareTo(o2.getFilter().getPattern());
			}
		});
		Facet memberFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org");
		memberFacet.addResourceValue("http://example.org/organisation/deri");
		memberFacet.addResourceValue("http://example.org/organisation/w3c");
		facets.add(memberFacet);
		
		Facet nickFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick");
		facets.add(nickFacet);
		
		String sparql = engine.countItemsMissingFacetSparql(mainFilter, facets, nickFacet);
		String expected = 
			"SELECT (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/w3c> .}" +
				"}" +
				"OPTIONAL {?s <http://xmlns.com/foaf/0.1/nick> ?nick .} FILTER(!bound(?nick)) ." +
			"} GROUP BY ?nick";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void towFacets(){
		//use a tree set so that order is predictable
		Set<Facet> facets = new TreeSet<Facet>(new Comparator<Facet>() {
			@Override
			public int compare(Facet o1, Facet o2) {
				//compare facets based on their triple patterns
				return o1.getFilter().getPattern().compareTo(o2.getFilter().getPattern());
			}
		});
		Facet memberFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org");
		facets.add(memberFacet);
		
		Facet nickFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick");
		nickFacet.addLiteralValue("cygri");
		facets.add(nickFacet);
		
		Facet hobbyFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://example.org/property/hobby>"), "hobby");
		hobbyFacet.addLiteralValue("chess");
		hobbyFacet.addLiteralValue("football");
		facets.add(hobbyFacet);
		
		
		String sparql = engine.countItemsMissingFacetSparql(mainFilter, facets, memberFacet);
		String expected = 
			"SELECT (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"chess\") .}" +
					"UNION" +
					"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\") .}" +
				"}" +
				"OPTIONAL {?s <http://xmlns.com/foaf/0.1/member> ?org .} FILTER(!bound(?org)) ." +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
				"}" +
			"} GROUP BY ?org";
		assertEquals(sparql, expected);
	}
}
