package org.deri.rdf.browser.test.sparql;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.model.RdfDecoratedValue;
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
	}
	
	@Test
	public void noFilters(){
		Set<Filter> filters = new HashSet<Filter>();
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String[] expectedSparqls = new String[] {
			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3030/test/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
   				"}" +
			"} GROUP BY ?v" ,
			
			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" +
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
				"}" +
			"} GROUP BY ?v"
		};
		assertEquals(sparqls, expectedSparqls);
	}
	
	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String[] expectedSparqls = new String[] {
			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3030/test/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
					"{SERVICE <http://localhost:3031/test2/query> {" +
				        "?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"football\"). " + 
				      "}}" +
   				"}" +
   			"} GROUP BY ?v",
				
   			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" +
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " +
					"{?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"football\"). }" +
				"}" +
			"} GROUP BY ?v"
		};
		assertEquals(sparqls, expectedSparqls);
	}
	
	@Test
	public void oneFilterTwoEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String property = "http://example.org/property/hobby";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String[] expectedSparqls = new String[] {
			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3030/test/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://example.org/property/hobby> ?v . " +
        			"{" +
	       				"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." +
	       			"}" +
	       			"UNION" +
	       			"{" +
        				"SERVICE <http://localhost:3031/test2/query> {" +
        					"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." +
        				"}" +
					"}" +
   				"}" +
			"} GROUP BY ?v",
				
			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" +
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://example.org/property/hobby> ?v . " +
        			"{" +
        				"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." +
        			"}" +
        			"UNION" +
        			"{" +
        				"SERVICE <http://localhost:3030/test/query> {" +
        					"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." +
        				"}" +
					"}" +
				"}" +
			"} GROUP BY ?v"
		};
		assertEquals(sparqls, expectedSparqls);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		hobbyF.addValue("http://localhost:3030/test/query","rugby",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		String[] expectedSparqls = new String[] {
			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
		    	"SERVICE <http://localhost:3030/test/query> {" + 	
		    		"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . " + 
	    			"{" +
		    				"?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"rugby\"). " +  
	    			"}" +
	    			"UNION" + 
	    			"{" +
		    			"SERVICE <http://localhost:3031/test2/query> {" + 
		    				"?s <http://example.org/property/hobby> ?rv2. FILTER(str(?rv2)=\"football\"). " + 
	    				"}" + 
	    			"}" + 
			    "}" +
			"} GROUP BY ?v" , 
			
			"SELECT ?v (COUNT(?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" +
					"?s a <http://xmlns.com/foaf/0.1/Person> . ?s <http://xmlns.com/foaf/0.1/member> ?v . "+
					"{" + 
						"?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"football\"). " +  
					"}" + 
					"UNION" + 
					"{" +
						"SERVICE <http://localhost:3030/test/query> {" + 
							"?s <http://example.org/property/hobby> ?rv2. FILTER(str(?rv2)=\"rugby\"). " + 
						"}" + 
					"}" + 
				"}" + 
			"} GROUP BY ?v"
		};
		assertEquals(sparqls, expectedSparqls);
	}
}
