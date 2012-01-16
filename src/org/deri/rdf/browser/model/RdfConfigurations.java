package org.deri.rdf.browser.model;

public class RdfConfigurations {

//	public static String sparqlEndpoint = "http://localhost:8080/openrdf-sesame/repositories/dcats";
//	public static String sparqlEndpoint = "http://semantic.ckan.net/sparql/";
//	public static String sparqlEndpoint = "http://localhost:3030/dcats/query";
//	public static String mainResourcesSelector = "?x a <http://www.w3.org/ns/dcat#Dataset>. ";
	//null for getting all properties
	public static String[] labelProperties = new String[]{"http://purl.org/dc/terms/title","http://purl.org/dc/terms/description",
		//"http://www.w3.org/ns/dcat#keyword"
		};
	
	
	/*
	 * dcat specific
	 */
	public static String distributioinsPattern = "<http://www.w3.org/ns/dcat#distribution> ?dist. ?dist <http://www.w3.org/ns/dcat#accessURL> ?url; " +
			"<http://purl.org/dc/terms/format> ?f. ?f <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> ?format. OPTIONAL{" +
			"?dist <http://www.w3.org/2000/01/rdf-schema#label> ?label.}. "; 
}
