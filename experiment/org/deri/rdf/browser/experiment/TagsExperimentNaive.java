package org.deri.rdf.browser.experiment;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.NaiveFederatedSparqlEngine;

public class TagsExperimentNaive {

	NaiveFederatedSparqlEngine engine;
	MainFilter mainFilter = new MainFilter("s", "a <http://www.w3.org/ns/dcat#Dataset> .");
	String[] endpoints = new String[]{
			"http://localhost:3030/test/query",
			"http://localhost:3031/test/query"
		};
	int start = 0;
	int length = 10;
		
	public void run(String[] tags) throws Exception{
		
	    engine = new NaiveFederatedSparqlEngine();
		
		String filename = "naive/" + StringUtils.join(tags,"-"); 
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		
		Set<Facet> currentFacets = new HashSet<Facet>();
		Facet dcatKeywordFacet = new Facet(new FacetFilter("<http://www.w3.org/ns/dcat#keyword>"), "tag", "tags");
		for(String t:tags){
			dcatKeywordFacet.addLiteralValue(t);
		}
		currentFacets.add(dcatKeywordFacet);
		String[] sparqls = selectValue(currentFacets);
		
		for(String s:sparqls){
			out.println(s);
		}
		
		out.flush();
		out.close();
	}
	
	private String[] selectValue(Set<Facet> facets){
		String[] queries = new String[facets.size()*2 +2];
		
		String getFocusItems = engine.getFocusItemsSparql(endpoints, mainFilter, facets, start, length);
		String countFocusItems = engine.countFocusItemsSparql(endpoints, mainFilter, facets);
		queries[0] = getFocusItems;
		queries[1] = countFocusItems;
		
		int i = 2;
		for(Facet f:facets){
			String facetValues = engine.getFacetValuesSparql(endpoints, mainFilter, facets, f);
			String countMissing = engine.countItemsMissingFacetSparql(endpoints, mainFilter, facets, f);
			queries[i] = facetValues;
			queries[i+1] = countMissing;
			i+=2;
		}
		return queries;
	}
	
	public static void main(String[] args) throws Exception {
		TagsExperimentNaive experiment = new TagsExperimentNaive();
		experiment.run(new String[]{"ontology"});
		experiment.run(new String[]{"ontology","format-rdf"});
		experiment.run(new String[]{"ontology","format-rdf","opendata"});
		experiment.run(new String[]{"ontology","format-rdf","opendata","linkedData"});
		experiment.run(new String[]{"ontology","format-rdf","opendata","linkedData","format-rdfa"});
	}
}
