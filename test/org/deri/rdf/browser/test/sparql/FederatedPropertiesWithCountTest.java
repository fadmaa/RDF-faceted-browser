package org.deri.rdf.browser.test.sparql;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FederatedPropertiesWithCountTest {

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
	};
	
	@Test
	public void noFilters(){
		Set<Filter> filters = new HashSet<Filter>();
		String property = "http://xmlns.com/foaf/0.1/member";
		String sparql = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String expectedSparql =
			"SELECT ?v (COUNT(?v) AS ?v_count) " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
    				"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
					"}" +
				"}" +
			"} GROUP BY ?v";
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		String sparql = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String expectedSparql =
			"SELECT ?v (COUNT(?v) AS ?v_count) " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
						"{SERVICE <http://localhost:3031/test2/query> {" +
					        "{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). }" + 
					      "}}" +
    				"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
						"{{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). }}" +
					"}" +
				"}" +
			"} GROUP BY ?v";
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterTwoEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",false);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",false);
		filters.add(memberF);
		String property = "http://example.org/property/hobby";
		String sparql = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String expectedSparql =
			"SELECT ?v (COUNT(?v) AS ?v_count) " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://example.org/property/hobby> ?v . " +
	        			"{" +
	        				"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>.}" +
	        			"}" +
	        			"UNION" +
	        			"{" +
	        				"SERVICE <http://localhost:3031/test2/query> {" +
	        					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>.}" +
	        				"}" +
						"}" +
    				"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://example.org/property/hobby> ?v . " +
        				"{" +
        					"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>.}" +
        				"}" +
        				"UNION" +
        				"{" +
        					"SERVICE <http://localhost:3030/test/query> {" +
        						"{?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>.}" +
        					"}" +
						"}" +
					"}" +
				"}" +
			"} GROUP BY ?v";
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		hobbyF.addValue("http://localhost:3030/test/query","rugby");
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		String sparql = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String expectedSparql =
			"SELECT ?v (COUNT(?v) AS ?v_count) " +
			"WHERE{" +
				"{" +
			    	"SERVICE <http://localhost:3030/test/query> {" + 	
			    		"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " + 
		    			"{" +
			    				"{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"rugby\"). }" +  
		    			"}" +
		    			"UNION" + 
		    			"{" +
			    			"SERVICE <http://localhost:3031/test2/query> {" + 
			    				"{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). }" + 
		    				"}" + 
		    			"}" + 
			    	"}" + 
			    "}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . "+
						"{" + 
							"{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). }" +  
						"}" + 
						"UNION" + 
						"{" +
							"SERVICE <http://localhost:3030/test/query> {" + 
								"{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"rugby\"). }" + 
							"}" + 
						"}" + 
					"}" + 
				"}" +
			"} GROUP BY ?v";
		assertEquals(sparql, expectedSparql);
	}
}
