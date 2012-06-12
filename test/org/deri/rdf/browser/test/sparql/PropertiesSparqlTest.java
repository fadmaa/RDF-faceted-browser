package org.deri.rdf.browser.test.sparql;

import static org.testng.Assert.assertEquals;

import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PropertiesSparqlTest {

	FederatedQueryEngine engine;
	String[] endpoints;
	String mainFilter;
	
	@BeforeClass
	public void init(){
		engine = new FederatedQueryEngine();
		endpoints = new String[]{
				"http://localhost:3030/test/query",
				"http://localhost:3031/test2/query"
		};
		mainFilter = "a <http://xmlns.com/foaf/0.1/Person> .";
	}
	
	@Test
	public void noFilters(){
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesSparql(endpoints, mainFilter, property);
		String[] expectedSparqls = new String[] {
			"SELECT DISTINCT ?v " +
			"WHERE{" +
				"SERVICE <http://localhost:3030/test/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
   				"}" +
			"}" ,
			
			"SELECT DISTINCT ?v " +
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" +
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
				"}" +
			"}"
		};
		assertEquals(sparqls, expectedSparqls);
	}
	
}
