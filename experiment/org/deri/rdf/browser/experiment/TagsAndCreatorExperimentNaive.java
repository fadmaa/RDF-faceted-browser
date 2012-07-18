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

public class TagsAndCreatorExperimentNaive {


	NaiveFederatedSparqlEngine engine;
	MainFilter mainFilter = new MainFilter("s", "a <http://www.w3.org/ns/dcat#Dataset> .");
	String[] endpoints = new String[]{
			"http://localhost:3030/test/query",
			"http://localhost:3031/test/query"
		};
	int start = 0;
	int length = 10;
		
	public void run(String[] tags, String[] creators) throws Exception{
		
	    engine = new NaiveFederatedSparqlEngine();
		
		String filename = "naive/" + StringUtils.join(tags,"-") + "-" + creators.length; 
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		
		Set<Facet> currentFacets = new HashSet<Facet>();
		Facet dcatKeywordFacet = new Facet(new FacetFilter("<http://www.w3.org/ns/dcat#keyword>"), "tag", "tags");
		for(String t:tags){
			dcatKeywordFacet.addLiteralValue(t);
		}
		currentFacets.add(dcatKeywordFacet);
		
		Facet dctCreatorFacet = new Facet(new FacetFilter("<http://purl.org/dc/terms/creator>"), "creator", "creator");
		for(String c:creators){
			dctCreatorFacet.addResourceValue(c);
		}
		currentFacets.add(dctCreatorFacet);
		
		
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
		TagsAndCreatorExperimentNaive experiment = new TagsAndCreatorExperimentNaive();
		experiment.run(new String[]{"format-rdf"}, new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[]{"format-rdf"}, new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
		
		experiment.run(new String[]{"format-rdf","format-rdfa"}, new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[]{"format-rdf","format-rdfa"}, new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
		
		experiment.run(new String[]{"format-rdf","format-rdfa","ontology"}, new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[]{"format-rdf","format-rdfa","ontology"}, new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
		
		experiment.run(new String[]{"format-rdf","format-rdfa","ontology","opendata"}, new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[]{"format-rdf","format-rdfa","ontology","opendata"}, new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
		
		experiment.run(new String[]{"format-rdf","format-rdfa","ontology","opendata","linkedData"}, new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[]{"format-rdf","format-rdfa","ontology","opendata","linkedData"}, new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
	}

}
