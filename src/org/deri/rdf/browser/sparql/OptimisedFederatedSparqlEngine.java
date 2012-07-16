package org.deri.rdf.browser.sparql;

import java.util.Collection;
import java.util.Set;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.util.SparqlUtil;

import com.google.common.collect.SetMultimap;

public class OptimisedFederatedSparqlEngine {
	
	public String getFocusItemsSparql(String[] endpoints, MainFilter mainFilter, Collection<Facet> facets, int start, int length){
		StringBuilder builder = new StringBuilder("SELECT DISTINCT ?");
		builder.append(mainFilter.getVarname()).append(" WHERE{");
		builder.append(getWhereClause(endpoints, mainFilter, facets));
		builder.append("} ORDER BY ?").append(mainFilter.getVarname()).append(" OFFSET ").append(start).append(" LIMIT ").append(length);
		return builder.toString();
	}
	
	public String countFocusItemsSparql(String[] endpoints,	MainFilter mainFilter, Collection<Facet> facets) {
		StringBuilder builder = new StringBuilder("SELECT (COUNT(DISTINCT ?");
		builder.append(mainFilter.getVarname()).append(") AS ?count) WHERE{");
		builder.append(getWhereClause(endpoints, mainFilter, facets));
		builder.append("}");
		return builder.toString();
	}
	
