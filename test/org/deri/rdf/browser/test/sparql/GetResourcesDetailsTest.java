package org.deri.rdf.browser.test.sparql;

import java.util.Set;
import java.util.TreeSet;

import org.deri.rdf.browser.sparql.FederatedQueryEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GetResourcesDetailsTest {

	FederatedQueryEngine engine;
	String[] endpoints;
	Set<String> uris = new TreeSet<String>();
	
	@BeforeClass
	public void init(){
		engine = new FederatedQueryEngine();
		endpoints = new String[]{
				"http://localhost:3030/test/query",
				"http://localhost:3031/test2/query"
		};
		
		uris.add("http://example.org/person/fadi");
		uris.add("http://example.org/person/alaa");
	};

	@Test
	public void test(){
		Set<String> properties = new TreeSet<String>();
		properties.add("http://xmlns.com/foaf/0.1/name");
		properties.add("http://xmlns.com/foaf/0.1/member");
		
		String sparql = engine.resourcesDetailsSparql(uris,properties);
		String expectedSparql = 
			"SELECT ?s ?p ?o " +
			"WHERE{" +
				"?s ?p ?o. " +
				"FILTER(?p=<http://xmlns.com/foaf/0.1/member> || ?p=<http://xmlns.com/foaf/0.1/name>)." +
				"FILTER(?s=<http://example.org/person/alaa> || ?s=<http://example.org/person/fadi>)." +
			"}";
		
		assertEquals(sparql,expectedSparql);
	}
}
