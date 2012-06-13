package org.deri.rdf.browser.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.rdf.browser.BrowsingEngine;
import org.deri.rdf.browser.FederatedRdfEngine;
import org.deri.rdf.browser.facet.RdfFacet;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.deri.rdf.browser.sparql.model.Filter;
import org.deri.rdf.browser.test.util.AssertionUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class BrowsingEngineTest {

	BrowsingEngine engine;
	
	String[] endpoints;
	String mainFilter;
	
	@BeforeClass
	public void init(){
		endpoints = new String[]{
				"http://localhost:3030/test/query",
				"http://localhost:3031/test2/query"
		};
		mainFilter = "a <http://xmlns.com/foaf/0.1/Person> .";
		engine = new BrowsingEngine(new FederatedQueryEngine(), new FederatedRdfEngine(),endpoints,mainFilter);
	}
	
	@Test
	public void noFilters(){
		Set<Filter> filters = new HashSet<Filter>();
		String property = "http://xmlns.com/foaf/0.1/member";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE, 
				new String[] {"http://localhost:3030/test/query", "http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", RdfDecoratedValue.RESOURCE, 
				new String[] {"http://localhost:3031/test2/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void noFiltersWithMissingVals(){
		Set<Filter> filters = new HashSet<Filter>();
		String property = "http://xmlns.com/foaf/0.1/nick";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "sheer", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(3, "missing value", RdfDecoratedValue.NULL, 
				new String[] {} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterOneEndpoint(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE, 
				new String[] {"http://localhost:3030/test/query", "http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", RdfDecoratedValue.RESOURCE, 
				new String[] {"http://localhost:3031/test2/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterOneEndpointWithMissingVals(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/nick";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "sheer", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "missing value", RdfDecoratedValue.NULL, 
				new String[] {} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoEndpoints(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://xmlns.com/foaf/0.1/member");
		hobbyF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		hobbyF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(hobbyF);
		String property = "http://example.org/property/hobby";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));

		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "football", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "rugby", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		expected.add(new AnnotatedResultItem(1, "chess", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoEndpointsWithMissingVals(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String property = "http://xmlns.com/foaf/0.1/nick";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));

		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "sheer", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(2, "missing value", RdfDecoratedValue.NULL, 
				new String[] {} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoValues(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		hobbyF.addValue("http://localhost:3030/test/query","rugby",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/member";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE, 
				new String[] {"http://localhost:3030/test/query", "http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", RdfDecoratedValue.RESOURCE, 
				new String[] {"http://localhost:3031/test2/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoValuesWithMissingVals(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		hobbyF.addValue("http://localhost:3030/test/query","rugby",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		String property = "http://xmlns.com/foaf/0.1/nick";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "sheer", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(3, "missing value", RdfDecoratedValue.NULL, 
				new String[] {} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoValuesTowEndpoints(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/w3c",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String property = "http://example.org/property/hobby";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "football", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "chess", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		expected.add(new AnnotatedResultItem(1, "piano", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(1, "rugby", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3030/test/query"} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}
	
	@Test
	public void oneFilterTwoValuesTowEndpointsWithMissingVals(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter memberF = new Filter("http://xmlns.com/foaf/0.1/member");
		memberF.addValue("http://localhost:3030/test/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/deri",RdfDecoratedValue.RESOURCE);
		memberF.addValue("http://localhost:3031/test2/query","http://example.org/organisation/w3c",RdfDecoratedValue.RESOURCE);
		filters.add(memberF);
		String property = "http://xmlns.com/foaf/0.1/nick";
		engine.setFilters(filters);
		List<AnnotatedResultItem> items = engine.getPropertiesWithCount(new RdfFacetMock(property));
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "sheer", RdfDecoratedValue.LITERAL, 
				new String[] {"http://localhost:3031/test2/query"}));
		expected.add(new AnnotatedResultItem(3, "missing value", RdfDecoratedValue.NULL, 
				new String[] {} ));
		
		AssertionUtil.assertEqualItemLists(items, expected);
	}

	@Test
	public void countResources(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		engine.setFilters(filters);
		long count = engine.getResourcesCount();
		assertEquals(count,2l);
	}
	
	@Test
	public void countResources2(){
		Set<Filter> filters = new HashSet<Filter>();
		Filter hobbyF = new Filter("http://example.org/property/hobby");
		hobbyF.addValue("http://localhost:3031/test2/query","football",RdfDecoratedValue.LITERAL);
		hobbyF.addValue("http://localhost:3030/test/query","rugby",RdfDecoratedValue.LITERAL);
		filters.add(hobbyF);
		engine.setFilters(filters);
		long count = engine.getResourcesCount();
		assertEquals(count,3l);
	}
}

class RdfFacetMock implements RdfFacet{

	private final String property;
	
	public RdfFacetMock(String p) {
		this.property = p;
	}
	@Override
	public Filter getFilter() {
		return new Filter(property);
	}
	
	@Override
	public void setChoices(List<AnnotatedResultItem> items) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeFromJSON(JSONObject o) throws JSONException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(JSONWriter writer) throws JSONException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
