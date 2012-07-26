package org.deri.rdf.browser.test;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GetFacetValuesTest {

	//fixture
	MainFilter mainFilter = new MainFilter("s", "s", "a <http://xmlns.com/foaf/0.1/Person> .");
	SparqlEngine engine;
	Set<Facet> facets;
	
	@BeforeClass
	public void init(){
		engine = new SparqlEngine();
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
		String sparql = engine.getFacetValuesSparql(mainFilter,facets,memberFacet);
		String expected = 
			"SELECT ?org (COUNT(DISTINCT ?s) AS ?count) WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{?s <http://xmlns.com/foaf/0.1/member> ?org .}" +
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
		
		String sparql = engine.getFacetValuesSparql(mainFilter, facets, nickFacet);
		String expected = 
			"SELECT ?nick (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/w3c> .}" +
				"}" +
				"{?s <http://xmlns.com/foaf/0.1/nick> ?nick .}" +
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
		
		
		String sparql = engine.getFacetValuesSparql(mainFilter, facets, memberFacet);
		String expected = 
			"SELECT ?org (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"chess\") .}" +
					"UNION" +
					"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\") .}" +
				"}" +
				"{?s <http://xmlns.com/foaf/0.1/member> ?org .}" +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
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
		
		
		String sparql = engine.getFacetValuesSparql(mainFilter, facets, hobbyFacet);
		String expected = 
			"SELECT ?hobby (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{?s <http://example.org/property/hobby> ?hobby .}" +
				"OPTIONAL{" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick_v .}" +
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
		
		
		String sparql = engine.getFacetValuesSparql(mainFilter, facets, hobbyFacet);
		String expected = 
			"SELECT ?hobby (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{?s <http://example.org/property/hobby> ?hobby .}" +
				"OPTIONAL{" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"sheer\") .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick_v .}" +
				"} FILTER(!bound(?nick_v)) ." +
			"} GROUP BY ?hobby";
		assertEquals(sparql, expected);
	}

}
