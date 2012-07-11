package org.deri.rdf.browser.federated.test;

import static org.testng.Assert.assertEquals;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.NaiveFederatedSparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetFocusItemsTest {


	//fixture
	MainFilter mainFilter = new MainFilter("s", "a <http://xmlns.com/foaf/0.1/Person> .");
	int start = 0;
	int length = 10;
	String[] endpoints = new String[]{
		"http://localhost:3030/test/query",
		"http://localhost:3031/test/query"
	};
	NaiveFederatedSparqlEngine engine;
	
	@BeforeClass
	public void init(){
		engine = new NaiveFederatedSparqlEngine();
	}
	
	@Test
	public void noFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		String sparql = engine.getFocusItemsSparql(endpoints,mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
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
			"} ORDER BY ?s OFFSET 0 LIMIT 10";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void oneFacetOneValue(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		memberFacet.addResourceValue("http://example.org/organisation/deri");
		facets.add(memberFacet);
		String sparql = engine.getFocusItemsSparql(endpoints,mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
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
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
					"}" +
				"}" +
			"} ORDER BY ?s OFFSET 0 LIMIT 10";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void oneFacetTwoValues(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org");
		memberFacet.addResourceValue("http://example.org/organisation/deri");
		memberFacet.addResourceValue("http://example.org/organisation/w3c");
		facets.add(memberFacet);
		String sparql = engine.getFocusItemsSparql(endpoints,mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
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
			"} ORDER BY ?s OFFSET 0 LIMIT 10";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void twoFacets(){
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
		nickFacet.addLiteralValue("cygri");
		nickFacet.addLiteralValue("philA");
		facets.add(nickFacet);
		
		String sparql = engine.getFocusItemsSparql(endpoints,mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
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
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
						"UNION" +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"philA\") .}" +
					"}" +
				"}" +
				"UNION" +
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
						"UNION" +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"philA\") .}" +
					"}" +
				"}" +
			"} ORDER BY ?s OFFSET 0 LIMIT 10";
		assertEquals(sparql, expected);
	}
}

class FacetWithPredictableOrderValues extends Facet{

	public FacetWithPredictableOrderValues(FacetFilter f, String v) {
		super(f, v,"");
	}

	@Override
	public Set<RdfDecoratedValue> getSelections() {
		Set<RdfDecoratedValue> set = new TreeSet<RdfDecoratedValue>(new Comparator<RdfDecoratedValue>() {

			@Override
			public int compare(RdfDecoratedValue o1, RdfDecoratedValue o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		
		set.addAll(super.getSelections());
		return set;
	}
}
