package org.deri.rdf.browser.sparql;

import java.util.Set;

import org.deri.rdf.browser.facet.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.model.Filter;

import com.google.common.collect.SetMultimap;

public class FederatedQueryEngine {

	public String[] propertiesWithCountSparql(String[] endpoints,	String mainFilter, Set<Filter> filters, String property) {
		String[] sparqls = new String[endpoints.length]; 
		for(int i=0; i<endpoints.length;i++){
			StringBuilder builder = new StringBuilder();
			builder.append(PROPERTIES_WITH_COUNT_SPARQL_PROLOG);
			String ep = endpoints[i];
			builder.append("SERVICE <");
			builder.append(ep);
			builder.append("> {?s ");
			builder.append(mainFilter);
			builder.append(" ?s <");
			builder.append(property);
			builder.append("> ?v . ");
			//append filter vlaues
			appendFilters(builder,filters,endpoints,i);
			builder.append("}");
			builder.append(PROPERTIES_WITH_COUNT_SPARQL_SUFFIX);
			sparqls[i] = builder.toString();
		}
		return sparqls;
	}

	public String[] propertiesSparql(String[] endpoints,	String mainFilter, String property) {
		String[] sparqls = new String[endpoints.length]; 
		for(int i=0; i<endpoints.length;i++){
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT DISTINCT ?v WHERE{");
			String ep = endpoints[i];
			builder.append("SERVICE <");
			builder.append(ep);
			builder.append("> {?s ");
			builder.append(mainFilter);
			builder.append(" ?s <");
			builder.append(property);
			builder.append("> ?v . ");
			builder.append("}");
			builder.append("}");
			sparqls[i] = builder.toString();
		}
		return sparqls;
	}

	public String resourcesSparql(String[] endpoints, String mainFilter, Set<Filter> filters, int limit) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT DISTINCT ?s WHERE{");
		for(int i=0; i<endpoints.length;i++){
			String ep = endpoints[i];
			builder.append("{SERVICE <");
			builder.append(ep);
			builder.append("> {?s ");
			builder.append(mainFilter);
			//append filter vlaues
			appendFilters(builder,filters,endpoints,i);
			builder.append("}}");
			if(i<endpoints.length -1){
				builder.append("UNION");
			}
		}
		builder.append("} LIMIT ");
		builder.append(limit);
		return builder.toString();
	}
	
	public String resourcesDetailsSparql(Set<String> uris, Set<String> properties) {
		StringBuilder builder = new StringBuilder();
		builder.append(RESOURCES_DETAILS_SPARQL_PROLOG);
		builder.append(orFilter("p",properties));
		builder.append(orFilter("s",uris));
		builder.append("}");
		return builder.toString();
	}
	
	public String resourcesCountSparql(String[] endpoints, String mainFilter, Set<Filter> filters) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE{");
		for(int i=0; i<endpoints.length;i++){
			String ep = endpoints[i];
			builder.append("{SERVICE <");
			builder.append(ep);
			builder.append("> {?s ");
			builder.append(mainFilter);
			//append filter vlaues
			appendFilters(builder,filters,endpoints,i);
			builder.append("}}");
			if(i<endpoints.length -1){
				builder.append("UNION");
			}
		}
		builder.append("}");
		return builder.toString();
	}
	
	public String[] propertiesMissingValueSparql(String[] endpoints, String mainFilter, Set<Filter> filters, String property) {
		String[] sparqls = new String[endpoints.length];
		String propertyFilter = "?s <" + property + "> ?v. ";
		for(int i=0; i<endpoints.length;i++){
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE{");
			String ep = endpoints[i];
			builder.append("SERVICE <");
			builder.append(ep);
			builder.append("> {?s ");
			builder.append(mainFilter);
			builder.append("OPTIONAL {");
			builder.append(union(endpoints,i,propertyFilter));
			builder.append("}");
			builder.append("FILTER (!bound(?v)).");
			//append filter vlaues
			appendFilters(builder,filters,endpoints,i);
			builder.append("}}");
			sparqls[i] = builder.toString();
		}
		return sparqls;
	}
	
	private void appendFilters(StringBuilder builder, Set<Filter> filters, String[] endpoints, int current){
		String endpoint = endpoints[current];
		for(Filter filter: filters){
			boolean filtered = false;
			if(! filter.getEndpointValuesMap().isEmpty()){
				filtered = true;
				SetMultimap<String, RdfDecoratedValue> endpointsMap = filter.getEndpointValuesMap();
				//do the current endpoint first
				if(endpointsMap.containsKey(endpoint)){
					builder.append("{");
					builder.append(union(filter.getProperty(), endpointsMap.get(endpoint)));
					builder.append("}");
					builder.append("UNION");
				}
				for(String wep:endpointsMap.keySet()){
					if(wep.equals(endpoint)){
						continue;
					}
					builder.append("{");
					builder.append("SERVICE <");
					builder.append(wep);
					builder.append("> {");
					builder.append(union(filter.getProperty(), endpointsMap.get(wep)));
					builder.append("}");
					builder.append("}");
					builder.append("UNION");
				}
				//get rid of the last union
				int lngth = builder.length();
				builder.delete(lngth-5,lngth);
			}
			
			if(filter.missingValueIncluded()){
				String propertyFilter = "?s <" + filter.getProperty() + "> ?v. ";
				if(filtered){
					builder.append("UNION{");
				}
				builder.append("OPTIONAL {");
				builder.append(union(endpoints, current, propertyFilter));
				builder.append("}FILTER (!bound(?v)). ");
				if(filtered){
					builder.append("}");
				}
			}
		}
	}
	
	private String union(String property, Set<RdfDecoratedValue> set) {
		if(set.isEmpty()){
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for(RdfDecoratedValue dv:set){
			builder.append("{?s <").append(property).append("> ");
			if(dv.isLiteral()){
				builder.append("?rv. FILTER(str(?rv)=\"").append(dv.getValue()).append("\"). ") ;
			}else{
				builder.append("<").append(dv.getValue()).append(">.") ;
			}
			builder.append("}UNION");
		}
		//if set has only one item, no need to the surrounding curly brackets. we use ineItemFilter to get rid of them
		int oneItemFilter = set.size()==1?1:0;
		//get rid of the last union
		int lngth = builder.length();
		builder.delete(lngth-5-oneItemFilter,lngth);
		//get rid of the the very first { in case the filter has only one value
		builder.delete(0, oneItemFilter);
		
		return builder.toString();
	}
	
	private String union(String[] endpoints, int current, String propertyFilter) {
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<endpoints.length;i++){
			builder.append("{");
			if(i!=current){
				builder.append("SERVICE <");
				builder.append(endpoints[i]);
				builder.append(">{");
			}	
			builder.append(propertyFilter);
			if(i!=current){
				builder.append("}");
			}
			builder.append("}UNION");
		}
		//get rid of the last union
		int lngth = builder.length();
		builder.delete(lngth-5,lngth);
		
		return builder.toString();
	}

	private String orFilter(String varname, Set<String> vals) {
		StringBuilder builder = new StringBuilder();
		builder.append("FILTER(");
		for(String val:vals){
			builder.append("?"); builder.append(varname);
			builder.append("=<"); builder.append(val); builder.append("> || ");
		}
		//get rid of the last  ||
		int lngth = builder.length();
		builder.delete(lngth-4, lngth);
		builder.append(").");
		return builder.toString();
		
	}
	
	private static final String PROPERTIES_WITH_COUNT_SPARQL_PROLOG = "SELECT ?v (COUNT(?s) AS ?count) WHERE{";
	private static final String PROPERTIES_WITH_COUNT_SPARQL_SUFFIX = "} GROUP BY ?v";
	
	private static final String RESOURCES_DETAILS_SPARQL_PROLOG = "SELECT ?s ?p ?o WHERE{?s ?p ?o. ";

}
