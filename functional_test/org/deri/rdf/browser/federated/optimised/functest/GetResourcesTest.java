package org.deri.rdf.browser.federated.optimised.functest;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.RdfEngine;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.OptimisedFederatedSparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetResourcesTest {
	RdfEngine rdfEngine;
	OptimisedFederatedSparqlEngine sparqlEngine;
	String endpoint = "http://localhost:3030/test/query";
	MainFilter mainFilter = new MainFilter("s", "a <http://xmlns.com/foaf/0.1/Person> .");
	int start = 0;
	int length = 10;
	String[] endpoints = new String[]{
		"http://localhost:3030/test/query",
		"http://localhost:3031/test/query"
	};
	
	@BeforeClass
	public void init(){
		sparqlEngine = new OptimisedFederatedSparqlEngine();
		rdfEngine = new RdfEngine();
	}
	
	@Test
	public void noFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		String sparql = sparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		Set<RdfDecoratedValue> resources = rdfEngine.getResources(sparql, endpoint, mainFilter.getVarname());
		Set<RdfDecoratedValue> expected = new HashSet<RdfDecoratedValue>();
		expected.add(new RdfDecoratedValue("http://example.org/person/fadi",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/gofran",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/alaa",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/phil",RdfDecoratedValue.RESOURCE));
		assertEquals(resources,expected);
	}

	@Test
	public void oneFacetOneValue(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3030/test/query");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3031/test/query");
		facets.add(memberFacet);
		String sparql = sparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		Set<RdfDecoratedValue> resources = rdfEngine.getResources(sparql, endpoint, mainFilter.getVarname());
		Set<RdfDecoratedValue> expected = new HashSet<RdfDecoratedValue>();
		expected.add(new RdfDecoratedValue("http://example.org/person/fadi",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/gofran",RdfDecoratedValue.RESOURCE));
		assertEquals(resources,expected);
	}
	
	@Test
	public void oneFacetTwoValue(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3030/test/query");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3031/test/query");
		memberFacet.addResourceValue("http://example.org/organisation/w3c","http://localhost:3030/test/query");
		memberFacet.addResourceValue("http://example.org/organisation/w3c","http://localhost:3031/test/query");
		facets.add(memberFacet);
		String sparql = sparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		Set<RdfDecoratedValue> resources = rdfEngine.getResources(sparql, endpoint, mainFilter.getVarname());
		Set<RdfDecoratedValue> expected = new HashSet<RdfDecoratedValue>();
		expected.add(new RdfDecoratedValue("http://example.org/person/fadi",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/gofran",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/alaa",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/phil",RdfDecoratedValue.RESOURCE));
		assertEquals(resources,expected);
	}
	
	@Test
	public void twoFacets(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3030/test/query");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3031/test/query");
		facets.add(memberFacet);
		Facet hobbyFacet = new Facet(new FacetFilter("<http://example.org/property/hobby>"), "hobby", "hobby");
		hobbyFacet.addLiteralValue("football","http://localhost:3031/test/query");
		facets.add(hobbyFacet);
		String sparql = sparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		Set<RdfDecoratedValue> resources = rdfEngine.getResources(sparql, endpoint, mainFilter.getVarname());
		Set<RdfDecoratedValue> expected = new HashSet<RdfDecoratedValue>();
		expected.add(new RdfDecoratedValue("http://example.org/person/fadi",RdfDecoratedValue.RESOURCE));
		assertEquals(resources,expected);
	}
	
	@Test
	public void twoFacetsMultipleValues(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3030/test/query");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3031/test/query");
		facets.add(memberFacet);
		Facet hobbyFacet = new Facet(new FacetFilter("<http://example.org/property/hobby>"), "hobby","hobby");
		hobbyFacet.addLiteralValue("football","http://localhost:3031/test/query");
		hobbyFacet.addLiteralValue("rugby","http://localhost:3030/test/query");
		facets.add(hobbyFacet);
		String sparql = sparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		Set<RdfDecoratedValue> resources = rdfEngine.getResources(sparql, endpoint, mainFilter.getVarname());
		Set<RdfDecoratedValue> expected = new HashSet<RdfDecoratedValue>();
		expected.add(new RdfDecoratedValue("http://example.org/person/fadi",RdfDecoratedValue.RESOURCE));
		expected.add(new RdfDecoratedValue("http://example.org/person/gofran",RdfDecoratedValue.RESOURCE));
		assertEquals(resources,expected);
	}
	
	@Test
	public void twoFacetsNoResults(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3030/test/query");
		memberFacet.addResourceValue("http://example.org/organisation/deri","http://localhost:3031/test/query");
		facets.add(memberFacet);
		Facet hobbyFacet = new Facet(new FacetFilter("<http://example.org/property/hobby>"), "hobby", "hobby");
		hobbyFacet.addLiteralValue("piano","http://localhost:3031/test/query");
		facets.add(hobbyFacet);
		String sparql = sparqlEngine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		Set<RdfDecoratedValue> resources = rdfEngine.getResources(sparql, endpoint, mainFilter.getVarname());
		Set<RdfDecoratedValue> expected = new HashSet<RdfDecoratedValue>();
		assertEquals(resources,expected);
	}
}
