package org.deri.rdf.browser.federated.optimised.test;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.OptimisedFederatedSparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GetFocusItemsTest {

	OptimisedFederatedSparqlEngine engine;
	MainFilter mainFilter = new MainFilter("s", "a <http://xmlns.com/foaf/0.1/Person> .");
	int start = 0;
	int length = 10;
	Set<Facet> facets;
	String[] endpoints = new String[]{
		"http://localhost:3030/test/query",
		"http://localhost:3031/test/query"
	};
	
	@BeforeClass
	public void init(){
		engine = new OptimisedFederatedSparqlEngine();
	};
	
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
		String sparql = engine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		String expectedSparql =
			"SELECT DISTINCT ?s "+ 
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
		assertEquals(sparql, expectedSparql);
	}
	

	@Test
	public void oneFilterOneEndpoint(){
		Facet hobbyF = new Facet(new FacetFilter("<http://example.org/property/hobby>"), "hobby", "hobby");
		hobbyF.addLiteralValue("football", "http://localhost:3031/test/query");
		facets.add(hobbyF);
		String sparql = engine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query>{" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"SERVICE <http://localhost:3031/test/query>{" + 
								"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " + 
							"}" +
						"}" + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). }" + 
					"}" +
				"}" + 
		"} ORDER BY ?s OFFSET 0 LIMIT 10";

		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void twoFiltersTwoEndpoint(){
		Facet memberF = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"),"organisation","org");
		memberF.addResourceValue("http://example.org/organisation/deri", "http://localhost:3031/test/query");
		memberF.addResourceValue("http://example.org/organisation/deri", "http://localhost:3030/test/query");
		facets.add(memberF);
		Facet hobbyF = new Facet(new FacetFilter("<http://example.org/property/hobby>"),"hobby","hobby");
		hobbyF.addLiteralValue("football","http://localhost:3031/test/query");
		facets.add(hobbyF);
		
		String sparql = engine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query>{" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" +
							"SERVICE <http://localhost:3031/test/query>{" +
								"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " +
							"}" +
						"}" +
						"{" +
				 			"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
				 		"}" + 
				 		"UNION" + 
				 		"{" + 
				 			"SERVICE <http://localhost:3031/test/query>{" + 
				 				"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
				 			"}" + 
				 		"}" +
				 	"}" +
				 "}" + 
				 "UNION" + 
				 "{" + 
				 	"SERVICE <http://localhost:3031/test/query>{" + 
				    	"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
				      	"{" + 
				      		"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " +
				      	"}" +
			      		"{" +
			      			"SERVICE <http://localhost:3030/test/query>{" +
			      				"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>."+
			      			"}" + 
			      		"}" +
			      		"UNION" +
			      		"{" + 
		      				"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
		      			"}" + 
			      	"}" +
			    "}" + 
			"} ORDER BY ?s OFFSET 0 LIMIT 10"
			;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Facet hobbyF = new Facet(new FacetFilter("<http://example.org/property/hobby>"),"hobby","hobby");
		hobbyF.addLiteralValue("football","http://localhost:3031/test/query");
		hobbyF.addLiteralValue("rugby", "http://localhost:3030/test/query");
		facets.add(hobbyF);
		
		String sparql = engine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" + 
				"{"+
					"SERVICE <http://localhost:3030/test/query>{" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" + 
							"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"rugby\"). " + 
						"}" + 
						"UNION" + 
						"{" + 
							"SERVICE <http://localhost:3031/test/query>{" + 
								"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " + 
							"}" + 
						"}" + 
					"}" + 
				"}" + 
				"UNION" + 
				"{" + 
					"SERVICE <http://localhost:3031/test/query>{" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"SERVICE <http://localhost:3030/test/query>{"+
								"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"rugby\"). " +  
							"}" + 
						"}" + 
						"UNION" +
						"{" +
							"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " +
						"}" + 
					"}" + 
				"}" + 
			"} ORDER BY ?s OFFSET 0 LIMIT 10"
			;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void twoFiltersTwoValues(){
		Facet hobbyF = new Facet(new FacetFilter("<http://example.org/property/hobby>"),"hobby","hobby");
		hobbyF.addLiteralValue("football", "http://localhost:3031/test/query");
		hobbyF.addLiteralValue("rugby", "http://localhost:3030/test/query");
		facets.add(hobbyF);
		Facet memberF = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"),"organisation","org");
		memberF.addResourceValue("http://example.org/organisation/deri", "http://localhost:3031/test/query");
		memberF.addResourceValue("http://example.org/organisation/deri", "http://localhost:3030/test/query");
		facets.add(memberF);
		
		String sparql = engine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" + 
				"{"+
					"SERVICE <http://localhost:3030/test/query>{" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" + 
							"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"rugby\"). " + 
						"}" + 
						"UNION" + 
						"{" + 
							"SERVICE <http://localhost:3031/test/query>{" + 
								"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " + 
							"}" + 
						"}" + 
						"{" +
							"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
						"}" + 
						"UNION" + 
						"{" + 
							"SERVICE <http://localhost:3031/test/query>{" + 
								"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
							"}" + 
						"}" +
					"}" + 
				"}" + 
				"UNION" + 
				"{" + 
					"SERVICE <http://localhost:3031/test/query>{" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"SERVICE <http://localhost:3030/test/query>{"+
								"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"rugby\"). " +  
							"}" + 
						"}" +
						"UNION" + 
						"{" +
							"?s <http://example.org/property/hobby> ?hobby. FILTER(str(?hobby)=\"football\"). " +
						"}" + 
						"{" +
							"SERVICE <http://localhost:3030/test/query>{" +
								"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>."+
							"}" + 
						"}" +
						"UNION" + 
						"{" + 
							"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
						"}" + 
					"}" + 
				"}" + 
			"} ORDER BY ?s OFFSET 0 LIMIT 10"
			;
		assertEquals(sparql, expectedSparql);
	}

	@Test
	public void missingValue(){
		Facet nickF = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"),"nick","nick");
		nickF.setMissingValueSelected(true);
		facets.add(nickF);
		String sparql = engine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query>{" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"OPTIONAL {" +
							"{" +
								"?s <http://xmlns.com/foaf/0.1/nick> ?nick. " +
							"}" +
							"UNION" +
							"{" +
								"SERVICE <http://localhost:3031/test/query>{" + 
									"?s <http://xmlns.com/foaf/0.1/nick> ?nick. " + 
								"}" +
							"}" +
						"} FILTER (!bound(?nick)) ." + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test/query>{" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"OPTIONAL {" +
							"{" +
								"SERVICE <http://localhost:3030/test/query>{" + 
									"?s <http://xmlns.com/foaf/0.1/nick> ?nick. " + 
								"}" +
							"}" +
							"UNION" +
							"{" +
								"?s <http://xmlns.com/foaf/0.1/nick> ?nick. " +
							"}" +
						"} FILTER (!bound(?nick)) ." + 
					"}" +
				"}" + 
		"} ORDER BY ?s OFFSET 0 LIMIT 10";

		assertEquals(sparql, expectedSparql);
	}
	
}

