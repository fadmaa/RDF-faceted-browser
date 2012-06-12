package org.deri.rdf.browser.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.rdf.browser.FederatedRdfEngine;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.deri.rdf.browser.test.util.AssertionUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PropertiesWithCountTest {
	FederatedQueryEngine engine;
	String[] endpoints;
	String mainFilter;
	FederatedRdfEngine rdfEngine;
	
	@BeforeClass
	public void init(){
		engine = new FederatedQueryEngine();
		endpoints = new String[]{
				"http://localhost:3030/test/query",
				"http://localhost:3031/test2/query"
		};
		mainFilter = "a <http://xmlns.com/foaf/0.1/Person> .";
		rdfEngine = new FederatedRdfEngine();
	}
	
	@Test
	public void noFilters(){
		Set<Filter> filters = new HashSet<Filter>();
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparqls,endpoints,true);
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "http://example.org/organisation/deri", AnnotatedResultItem.RESOURCE, 
				new String[] {"http://localhost:3030/test/query", "http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", AnnotatedResultItem.RESOURCE, 
				new String[] {"http://localhost:3031/test2/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparqls,endpoints,false);
		
		String[] propSparqls = engine.propertiesSparql(endpoints, mainFilter, property);
		rdfEngine.annotatePropertiesWithEndpoints(propSparqls, endpoints, items);
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/deri", AnnotatedResultItem.RESOURCE, 
				new String[] {"http://localhost:3030/test/query", "http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", AnnotatedResultItem.RESOURCE, 
				new String[] {"http://localhost:3031/test2/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoEndpoints(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://xmlns.com/foaf/0.1/member");
		hobbyF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",false);
		hobbyF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",false);
		filters.add(hobbyF);
		String property = "http://example.org/property/hobby";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparqls,endpoints,false);
		
		String[] propSparqls = engine.propertiesSparql(endpoints, mainFilter, property);
		rdfEngine.annotatePropertiesWithEndpoints(propSparqls, endpoints, items);
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "football", AnnotatedResultItem.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "rugby", AnnotatedResultItem.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		expected.add(new AnnotatedResultItem(1, "chess", AnnotatedResultItem.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football");
		hobbyF.addValue("http://localhost:3030/test/query","rugby");
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparqls,endpoints,false);
		
		String[] propSparqls = engine.propertiesSparql(endpoints, mainFilter, property);
		rdfEngine.annotatePropertiesWithEndpoints(propSparqls, endpoints, items);
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "http://example.org/organisation/deri", AnnotatedResultItem.RESOURCE, 
				new String[] {"http://localhost:3030/test/query", "http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", AnnotatedResultItem.RESOURCE, 
				new String[] {"http://localhost:3031/test2/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoValuesTowEndpoints(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",false);
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",false);
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/w3c",false);
		filters.add(memberF);
		String property = "http://example.org/property/hobby";
		String[] sparqls = engine.propertiesWithCountSparql(endpoints, mainFilter, filters, property);
		List<AnnotatedResultItem> items = rdfEngine.getPropertiesWithCount(sparqls,endpoints,false);
		
		String[] propSparqls = engine.propertiesSparql(endpoints, mainFilter, property);
		rdfEngine.annotatePropertiesWithEndpoints(propSparqls, endpoints, items);
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "football", AnnotatedResultItem.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "chess", AnnotatedResultItem.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		expected.add(new AnnotatedResultItem(1, "piano", AnnotatedResultItem.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "rugby", AnnotatedResultItem.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}


}
