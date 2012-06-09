package org.deri.rdf.browser.sparql;

import java.util.Set;

import org.deri.rdf.browser.facet.RdfDecoratedValue;
import org.deri.rdf.browser.sparql.model.Filter;

import com.google.common.collect.SetMultimap;

public class FederatedQueryEngine {

	public String propertiesWithCountSparql(String[] endpoints,	String mainFilter, Set<Filter> filters, String property) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ?v (COUNT(?v) AS ?v_count) WHERE{");
		for(int i=0; i<endpoints.length;i++){
			String ep = endpoints[i];
			builder.append("{SERVICE <");
			builder.append(ep);
			builder.append("> {?s ");
			builder.append(mainFilter);
			builder.append(" ?s <");
			builder.append(property);
			builder.append("> ?v . ");
			//append filter vlaues
			for(Filter filter: filters){
				if(! filter.getEndpointValuesMap().isEmpty()){
					SetMultimap<String, RdfDecoratedValue> endpointsMap = filter.getEndpointValuesMap();
					//do the current endpoint first
					if(endpointsMap.containsKey(ep)){
						builder.append("{");
						builder.append(union(filter.getProperty(), endpointsMap.get(ep)));
						builder.append("}");
						builder.append("UNION");
					}
					for(String wep:endpointsMap.keySet()){
						if(wep.equals(ep)){
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
			}
			builder.append("}}");
			if(i<endpoints.length -1){
				builder.append("UNION");
			}
		}
		builder.append("} GROUP BY ?v");
		return builder.toString();
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
			builder.append("}}");
			if(i<endpoints.length -1){
				builder.append("UNION");
			}
		}
		builder.append("} LIMIT ");
		builder.append(limit);
		return builder.toString();
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
		//get rid of the last union
		int lngth = builder.length();
		builder.delete(lngth-5,lngth);
		
		return builder.toString();
	}

}
