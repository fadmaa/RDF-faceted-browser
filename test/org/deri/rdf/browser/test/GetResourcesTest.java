package org.deri.rdf.browser.test;

import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.FederatedRdfEngine;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GetResourcesTest {

	FederatedQueryEngine engine;
	String[] endpoints;
	String mainFilter;
	FederatedRdfEngine rdfEngine;
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
		rdfEngine = new FederatedRdfEngine();
	}
	
	@Test
	public void noFilters(){
		Set<Filter> filters = new HashSet<Filter>();
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		Set<String> items = rdfEngine.getResources(sparql,endpoints[0]);
		
		Set<String> expected = new HashSet<String>();
		expected.add("http://example.org/person/fadi");
		expected.add("http://example.org/person/gofran");
		expected.add("http://example.org/person/alaa");
		
		assertEquals(items, expected);
	}
	
	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		Set<String> items = rdfEngine.getResources(sparql,endpoints[0]);
		
		Set<String> expected = new HashSet<String>();
		expected.add("http://example.org/person/fadi");
		expected.add("http://example.org/person/alaa");
		
		assertEquals(items, expected);
	}
	
	@Test
	public void twoFiltersTwoProperties(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
//		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",false);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		Set<String> items = rdfEngine.getResources(sparql,endpoints[0]);
		
		Set<String> expected = new HashSet<String>();
		expected.add("http://example.org/person/fadi");
		
		assertEquals(items, expected);
	}
	
	@Test
	public void twoFiltersTwoPropertiesTwoValues(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		hobbyF.addValue("http://localhost:3030/test/query","rugby",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		Set<String> items = rdfEngine.getResources(sparql,endpoints[0]);
		
		Set<String> expected = new HashSet<String>();
		expected.add("http://example.org/person/fadi");
		expected.add("http://example.org/person/gofran");
		
		assertEquals(items, expected);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/w3c",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String sparql = engine.resourcesSparql(endpoints, mainFilter, filters, offset, limit);
		Set<String> items = rdfEngine.getResources(sparql,endpoints[0]);
		
		Set<String> expected = new HashSet<String>();
		expected.add("http://example.org/person/fadi");
		expected.add("http://example.org/person/gofran");
		expected.add("http://example.org/person/alaa");
		
		assertEquals(items, expected);
	}
}
