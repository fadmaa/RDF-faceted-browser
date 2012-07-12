package org.deri.rdf.browser.test;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.SparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RefocusTest {

	//fixture
	MainFilter mainFilter = new MainFilter("s", "a <http://xmlns.com/foaf/0.1/Person> .");
	SparqlEngine engine;
	int start = 0;
	int length = 10;
	
	@BeforeClass
	public void init(){
		engine = new SparqlEngine();
	}
	
	@Test
	public void noFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);
		MainFilter newMainFilter = engine.refocusSaprql(mainFilter, facets, memberFacet);
		assertEquals(newMainFilter.getSparqlPattern(),"?org ^(<http://xmlns.com/foaf/0.1/member>)?s. ?s a <http://xmlns.com/foaf/0.1/Person> .");
		
		String sparql = engine.getFocusItemsSparql(newMainFilter, facets, start, length);
		String expected = "SELECT DISTINCT ?org WHERE{" +
				"?org ^(<http://xmlns.com/foaf/0.1/member>)?s. ?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"} ORDER BY ?org OFFSET 0 LIMIT 10";
		assertEquals(sparql, expected);
	}
}
