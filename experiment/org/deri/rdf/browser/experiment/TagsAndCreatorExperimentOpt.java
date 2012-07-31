package org.deri.rdf.browser.experiment;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.FacetFilter;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.sparql.EnhancedFederatedSparqlEngine;

public class TagsAndCreatorExperimentOpt {
	EnhancedFederatedSparqlEngine engine;
	MainFilter mainFilter = new MainFilter("s", "s", "a <http://www.w3.org/ns/dcat#Dataset> .");
	String[] endpoints = new String[]{
			"http://localhost:3030/test/query",
			"http://localhost:3031/test/query",
			"http://localhost:3032/test/query"
		};
	int start = 0;
	int length = 10;
		
	public void run(String[][] tags, String[] creators) throws Exception{
		
	    engine = new EnhancedFederatedSparqlEngine();

	    Set<String> set = new LinkedHashSet<String>();
	    for(String[] t:tags){
	    	set.add(t[0]);
	    }
	    String filename = "optimised/2/" + StringUtils.join(set,"-") + "-" + creators.length;
		FileWriter outFile = new FileWriter(filename);
		PrintWriter out = new PrintWriter(outFile);
		
		Set<Facet> currentFacets = new HashSet<Facet>();
		Facet dcatKeywordFacet = new Facet(new FacetFilter("<http://www.w3.org/ns/dcat#keyword>"), "tag", "tags");
		for(String[] t:tags){
			dcatKeywordFacet.addLiteralValue(t[0],t[1]);
		}
		currentFacets.add(dcatKeywordFacet);
		
		Facet dctCreatorFacet = new Facet(new FacetFilter("<http://purl.org/dc/terms/creator>"), "creator", "creator");
		for(String c:creators){
			dctCreatorFacet.addResourceValue(c, "http://localhost:3030/test/query");
		}
		currentFacets.add(dctCreatorFacet);
	
		String[] sparqls = selectValueOpt(currentFacets);
		
		for(String s:sparqls){
			out.println(s);
		}
		
		out.flush();
		out.close();
	}
	
	private String[] selectValueOpt(Set<Facet> facets){
		String[] queries = new String[facets.size()*(2+endpoints.length) +2];
		
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
			String[] annotateQs = engine.annotateValuesSparql(endpoints, mainFilter, f);
			for(String q:annotateQs){
				queries[i] = q;
				i+=1;
			}
		}
		return queries;
	}
	
	public static void main(String[] args) throws Exception {
		TagsAndCreatorExperimentOpt experiment = new TagsAndCreatorExperimentOpt();
		experiment.run(new String[][] {new String[]{"format-rdf","http://localhost:3030/test/query"}
									},
						new String[] {"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[][] {new String[]{"format-rdf","http://localhost:3030/test/query"}
									},
						new String[] {"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
		
		
		experiment.run(new String[][] {new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
									},
						new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[][] {
										new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
									},
						new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});

		experiment.run(new String[][] {
										new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3031/test/query"}
									},
						new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[][] {
										new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3031/test/query"}
									},
						new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
		
		experiment.run(new String[][] {
										new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3031/test/query"}
										,new String[]{"opendata","http://localhost:3030/test/query"}
										,new String[]{"opendata","http://localhost:3031/test/query"}
									},
						new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[][] {
										new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3031/test/query"}
										,new String[]{"opendata","http://localhost:3030/test/query"}
										,new String[]{"opendata","http://localhost:3031/test/query"}
									},
						new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
		
		experiment.run(new String[][] {
										new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3031/test/query"}
										,new String[]{"opendata","http://localhost:3030/test/query"}
										,new String[]{"opendata","http://localhost:3031/test/query"}
										,new String[]{"linkedData","http://localhost:3031/test/query"}
									},
						new String[]{"http://thedatahub.org/dataset/ian-davis"});
		experiment.run(new String[][] {
										new String[]{"format-rdf","http://localhost:3030/test/query"}
										,new String[]{"format-rdfa","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3030/test/query"}
										,new String[]{"ontology","http://localhost:3031/test/query"}
										,new String[]{"opendata","http://localhost:3030/test/query"}
										,new String[]{"opendata","http://localhost:3031/test/query"}
										,new String[]{"linkedData","http://localhost:3031/test/query"}
									},
					new String[]{"http://thedatahub.org/dataset/ian-davis", "http://thedatahub.org/dataset/hugh-glaser"});
	}
}
