package org.deri.rdf.browser.federated.functest;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.rdf.browser.RdfEngine;
import org.deri.rdf.browser.functest.util.AssertionUtil;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.NaiveFederatedSparqlEngine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetPropertiesWithCountTest {

	RdfEngine rdfEngine;
	NaiveFederatedSparqlEngine sparqlEngine;
	String[] endpoints = new String[]{
			"http://localhost:3030/test/query",
			"http://localhost:3031/test/query"
	};
	MainFilter mainFilter = new MainFilter("s", "s", "a <http://xmlns.com/foaf/0.1/Person> .");

	@BeforeClass
	public void init(){
		sparqlEngine = new NaiveFederatedSparqlEngine();
		rdfEngine = new RdfEngine();
	}
	
	@Test
	public void noFilterNickProperty(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet nickFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick", "nick");
		facets.add(nickFacet);//this is important as the method expects the focusfacet to be in facets
		String sparql = sparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, nickFacet);
		List<AnnotatedResultItem> res = rdfEngine.getPropertiesWithCount(sparql, endpoints[0], nickFacet.getVarname());
		
		assertEquals(res.size(), 2);
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "sheer", RdfDecoratedValue.LITERAL));
		expected.add(new AnnotatedResultItem(1, "philA", RdfDecoratedValue.LITERAL));
		
		AssertionUtil.assertEqualItemLists(res, expected);
		
	}

	@Test
	public void noFilterMemberProperty(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);//this is important as the method expects the focusfacet to be in facets
		String sparql = sparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, memberFacet);
		List<AnnotatedResultItem> res = rdfEngine.getPropertiesWithCount(sparql, endpoints[0], memberFacet.getVarname());
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "http://example.org/organisation/w3c", RdfDecoratedValue.RESOURCE));
		expected.add(new AnnotatedResultItem(2, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE));
		
		AssertionUtil.assertEqualItemLists(res, expected);
	}
	
	@Test
	public void filterMemberProperty(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);//this is important as the method expects the focusfacet to be in facets
		Facet nickFacet =  new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick", "nick");
		nickFacet.addLiteralValue("sheer");
		nickFacet.addLiteralValue("cygri");
		facets.add(nickFacet);
		
		String sparql = sparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, memberFacet);
		List<AnnotatedResultItem> res = rdfEngine.getPropertiesWithCount(sparql, endpoints[0], memberFacet.getVarname());
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE));
		
		AssertionUtil.assertEqualItemLists(res, expected);
	}
	
	@Test
	public void missingValueFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);//this is important as the method expects the focusfacet to be in facets
		Facet nickFacet =  new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick", "nick");
		nickFacet.setMissingValueSelected(true);
		facets.add(nickFacet);
		
		String sparql = sparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, memberFacet);
		List<AnnotatedResultItem> res = rdfEngine.getPropertiesWithCount(sparql, endpoints[0], memberFacet.getVarname());
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", RdfDecoratedValue.RESOURCE));
		
		AssertionUtil.assertEqualItemLists(res, expected);
	}
	
	@Test
	public void missingValueAndotherFilter(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);//this is important as the method expects the focusfacet to be in facets
		Facet nickFacet =  new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick", "nick");
		nickFacet.setMissingValueSelected(true);
		nickFacet.addLiteralValue("sheer");
		facets.add(nickFacet);
		
		String sparql = sparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, memberFacet);
		List<AnnotatedResultItem> res = rdfEngine.getPropertiesWithCount(sparql, endpoints[0], memberFacet.getVarname());
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(2, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE));
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", RdfDecoratedValue.RESOURCE));
		
		AssertionUtil.assertEqualItemLists(res, expected);
	}
	
	@Test
	public void missingValueAndotherFilter2(){
		Set<Facet> facets = new HashSet<Facet>();
		Facet memberFacet = new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/member>"), "org", "organisation");
		facets.add(memberFacet);//this is important as the method expects the focusfacet to be in facets
		Facet nickFacet =  new Facet(new FacetFilter("<http://xmlns.com/foaf/0.1/nick>"), "nick", "nick");
		nickFacet.setMissingValueSelected(true);
		nickFacet.addLiteralValue("cygri");//not in the data
		facets.add(nickFacet);
		
		String sparql = sparqlEngine.getFacetValuesSparql(endpoints, mainFilter, facets, memberFacet);
		List<AnnotatedResultItem> res = rdfEngine.getPropertiesWithCount(sparql, endpoints[0], memberFacet.getVarname());
		
		List<AnnotatedResultItem> expected = new ArrayList<AnnotatedResultItem>();
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/deri", RdfDecoratedValue.RESOURCE));//only 1
		expected.add(new AnnotatedResultItem(1, "http://example.org/organisation/w3c", RdfDecoratedValue.RESOURCE));
		
		AssertionUtil.assertEqualItemLists(res, expected);
	}
}
