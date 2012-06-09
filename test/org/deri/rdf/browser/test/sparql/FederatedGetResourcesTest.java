package org.deri.rdf.browser.test.sparql;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FederatedGetResourcesTest {

	FederatedQueryEngine engine;
	String[] endpoints;
	String mainFilter;
	int limit = 10;
	
	@BeforeClass
	public void init(){
		engine = new FederatedQueryEngine();
		endpoints = new String[]{
				"http://localhost:3030/test/query",
				"http://localhost:3031/test2/query"
		};
		mainFilter = "a <http://xmlns.com/foaf/0.1/Person> .";
	};
	
	@Test
	public void noFilters(){
		Set<Filter> filters = new HashSet<Filter>();
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters,limit);
		String expectedSparql =
			"SELECT DISTINCT ?s "+ 
			"WHERE{" + 
				"{" + 
					"SERVICE <http://localhost:3030/test/query> {" + 
				    	"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
				    "}" + 
				"}" + 
				"UNION" + 
				"{" + 
					"SERVICE <http://localhost:3031/test2/query> {" + 
				    	"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
					"}" + 
				"}" +
			"} LIMIT 10";
		assertEquals(sparql, expectedSparql);
	}
	

	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		filters.add(hobbyF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> . " + 
						"SERVICE <http://localhost:3031/test2/query> {" + 
							"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\") . " + 
						"}" + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\")" + 
					"}" +
				"}" + 
		"} LIMIT 10";

		assertEquals(sparql, expectedSparql);
	}
}