	public String getFacetValuesSparql(String[] endpoints, MainFilter mainFilter, Collection<Facet> facets, Facet focusFacet) {
		if(!facets.contains(focusFacet)){
			throw new RuntimeException("Facets set should contain the focus facet");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ?").append(focusFacet.getVarname()).append(" (COUNT(DISTINCT ?").append(mainFilter.getVarname()).append(") AS ?count) WHERE{");
		builder.append(getWhereClause(endpoints, mainFilter, facets));
		//add focus facet patterns
		String focusFacetPattern = "?" + mainFilter.getVarname() + " " + focusFacet.getFilter().getPattern() + " ?" + focusFacet.getVarname() + " .";
		for(int i=0;i<endpoints.length;i++){
			String ep = endpoints[i];
			builder.append("{SERVICE <").append(ep).append(">{");
			builder.append(focusFacetPattern);
			builder.append("}}");
			if(i<endpoints.length-1){
				builder.append("UNION");
			}
		}
		builder.append("} GROUP BY ?").append(focusFacet.getVarname());
		return builder.toString();	
	}

	public String countItemsMissingFacetSparql(String[] endpoints, MainFilter mainFilter, Collection<Facet> facets, Facet focusFacet) {
		if(!facets.contains(focusFacet)){
			throw new RuntimeException("Facets set should contain the focus facet");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT (COUNT(DISTINCT ?").append(mainFilter.getVarname()).append(") AS ?count) WHERE{");
		for(int i=0; i<endpoints.length;i++){
			String currentEndpoint = endpoints[i];
			builder.append("{SERVICE <");
			builder.append(currentEndpoint);
			builder.append(">{");
			builder.append(mainFilter.getSparqlPattern());
			for(Facet facet:facets){
				if(facet.equals(focusFacet)){
					String propertyFilter = "?" + mainFilter.getVarname() + " " + facet.getFilter().getPattern() + " ?" + facet.getVarname() + ". ";
					builder.append("OPTIONAL {");
					builder.append(union(endpoints, currentEndpoint, propertyFilter));
					builder.append("} FILTER (!bound(?").append(focusFacet.getVarname()).append(")) .");
					continue;
				}
				SetMultimap<String, RdfDecoratedValue> endpointsMap = facet.getEndpointValuesMap();
				for(String ep:endpointsMap.keySet()){
					builder.append("{");
					if(!ep.equals(currentEndpoint)){
						builder.append("SERVICE <");
						builder.append(ep);
						builder.append(">{");
					}
					builder.append(union(mainFilter, facet, endpointsMap.get(ep)));
					builder.append("}");
					if(!ep.equals(currentEndpoint)){
						builder.append("}");
					}
					builder.append("UNION");
				}
				//get rid of the last union
				if(! endpointsMap.isEmpty()){
					int lngth = builder.length();
					builder.delete(lngth-5,lngth);
				}
				
				if(facet.missingValueSelected()){
					String propertyFilter = "?" + mainFilter.getVarname() + " " + facet.getFilter().getPattern() + " ?" + facet.getVarname() + DISAMBIGUATION_SUFFIX + ". ";
					builder.append("OPTIONAL {");
					builder.append(union(endpoints, currentEndpoint, propertyFilter));
					builder.append("} FILTER (!bound(?").append(facet.getVarname()).append(DISAMBIGUATION_SUFFIX).append(")) .");
				}
			}
			builder.append("}}");
			if(i<endpoints.length -1){
				builder.append("UNION");
			}
		}
		builder.append("}");
		return builder.toString();	
	}
	
	public String[] propertiesSparql(String[] endpoints,MainFilter mainFilter, Facet facet) {
		String[] sparqls = new String[endpoints.length]; 
		for(int i=0; i<endpoints.length;i++){
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT DISTINCT ?").append(facet.getVarname()).append(" WHERE{");
			builder.append(mainFilter.getSparqlPattern());
			builder.append(" ?").append(mainFilter.getVarname());
			builder.append(facet.getFilter().getPattern());
			builder.append("> ?").append(facet.getVarname()).append(" . ");
			builder.append("}");
			sparqls[i] = builder.toString();
		}
		return sparqls;
	}
	
	public String resourcesDetailsSparql(String[] endpoints, Set<String> uris, Set<String> properties) {
		StringBuilder builder = new StringBuilder();
		builder.append(RESOURCES_DETAILS_SPARQL_PROLOG);
		
		for(String ep:endpoints){
			builder.append("{SERVICE<").append(ep).append("> {");
			builder.append("?s ?p ?o .");
			builder.append(orFilter("p",properties));
			builder.append(orFilter("s",uris));
			builder.append("}}UNION");
		}
		SparqlUtil.getRidOfLastUnion(builder);		
		builder.append("}");
		return builder.toString();
	}
	
	private String getWhereClause(String[] endpoints, MainFilter mainFilter, Collection<Facet> facets) {
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<endpoints.length;i++){
			String ep = endpoints[i];
			builder.append("{SERVICE <");
			builder.append(ep);
			builder.append(">{");
			builder.append(mainFilter.getSparqlPattern());
			//append filter vlaues
			appendFilters(builder,endpoints, mainFilter, facets, ep);
			builder.append("}}");
			if(i<endpoints.length -1){
				builder.append("UNION");
			}
		}		
		return builder.toString();
	}
	
	private void appendFilters(StringBuilder builder, String[] endpoints, MainFilter mainFilter, Collection<Facet> facets, String currentEndpoint){
		for(Facet facet:facets){
			StringBuilder subBuilder = new StringBuilder();
			SetMultimap<String, RdfDecoratedValue> endpointsMap = facet.getEndpointValuesMap();
			for(String ep:endpointsMap.keySet()){
				subBuilder.append("{");
				if(!ep.equals(currentEndpoint)){
					subBuilder.append("SERVICE <");
					subBuilder.append(ep);
					subBuilder.append(">{");
				}
				subBuilder.append(union(mainFilter, facet, endpointsMap.get(ep)));
				subBuilder.append("}");
				if(!ep.equals(currentEndpoint)){
					subBuilder.append("}");
				}
				subBuilder.append("UNION");
			}
			//get rid of the last union
			if(! endpointsMap.isEmpty()){
				int lngth = subBuilder.length();
				subBuilder.delete(lngth-5,lngth);
			}
			
			if(facet.missingValueSelected()){
				String propertyFilter = "?" + mainFilter.getVarname() + " " + facet.getFilter().getPattern() + " ?" + facet.getVarname() + DISAMBIGUATION_SUFFIX + ". ";
				if(! endpointsMap.isEmpty()){
					subBuilder.append("UNION{");
				}
				subBuilder.append(union(endpoints, currentEndpoint, propertyFilter));
				if(! endpointsMap.isEmpty()){
					subBuilder.append("}");
				}
				//left join
				subBuilder.replace(0, 0, "OPTIONAL {");
				subBuilder.append("} FILTER (!bound(?").append(facet.getVarname()).append(DISAMBIGUATION_SUFFIX).append(")) .");
			}
			builder.append(subBuilder);
		}
	}
	
	private String union(MainFilter mainFilter, Facet facet, Set<RdfDecoratedValue> set) {
		if(set.isEmpty()){
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for(RdfDecoratedValue dv:set){
			builder.append("{?").append(mainFilter.getVarname()).append(" ").append(facet.getFilter().getPattern());
			if(dv.getType()==RdfDecoratedValue.LITERAL){
				builder.append(" ?").append(facet.getVarname()).append(". FILTER(str(?").append(facet.getVarname()).append(")=\"").append(dv.getValue()).append("\"). ") ;
			}else{
				builder.append(" <").append(dv.getValue()).append(">.") ;
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
	
	private String union(String[] endpoints, String currentEndpoint, String propertyFilter) {
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<endpoints.length;i++){
			String ep = endpoints[i];
			builder.append("{");
			if(! ep.equals(currentEndpoint)){
				builder.append("SERVICE <");
				builder.append(ep);
				builder.append(">{");
			}	
			builder.append(propertyFilter);
			if(! ep.equals(currentEndpoint)){
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
		if(vals.isEmpty()){
			return "";
		}
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
	
	
	private static final String DISAMBIGUATION_SUFFIX = "_v";
	private static final String RESOURCES_DETAILS_SPARQL_PROLOG = "SELECT ?s ?p ?o WHERE{" ;
}
