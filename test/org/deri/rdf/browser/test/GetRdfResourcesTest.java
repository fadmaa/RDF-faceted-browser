package org.deri.rdf.browser.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.rdf.browser.FederatedRdfEngine;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GetRdfResourcesTest {

	FederatedQueryEngine engine;
	String[] endpoints;
	String mainFilter;
	FederatedRdfEngine rdfEngine;
	int limit = 10;
	Set<String> properties = new HashSet<String>();
	Set<String> uris = new HashSet<String>();
	
	@BeforeClass
	public void init(){
		engine = new FederatedQueryEngine();
		endpoints = new String[]{
				"http://localhost:3030/test/query",
				"http://localhost:3031/test2/query"
		};
		rdfEngine = new FederatedRdfEngine();
		
		properties.add("http://xmlns.com/foaf/0.1/name");
		properties.add("http://xmlns.com/foaf/0.1/nick");
		properties.add("http://example.org/property/hobby");
		
		uris.add("http://example.org/person/fadi");
		uris.add("http://example.org/person/alaa");
	}
	
	@Test
	public void getResourcesDetails(){
		String detailSparql = engine.resourcesDetailsSparql(uris, properties);
		Collection<RdfResource> resources = rdfEngine.getRdfResources(detailSparql, endpoints);
		Collection<RdfResource> expectedResources = new HashSet<RdfResource>();
		
		RdfResource fadi = new RdfResource("http://example.org/person/fadi");
		fadi.addProperty("http://xmlns.com/foaf/0.1/name", "Fadi");
		fadi.addProperty("http://xmlns.com/foaf/0.1/nick", "sheer");
		fadi.addProperty("http://example.org/property/hobby", "football");
		fadi.addProperty("http://example.org/property/hobby", "chess");
		
		expectedResources.add(fadi);
		
		RdfResource alaa = new RdfResource("http://example.org/person/alaa");
		alaa.addProperty("http://xmlns.com/foaf/0.1/name", "Alaa");
		alaa.addProperty("http://example.org/property/hobby", "football");
		alaa.addProperty("http://example.org/property/hobby", "piano");

		expectedResources.add(alaa);
		
		assertSameResources(resources, expectedResources);
	}

	private void assertSameResources(Collection<RdfResource> resources,	Collection<RdfResource> expected) {
		List<RdfResource> workingCopy = new ArrayList<RdfResource>(expected);
		Set<RdfResource> notAsExpected = new HashSet<RdfResource>();
		Set<RdfResource> notExpected = new HashSet<RdfResource>();
		
		for(RdfResource r:resources){
			if(workingCopy.contains(r)){
				int index = workingCopy.indexOf(r);
				RdfResource e = workingCopy.get(index);
				workingCopy.remove(index);
				if(! (e.getUri().equals(r.getUri()) &&	e.getProperties().equals(r.getProperties())) ){
					notAsExpected.add(r);
				}
			}else{
				notExpected.add(r);
			}
		}
		
		assertTrue(notExpected.isEmpty(), "these elements were not expected: " + notExpected.toString());
		assertTrue(notAsExpected.isEmpty(), "these elements were not as expected: " + notAsExpected.toString());
		assertTrue(workingCopy.isEmpty(), "these elements were missing from the result: " + workingCopy.toString());
	}
}
