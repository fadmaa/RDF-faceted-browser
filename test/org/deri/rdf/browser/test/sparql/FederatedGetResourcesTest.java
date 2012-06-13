package org.deri.rdf.browser.test.sparql;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.model.RdfDecoratedValue;
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
	int offset = 0;
	
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
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
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
			"} ORDER BY ?s LIMIT 10 OFFSET 0";
		assertEquals(sparql, expectedSparql);
	}
	

	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"SERVICE <http://localhost:3031/test2/query> {" + 
								"?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"football\"). " + 
							"}" +
						"}" + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{?s <http://example.org/property/hobby> ?rv2. FILTER(str(?rv2)=\"football\"). }" + 
					"}" +
				"}" + 
		"} ORDER BY ?s LIMIT 10 OFFSET 0";

		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void twoFiltersTwoEndpoint(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter memberF = new ComparableFilter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		Filter hobbyF = new ComparableFilter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" +
							"SERVICE <http://localhost:3031/test2/query> {" +
								"?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"football\"). " +
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
				      		"?s <http://example.org/property/hobby> ?rv4. FILTER(str(?rv4)=\"football\"). " +
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
			"} ORDER BY ?s LIMIT 10 OFFSET 0"
			;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter hobbyF = new ComparableFilter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		hobbyF.addValue("http://localhost:3030/test/query","rugby",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" + 
				"{"+
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
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
				"}" + 
				"UNION" + 
				"{" + 
					"SERVICE <http://localhost:3031/test2/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"?s <http://example.org/property/hobby> ?rv3. FILTER(str(?rv3)=\"football\"). " +
						"}" + 
						"UNION" + 
						"{" +
							"SERVICE <http://localhost:3030/test/query> {"+
								"?s <http://example.org/property/hobby> ?rv4. FILTER(str(?rv4)=\"rugby\"). " +  
							"}" + 
						"}" + 
					"}" + 
				"}" + 
			"} ORDER BY ?s LIMIT 10 OFFSET 0"
			;
		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void twoFiltersTwoValues(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter hobbyF = new ComparableFilter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		hobbyF.addValue("http://localhost:3030/test/query","rugby",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		Filter memberF = new ComparableFilter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" + 
				"{"+
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{" + 
							"?s <http://example.org/property/hobby> ?rv1. FILTER(str(?rv1)=\"rugby\"). " + 
						"}" + 
						"UNION" + 
						"{" + 
							"SERVICE <http://localhost:3031/test2/query> {" + 
								"?s <http://example.org/property/hobby> ?rv2. FILTER(str(?rv2)=\"football\"). " + 
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
							"?s <http://example.org/property/hobby> ?rv5. FILTER(str(?rv5)=\"football\"). " +
						"}" + 
						"UNION" + 
						"{" +
							"SERVICE <http://localhost:3030/test/query> {"+
								"?s <http://example.org/property/hobby> ?rv6. FILTER(str(?rv6)=\"rugby\"). " +  
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
			"} ORDER BY ?s LIMIT 10 OFFSET 0"
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
