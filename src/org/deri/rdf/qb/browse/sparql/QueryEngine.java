package org.deri.rdf.qb.browse.sparql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deri.rdf.browser.model.RdfResource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class QueryEngine {

	public Collection<RdfResource> getDatasets(String sparqlEndpoint){
		return getResources(sparqlEndpoint, DATASET_QUERY, "ds", null);
	}
	
	public Collection<RdfResource> getDimensions(String sparqlEndpoint){
		return getResources(sparqlEndpoint, DIMENSION_QUERY, "d", null);
	}
	
	public Collection<RdfResource> getMeasures(String sparqlEndpoint){
		return getResources(sparqlEndpoint, MEASURE_QUERY, "m", null);
	}
	
    private Collection<RdfResource> getResources(String sparqlEndpoint, String query, String varname,String graphname){
    	QueryExecution qExec;
    	if(graphname==null){
			qExec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		}else{
			qExec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query,graphname);
		}
		ResultSet res = qExec.execSelect();
		Set<RdfResource> resources = new HashSet<RdfResource>();
		while(res.hasNext()){
			QuerySolution sol = res.next();
			RdfResource r = new RdfResource(sol.getResource(varname).getURI());
			if(sol.get("lbl1")!=null){
				r.getProperties().put(RDFS_LABEL,sol.getLiteral("lbl1").getString());
			}else if(sol.get("lbl2")!=null){
				r.getProperties().put(RDFS_LABEL,sol.getLiteral("lbl2").getString());
			}
			resources.add(r);
		}
		return resources;
	}
    
	private static final String DATASET_QUERY = 
			"PREFIX qb:<http://purl.org/linked-data/cube#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"SELECT DISTINCT ?ds ?lbl1 " +
			"WHERE{" +
				"?ds a qb:Dataset. " +
				"OPTIONAL{ ?ds rdfs:label ?lbl1. }" +
			"}";
	private static final String DIMENSION_QUERY = 
			"PREFIX qb:<http://purl.org/linked-data/cube#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
			"SELECT DISTINCT ?d ?lbl1 ?lbl2 " +
			"WHERE{" +
				"?o ?d ?v; a qb:Observation. ?d a qb:DimensionProperty. " +
				"OPTIONAL { ?d skos:prefLabel ?lbl1. } " +
				"OPTIONAL { ?d rdfs:label ?lbl2. } " +
			"}";
	private static final String MEASURE_QUERY =
		"PREFIX qb:<http://purl.org/linked-data/cube#> " +
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
		"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
		"SELECT DISTINCT ?m ?lbl1 ?lbl2 " +
		"WHERE{" +
			"?o ?m ?v; a qb:Observation. ?m a qb:MeasureProperty. " +
			"OPTIONAL{ ?m skos:prefLabel ?lbl1. }" +
			"OPTIONAL{ ?m rdfs:label ?lbl2. }" +
		"}"; 
	private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#";
}
