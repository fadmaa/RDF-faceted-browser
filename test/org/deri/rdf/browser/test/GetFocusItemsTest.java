package org.deri.rdf.browser.test;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GetFocusItemsTest {

	//fixture
	MainFilter mainFilter = new MainFilter("s", "s", "a <http://xmlns.com/foaf/0.1/Person> .");
	int start = 0;
	int length = 10;
	SparqlEngine engine;
	
	@BeforeClass
	public void init(){
		engine = new SparqlEngine();
	}
	
	@Test
	public void noFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		String sparql = engine.getFocusItemsSparql(mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
			"} ORDER BY ?s OFFSET 0 LIMIT 10";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void oneFacetOneValue(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		memberFacet.addResourceValue("http://example.org/organisation/deri");
		facets.add(memberFacet);
		String sparql = engine.getFocusItemsSparql(mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
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
		String sparql = engine.getFocusItemsSparql(mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/w3c> .}" +
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
		nickFacet.addLiteralValue("timbl");
		facets.add(nickFacet);
		
		String sparql = engine.getFocusItemsSparql(mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/w3c> .}" +
				"}" +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"timbl\") .}" +
				"}" +
			"} ORDER BY ?s OFFSET 0 LIMIT 10";
		assertEquals(sparql, expected);
	}
	
	@Test
	public void threeFacets(){
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
		nickFacet.addLiteralValue("timbl");
		facets.add(nickFacet);
		
		Facet hobbyFacet = new FacetWithPredictableOrderValues(new FacetFilter("<http://example.org/property/hobby>"), "hobby");
		hobbyFacet.addLiteralValue("chess");
		facets.add(hobbyFacet);
		
		String sparql = engine.getFocusItemsSparql(mainFilter, facets, start,length);
		String expected = 
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"{" +
					"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"chess\") .}" +
				"}" +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri> .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/w3c> .}" +
				"}" +
				"{" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"cygri\") .}" +
					"UNION" +
					"{?s <http://xmlns.com/foaf/0.1/nick> ?nick. FILTER(str(?nick)=\"timbl\") .}" +
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
