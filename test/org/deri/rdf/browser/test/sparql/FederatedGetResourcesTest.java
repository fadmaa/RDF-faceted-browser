package org.deri.rdf.browser.test.sparql;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"SERVICE <http://localhost:3031/test2/query> {" + 
								"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). " + 
							"}" +
						"}" + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). }" + 
					"}" +
				"}" + 
		"} LIMIT 10";

		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void twoFiltersTwoEndpoint(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter memberF = new ComparableFilter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",false);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",false);
		filters.add(memberF);
		Filter hobbyF = new ComparableFilter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		filters.add(hobbyF);
		
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" +
							"SERVICE <http://localhost:3031/test2/query> {" +
								"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). " +
							"}" +
						"}" +
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
				 "}" + 
				 "UNION" + 
				 "{" + 
				 	"SERVICE <http://localhost:3031/test2/query> {" + 
				    	"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
				      	"{" + 
				      		"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). " +
				      	"}" +
			      		"{" + 
			      			"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
			      		"}" + 
			      		"UNION" +
			      		"{" +
			      			"SERVICE <http://localhost:3030/test/query> {" +
			      				"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>."+
			      			"}" + 
			      		"}" + 
			      	"}" +
			    "}" + 
			"} LIMIT 10"
			;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter hobbyF = new ComparableFilter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		hobbyF.addValue("http://localhost:3030/test/query","rugby");
		filters.add(hobbyF);
		
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" + 
				"{"+
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" + 
							"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"rugby\"). " + 
						"}" + 
						"UNION" + 
						"{" + 
							"SERVICE <http://localhost:3031/test2/query> {" + 
								"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). " + 
							"}" + 
						"}" + 
					"}" + 
				"}" + 
				"UNION" + 
				"{" + 
					"SERVICE <http://localhost:3031/test2/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). " +
						"}" + 
						"UNION" + 
						"{" +
							"SERVICE <http://localhost:3030/test/query> {"+
								"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"rugby\"). " +  
							"}" + 
						"}" + 
					"}" + 
				"}" + 
			"} LIMIT 10"
			;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void twoFiltersTwoValues(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter hobbyF = new ComparableFilter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		hobbyF.addValue("http://localhost:3030/test/query","rugby");
		filters.add(hobbyF);
		Filter memberF = new ComparableFilter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",false);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",false);
		filters.add(memberF);
		
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" + 
				"{"+
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" + 
							"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"rugby\"). " + 
						"}" + 
						"UNION" + 
						"{" + 
							"SERVICE <http://localhost:3031/test2/query> {" + 
								"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). " + 
							"}" + 
						"}" + 
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
				"}" + 
				"UNION" + 
				"{" + 
					"SERVICE <http://localhost:3031/test2/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). " +
						"}" + 
						"UNION" + 
						"{" +
							"SERVICE <http://localhost:3030/test/query> {"+
								"?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"rugby\"). " +  
							"}" + 
						"}" + 
						"{" + 
							"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>." + 
						"}" + 
						"UNION" +
						"{" +
							"SERVICE <http://localhost:3030/test/query> {" +
								"?s <http://xmlns.com/foaf/0.1/member> <http://example.org/organisation/deri>."+
							"}" + 
						"}" + 
					"}" + 
				"}" + 
			"} LIMIT 10"
			;
		assertEquals(sparql, expectedSparql);
	}
	
}

class ComparableFilter extends Filter implements Comparable<ComparableFilter>{

	public ComparableFilter(String p) {
		super(p);
	}

	@Override
	public int compareTo(ComparableFilter o) {
		return this.getProperty().compareTo(o.getProperty());
	}
	
}
