package org.deri.rdf.browser.federated.enhanced.test;

import static org.testng.Assert.assertEquals;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.EnhancedFederatedSparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetFacetValusTest {

	//fixture
	MainFilter mainFilter = new MainFilter("s", "s", "a <http://xmlns.com/foaf/0.1/Person> .");
	EnhancedFederatedSparqlEngine engine;
	Set<Facet> facets;
	String[] endpoints = new String[] {
			"http://localhost:3030/test/query",
			"http://localhost:3031/test/query"
	};
	
	
	@BeforeClass
	public void init(){
		engine = new EnhancedFederatedSparqlEngine();
	}
	
	@BeforeMethod
	public void setUp(){
		//use a tree set so that order is predictable
		facets = new TreeSet<Facet>(new Comparator<Facet>() {
			@Override
			public int compare(Facet o1, Facet o2) {
				//compare facets based on their triple patterns
				return o1.getFilter().getPattern().compareTo(o2.getFilter().getPattern());
			}
		});
	}
	
	@Test
	public void noFilters(){
		Facet memberF = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"),"org","organisation");
		facets.add(memberF);
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, memberF);
		String expectedSparql = 
			"SELECT ?org (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{"+
			"{"+
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
			"}" +
			"{" +
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s <http://xmlns.com/foaf/0.1/member> ?org ." +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s <http://xmlns.com/foaf/0.1/member> ?org ." +
				"}" +
			"}" +
			"} GROUP BY ?org" 
		;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterOneEndpoint(){
		Facet hobbyF = new Facet(new FacetFilter("<http://example.org/property/hobby>"),"hobby","hobby");
		hobbyF.addLiteralValue("football", "http://localhost:3031/test/query");
		facets.add(hobbyF);
		Facet memberF = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"),"org","organisation");
		facets.add(memberF);
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, memberF);
		String expectedSparql = 
			"SELECT ?org (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"{"+
					"SERVICE <http://localhost:3030/test/query>{" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
					"}" +
				"}" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " +
					"}" +
				"}" +
				"{" +
					"SERVICE <http://localhost:3030/test/query>{" +
						"?s <http://xmlns.com/foaf/0.1/member> ?org ." +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"?s <http://xmlns.com/foaf/0.1/member> ?org ." +
					"}" +
				"}" +
			"} GROUP BY ?org" 

			;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterTwoEndpoint(){
		Facet memberF = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"),"org","organisation");
		memberF.addResourceValue("http://example.org/organisation/deri","http://localhost:3031/test/query");
		memberF.addResourceValue("http://example.org/organisation/deri","http://localhost:3030/test/query");
		facets.add(memberF);
		Facet hobbyF = new Facet(new FacetFilter("<http://example.org/property/hobby>"),"hobby","hobby");
		facets.add(hobbyF);
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, hobbyF);
		String expectedSparql = 
			"SELECT ?hobby (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
			"{"+
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
			"}" +
			"{" +
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s <http://example.org/property/hobby> ?hobby ." +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s <http://example.org/property/hobby> ?hobby ." +
				"}" +
			"}" +
			"{" +
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>. " +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>. " +
				"}" +
			"}" +
			"} GROUP BY ?hobby" 
		;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Facet hobbyF = new Facet(new FacetFilter("<http://example.org/property/hobby>"),"hobby","hobby");
		hobbyF.addLiteralValue("football","http://localhost:3031/test/query");
		hobbyF.addLiteralValue("rugby","http://localhost:3030/test/query");
		facets.add(hobbyF);
		Facet memberF = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"),"org","organisation");
		facets.add(memberF);
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, memberF);
		String expectedSparql = 
			"SELECT ?org (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
			"{"+
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
			"}" +
			"{" +
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"rugby\"). " +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " +
				"}" +
			"}" +  
			"{" +
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s <http://xmlns.com/foaf/0.1/member> ?org ." +
				"}" +
			"}" +
			"UNION" +
			"{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s <http://xmlns.com/foaf/0.1/member> ?org ." +
				"}" +
			"}" +
			"} GROUP BY ?org" 
		;
		assertEquals(sparql, expectedSparql);
	}

}
