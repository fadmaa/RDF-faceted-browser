package org.deri.rdf.browser.federated.tradedoff.test;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.TradedoffFederatedSparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CountItemsMissingFacetTest {
	//fixture
	MainFilter mainFilter = new MainFilter("s", "s", "a <http://xmlns.com/foaf/0.1/Person> .");
	TradedoffFederatedSparqlEngine engine;
	String[] endpoints = new String[]{
		"http://localhost:3030/test/query",
		"http://localhost:3031/test/query"
	};
	
	@BeforeClass
	public void init(){
		engine = new TradedoffFederatedSparqlEngine();
	}
	
	@Test
	public void noFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);
		String[] sparqls = engine.countItemsMissingFacetSparql(endpoints, mainFilter,facets,memberFacet);
		String[] expectedSparqls = new String[]{ 
			"SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE{" +
				"SERVICE <http://localhost:3030/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
				"OPTIONAL {" +
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
				"} FILTER(!bound(?org)) ." +
			"}"
			,
			"SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE{" +
				"SERVICE <http://localhost:3031/test/query>{" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
				"}" +
				"OPTIONAL {" +
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
				"} FILTER(!bound(?org)) ." +
			"}"
		};
		assertEquals(sparqls, expectedSparqls);
	}
}
