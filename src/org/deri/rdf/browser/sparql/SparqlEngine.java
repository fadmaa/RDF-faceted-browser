package org.deri.rdf.browser.sparql;

import java.util.Collection;
import java.util.Set;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.util.SparqlUtil;

public class SparqlEngine {

	public String getFocusItemsSparql(MainFilter mainFilter, Collection<Facet> facets, int start, int length) {
		StringBuilder builder = new StringBuilder("SELECT DISTINCT ?");
		builder.append(mainFilter.getVarname()).append(" WHERE{");
		builder.append(getWhereClause(mainFilter, facets));
		builder.append("} ORDER BY ?").append(mainFilter.getVarname()).append(" OFFSET ").append(start).append(" LIMIT ").append(length);
		return builder.toString();
	}

	public String countFocusItemsSparql(MainFilter mainFilter, Collection<Facet> facets) {
		StringBuilder builder = new StringBuilder("SELECT (COUNT(DISTINCT ?");
		builder.append(mainFilter.getVarname()).append(") AS ?count) WHERE{");
		builder.append(getWhereClause(mainFilter, facets));
		builder.append("}");
		return builder.toString();
	}

	public String getFacetValuesSparql(MainFilter mainFilter, Collection<Facet> facets, Facet focusFacet) {
		if(!facets.contains(focusFacet)){
			throw new RuntimeException("Facets set should contain the focus facet");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ?").append(focusFacet.getVarname()).append(" (COUNT(DISTINCT ?").append(mainFilter.getVarname()).append(") AS ?count) WHERE{");
		builder.append(mainFilter.getSparqlPattern());
		for(Facet f:facets){
			if(f.equals(focusFacet)){
				builder.append("{?").append(mainFilter.getVarname()).append(" ").append(f.getFilter().getPattern()).append(" ?").append(f.getVarname()).append(" .}");
			}else{
				builder.append(getFacetFilter(f, mainFilter.getVarname()));
			}
		}
		builder.append("} GROUP BY ?").append(focusFacet.getVarname());
		return builder.toString();
	}
	
	public String countItemsMissingFacetSparql(MainFilter mainFilter, Collection<Facet> facets, Facet focusFacet) {
		if(!facets.contains(focusFacet)){
			throw new RuntimeException("Facets set should contain the focus facet");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT (COUNT(DISTINCT ?").append(mainFilter.getVarname()).append(") AS ?count) WHERE{");
		builder.append(mainFilter.getSparqlPattern());
		for(Facet f:facets){
			if(f.equals(focusFacet)){
				builder.append("OPTIONAL {?").append(mainFilter.getVarname()).append(" ").append(f.getFilter().getPattern()).append(" ?").append(f.getVarname()).append(" .} FILTER(!bound(?")
				.append(f.getVarname()).append(")) .");
			}else{
				builder.append(getFacetFilter(f, mainFilter.getVarname()));
			}
		}
		builder.append("} GROUP BY ?").append(focusFacet.getVarname());
		return builder.toString();
	}
	
	public MainFilter refocusSaprql(MainFilter mainFilter, Collection<Facet> facets,	Facet refocusFacet) {
		String newPattern;
		if(refocusFacet.getFilter().getPattern().startsWith("^")){
			//to avoid having ^^
			newPattern = refocusFacet.getFilter().getPattern().substring(1);
		}else{
			newPattern = "^(" + refocusFacet.getFilter().getPattern() + ")";
		}
		MainFilter newMainFilter = mainFilter.extend(refocusFacet.getVarname(), newPattern);
		return newMainFilter;
	}
	
	public String resourcesDetailsSparql(Set<String> uris, Set<String> properties) {
		StringBuilder builder = new StringBuilder();
		builder.append(RESOURCES_DETAILS_SPARQL_PROLOG);
		builder.append(orFilter("p",properties));
		builder.append(orFilter("s",uris));
		builder.append("}");
		return builder.toString();
	}
	
	private String getWhereClause(MainFilter mainFilter, Collection<Facet> facets) {
		StringBuilder builder = new StringBuilder();
		builder.append(mainFilter.getSparqlPattern());
		for(Facet f:facets){
			String filter = getFacetFilter(f,mainFilter.getFacetsVarname());
			builder.append(filter);
		}
		return builder.toString();
	}
	
	private String getFacetFilter(Facet f, String varname){
		if(!f.hasSelection()){
			return "";
		}
		StringBuilder filter = new StringBuilder();
		filter.append("{");
		for(RdfDecoratedValue v:f.getSelections()){
			filter.append("{?").append(varname).append(" ").append(f.getFilter().getPattern()).append(" ").append(v.inSparqlFilter(f.getVarname())).append(" .}").append("UNION");
		}
		if(f.missingValueSelected()){
			filter.append("{?").append(varname).append(" ").append(f.getFilter().getPattern()).append(" ?").append(f.getVarname()).append(DISAMBIGUATION_SUFFIX).append(" .}").append("UNION");
		}
		
		//get rid of the last UNION
		SparqlUtil.getRidOfLastUnion(filter);

		if(f.missingValueSelected()){
			//left join
			filter.replace(0, 0, "OPTIONAL");
			filter.append("} FILTER(!bound(?").append(f.getVarname()).append(DISAMBIGUATION_SUFFIX).append(")) .");
		}else{
			filter.append("}");
		}
		return filter.toString();
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
	
	private static final String RESOURCES_DETAILS_SPARQL_PROLOG = "SELECT ?s ?p ?o WHERE{?s ?p ?o. ";
	private static final String DISAMBIGUATION_SUFFIX = "_v";
}
