package org.deri.rdf.browser.federated.test;

import static org.testng.Assert.assertEquals;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.NaiveFederatedSparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetFacetValuesTest {

	//fixture
	MainFilter mainFilter = new MainFilter("s", "s", "a <http://xmlns.com/foaf/0.1/Person> .");
	NaiveFederatedSparqlEngine engine;
	Set<Facet> facets;
	String[] endpoints = new String[] {
			"http://localhost:3030/test/query",
			"http://localhost:3031/test/query"
	};
	
	@BeforeClass
	public void init(){
		engine = new NaiveFederatedSparqlEngine();
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
	public void noFilter(){
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);
		String sparql = engine.getFacetValuesSparql(endpoints,mainFilter,facets,memberFacet);
		String expected = 
			"SELECT ?org (COUNT(DISTINCT ?s) AS ?count) WHERE{" +
				"{" +
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
						"{?s <http://xmlns.com/foaf/0.1/member> ?org .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/member> ?org .}" +
					"}" +
				"}" +
			"} GROUP BY ?org";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void oneFacet(){
		Facet memberFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org");
		memberFacet.addResourceValue("http://example.org/organisation/deri");
		memberFacet.addResourceValue("http://example.org/organisation/w3c");
		facets.add(memberFacet);
		
		Facet nickFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick");
		facets.add(nickFacet);
		
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, nickFacet);
		String expected = 
			"SELECT ?nick (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"{" +
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
						"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
						"UNION" +
						"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/w3c> .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
						"UNION" +
						"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/w3c> .}" +
					"}" +
				"}" +
				"{" +
					"SERVICE <http://localhost:3030/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick .}" +
					"}" +
				"}" +
			"} GROUP BY ?nick";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void towFacets(){
		Facet memberFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org");
		facets.add(memberFacet);
		
		Facet nickFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick");
		nickFacet.addLiteralValue("cygri");
		facets.add(nickFacet);
		
		Facet hobbyFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://example.org/property/hobby>"), "hobby");
		hobbyFacet.addLiteralValue("chess");
		hobbyFacet.addLiteralValue("football");
		facets.add(hobbyFacet);
		
		
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, memberFacet);
		String expected = 
			"SELECT ?org (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"{" +
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
						"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"chess\") .}" +
						"UNION" +
						"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\") .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"chess\") .}" +
						"UNION" +
						"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\") .}" +
					"}" +
				"}" +
				"{" +
					"SERVICE <http://localhost:3030/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/member> ?org .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/member> ?org .}" +
					"}" +
				"}" +
				"{" +
					"SERVICE <http://localhost:3030/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
					"}" +
				"}" +
			"} GROUP BY ?org";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void onlyMissingValue(){
		Facet nickFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick");
		nickFacet.setMissingValueSelected(true);
		facets.add(nickFacet);
		
		Facet hobbyFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://example.org/property/hobby>"), "hobby");
		facets.add(hobbyFacet);
		
		
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, hobbyFacet);
		String expected = 
			"SELECT ?hobby (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"{" +
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
						"{?s <http://example.org/property/hobby> ?hobby .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://example.org/property/hobby> ?hobby .}" +
					"}" +
				"}" +
				"OPTIONAL{" +
					"{" +
						"SERVICE <http://localhost:3030/test/query>{" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?nick_v .}" +
						"}" +
					"}" +
					"UNION" +
					"{" +
						"SERVICE <http://localhost:3031/test/query>{" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?nick_v .}" +
						"}" +
					"}" +
				"} FILTER(!bound(?nick_v)) ." +
			"} GROUP BY ?hobby";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void missingValueAndOther(){
		Facet nickFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick");
		nickFacet.setMissingValueSelected(true);
		nickFacet.addLiteralValue("sheer");
		facets.add(nickFacet);
		
		Facet hobbyFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://example.org/property/hobby>"), "hobby");
		facets.add(hobbyFacet);
		
		
		String sparql = engine.getFacetValuesSparql(endpoints, mainFilter, facets, hobbyFacet);
		String expected = 
			"SELECT ?hobby (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"{" +
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
						"{?s <http://example.org/property/hobby> ?hobby .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://example.org/property/hobby> ?hobby .}" +
					"}" +
				"}" +
				"OPTIONAL{" +
					"{" +
						"SERVICE <http://localhost:3030/test/query>{" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"sheer\") .}" +
							"UNION" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?nick_v .}" +
						"}" +
					"}" +
					"UNION" +
					"{" +
						"SERVICE <http://localhost:3031/test/query>{" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"sheer\") .}" +
							"UNION" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?nick_v .}" +
						"}" +
					"}" +
				"} FILTER(!bound(?nick_v)) ." +
			"} GROUP BY ?hobby";
		assertEquals(sparql, expected);
	}
	
}
