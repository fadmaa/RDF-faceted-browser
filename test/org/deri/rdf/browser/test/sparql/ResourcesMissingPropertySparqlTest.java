package org.deri.rdf.browser.test.sparql;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResourcesMissingPropertySparqlTest {

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
		String[] sparqls = engine.propertiesMissingValueSparql(endpoints, mainFilter, filters, property);
		String[] expectedSparqls = new String[] {
			"SELECT (COUNT(DISTINCT ?s) AS ?count) " + 
			"WHERE{" +
				"SERVICE <http://localhost:3030/test/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
					"OPTIONAL {" + 
			        	"{?s <http://xmlns.com/foaf/0.1/member> ?v. }" + 
			        	"UNION" + 
			        	"{" + 
			        		"SERVICE <http://localhost:3031/test2/query>{" + 
			        			"?s <http://xmlns.com/foaf/0.1/member> ?v. " + 
			        		"}" + 
			        	"}" + 
			      	"}" + 
			      	"FILTER (!bound(?v))." + 
			    "}" + 
			"}" ,

			"SELECT (COUNT(DISTINCT ?s) AS ?count) " + 
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
					"OPTIONAL {" + 
						"{" + 
							"SERVICE <http://localhost:3030/test/query>{" + 
								"?s <http://xmlns.com/foaf/0.1/member> ?v. " + 
							"}" + 
						"}" +
						"UNION" +
			        	"{?s <http://xmlns.com/foaf/0.1/member> ?v. }" + 
			      	"}" + 
			      	"FILTER (!bound(?v))." + 
			    "}" + 
			"}" 
		};
		assertEquals(sparqls, expectedSparqls);
	}
	
	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter hobbyF = new ComparableFilter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesMissingValueSparql(endpoints, mainFilter, filters, property);
		String[] expectedSparqls = new String[] {
			"SELECT (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3030/test/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
					"OPTIONAL {" + 
		        		"{?s <http://xmlns.com/foaf/0.1/member> ?v. }" + 
		        		"UNION" + 
		        		"{" + 
		        			"SERVICE <http://localhost:3031/test2/query>{" + 
		        				"?s <http://xmlns.com/foaf/0.1/member> ?v. " + 
		        			"}" + 
		        		"}" + 
		        	"}" + 
		        	"FILTER (!bound(?v))." +
		        	"{" +
						"SERVICE <http://localhost:3031/test2/query> {" +
							"?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"football\"). " + 
						"}" +
					"}" +
   				"}" +
   			"}",
				
   			"SELECT (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
					"OPTIONAL {" + 
						"{" + 
							"SERVICE <http://localhost:3030/test/query>{" + 
								"?s <http://xmlns.com/foaf/0.1/member> ?v. " + 
							"}" + 
						"}" +
						"UNION" +
						"{" +
							"?s <http://xmlns.com/foaf/0.1/member> ?v. " +
						"}" + 
					"}" + 
					"FILTER (!bound(?v))." +
					"{?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"football\"). }" +
				"}" +
			"}"
		};
		assertEquals(sparqls, expectedSparqls);
	}
	
	@Test
	public void oneFilterTwoEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new ComparableFilter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String property = "http://example.org/property/hobby";
		String[] sparqls = engine.propertiesMissingValueSparql(endpoints, mainFilter, filters, property);
		String[] expectedSparqls = new String[] {
			"SELECT (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3030/test/query> {" + 
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
					"OPTIONAL {" + 
						"{?s <http://example.org/property/hobby> ?v. }" +
						"UNION" +
						"{" + 
							"SERVICE <http://localhost:3031/test2/query>{" + 
								"?s <http://example.org/property/hobby> ?v. " + 
							"}" + 
						"}" +
					"}" + 
					"FILTER (!bound(?v))." +
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
			"}",
				
			"SELECT (COUNT(DISTINCT ?s) AS ?count) " +
			"WHERE{" +
				"SERVICE <http://localhost:3031/test2/query> {" +
					"?s a <http://xmlns.com/foaf/0.1/Person> ." +
					"OPTIONAL {" + 
						"{" + 
							"SERVICE <http://localhost:3030/test/query>{" + 
								"?s <http://example.org/property/hobby> ?v. " + 
							"}" + 
						"}" +
						"UNION" +
						"{?s <http://example.org/property/hobby> ?v. }" + 
					"}" + 
					"FILTER (!bound(?v))." +
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
			"}"
		};
		assertEquals(sparqls, expectedSparqls);
	}
}
