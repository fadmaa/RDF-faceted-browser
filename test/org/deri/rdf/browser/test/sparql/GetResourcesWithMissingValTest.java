package org.deri.rdf.browser.test.sparql;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetResourcesWithMissingValTest {

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
	public void onlyMissingValFilter(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter nickF = new Filter("http://xmlns.com/foaf/0.1/nick");
		nickF.addMissingValue();
		filters.add(nickF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"OPTIONAL {" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?v. }" +
							"UNION" +
							"{" +
								"SERVICE <http://localhost:3031/test2/query>{" +
									"?s <http://xmlns.com/foaf/0.1/nick> ?v. " +
								"}" +
							"}" + 
						"}" +
						"FILTER (!bound(?v)). " + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"OPTIONAL {" +
							"{" +
								"SERVICE <http://localhost:3030/test/query>{" +
									"?s <http://xmlns.com/foaf/0.1/nick> ?v. " +
								"}" +
							"}" +
							"UNION" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?v. }" +
						"}" +
						"FILTER (!bound(?v)). " +
					"}" +
				"}" + 
		"} LIMIT 10";

		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void missingValAndOtherFilter(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter nickF = new ComparableFilter("http://xmlns.com/foaf/0.1/nick");
		nickF.addMissingValue();
		filters.add(nickF);
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
						"OPTIONAL {" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?v. }" +
							"UNION" +
							"{" +
								"SERVICE <http://localhost:3031/test2/query>{" +
									"?s <http://xmlns.com/foaf/0.1/nick> ?v. " +
								"}" +
							"}" + 
						"}" +
						"FILTER (!bound(?v)). " + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{?s <http://example.org/property/hobby> ?rv. FILTER(str(?rv)=\"football\"). }" +
						"OPTIONAL {" +
							"{" +
								"SERVICE <http://localhost:3030/test/query>{" +
									"?s <http://xmlns.com/foaf/0.1/nick> ?v. " +
								"}" +
							"}" +
							"UNION" +
							"{?s <http://xmlns.com/foaf/0.1/nick> ?v. }" +
						"}" +
						"FILTER (!bound(?v)). " +
					"}" +
				"}" + 
		"} LIMIT 10";

		assertEquals(sparql, expectedSparql);
	}
	
	@Test
	public void missingValWithOtherVal(){
		Set<Filter> filters = new TreeSet<Filter>();
		Filter nickF = new ComparableFilter("http://xmlns.com/foaf/0.1/nick");
		nickF.addMissingValue();
		nickF.addValue("http://localhost:3031/test2/query", "sheer");
		filters.add(nickF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, limit);
		String expectedSparql =
			"SELECT DISTINCT ?s " +
			"WHERE{" +
				"{" +
					"SERVICE <http://localhost:3030/test/query> {" + 
						"?s a <http://xmlns.com/foaf/0.1/Person> ." + 
						"{" +
							"SERVICE <http://localhost:3031/test2/query> {" +
								"?s <http://xmlns.com/foaf/0.1/nick> ?rv. FILTER(str(?rv)=\"sheer\"). " + 
							"}" +
						"}" +
						"UNION" +
						"{" +
							"OPTIONAL {" +
								"{?s <http://xmlns.com/foaf/0.1/nick> ?v. }" +
								"UNION" +
								"{" +
									"SERVICE <http://localhost:3031/test2/query>{" +
										"?s <http://xmlns.com/foaf/0.1/nick> ?v. " +
									"}" +
								"}" + 
							"}" +
							"FILTER (!bound(?v)). " +
						"}" + 
					"}" +
				"}" + 
				"UNION" + 
				"{" +
					"SERVICE <http://localhost:3031/test2/query> {" +
						"?s a <http://xmlns.com/foaf/0.1/Person> ." +
						"{?s <http://xmlns.com/foaf/0.1/nick> ?rv. FILTER(str(?rv)=\"sheer\"). }" +
						"UNION" +
						"{" +
							"OPTIONAL {" +
								"{" +
									"SERVICE <http://localhost:3030/test/query>{" +
										"?s <http://xmlns.com/foaf/0.1/nick> ?v. " +
									"}" +
								"}" +
								"UNION" +
								"{?s <http://xmlns.com/foaf/0.1/nick> ?v. }" +
							"}" +
							"FILTER (!bound(?v)). " +
						"}" +
					"}" +
				"}" + 
		"} LIMIT 10";

		assertEquals(sparql, expectedSparql);
	}
}
